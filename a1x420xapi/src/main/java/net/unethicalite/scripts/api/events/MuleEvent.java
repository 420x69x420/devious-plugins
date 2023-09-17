package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.util.Text;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.api.utils.Sleep;
import net.unethicalite.scripts.api.muling.BotClient;
import net.unethicalite.scripts.api.muling.OfferedItem;
import net.unethicalite.scripts.api.muling.RequiredItem;
import net.unethicalite.scripts.api.muling.messages.client.MuleRequestMessage;
import net.unethicalite.scripts.api.muling.messages.client.TradeCompletedMessage;
import net.unethicalite.scripts.api.muling.messages.client.TradeRequestMessage;
import net.unethicalite.scripts.api.muling.messages.server.MuleResponseMessage;
import net.unethicalite.scripts.api.muling.messages.server.TradeResponseMessage;
import net.unethicalite.scripts.framework.InterfaceInstance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MuleEvent extends AbstractEvent {
    private MuleResponseMessage muleResponseMessage = null;
    private MuleRequestMessage muleRequestMessage = null;
    private TradeResponseMessage tradeResponseMessage = null;
    private TradeRequestMessage tradeRequestMessage = null;
    private TradeCompletedMessage tradeCompletedMessage = null;
    private List<RequiredItem> requiredItems = new ArrayList<>();
    private List<OfferedItem> offeredItems = new ArrayList<>();
    private MuleSubEvent subEvent;
    private BotClient botClient;
    private int port = 42067;
    private List<OfferedItem> validatedItemsToOffer;
    private BankWithdrawInventoryEvent withdrawEvent;
    private String botGroup;
    public MuleEvent() {
        this.botGroup = InterfaceInstance.pluginInterface.botGroup();
        this.setEventCompletedCondition(() -> subEvent != null);
        this.setEventTimeoutTicks(200);
    }

    public MuleEvent setBotGroup(String botGroup) {
        this.botGroup = botGroup;
        return this;
    }
    public MuleEvent setOfferedItems(List<OfferedItem> offeredItems) {
        this.offeredItems = offeredItems;
        return this;
    }
    public MuleEvent setRequiredItems(List<RequiredItem> requiredItems) {
        this.requiredItems = requiredItems;
        return this;
    }
    public MuleEvent addRequiredItem(int id, int qty) {
        this.requiredItems.add(new RequiredItem(id, qty));
        return this;
    }
    public MuleEvent addAllOwnedOfferedItem(int id) {
        this.offeredItems.add(new OfferedItem(id, OwnedItems.getCountIncludingNoted(id)));
        return this;
    }
    public MuleEvent addOfferedItem(int id, int qty) {
        this.offeredItems.add(new OfferedItem(id, qty));
        return this;
    }
    public MuleEvent setPort(int port) {
        this.port = port;
        return this;
    }
    public boolean connect() {
        if (botClient != null) {
            return true;
        }
        URI uri = null;
        try {
            uri = new URI("ws://localhost:"+port);
        } catch (URISyntaxException e) {
            log.info("Error getting localhost on port "+port+" in URI parser in script onStart method! websocket client not initiated");
            return false;
        }
        Map<String, String> headers = new HashMap<>();

        //all clients handshake server with these headers
        headers.put("clientUsername", "Devious Client");
        headers.put("playerName", Text.sanitize(Players.getLocal().getName()));
        headers.put("isMule", "false");
        headers.put("isMember", (Game.getMembershipDays() > 0  || Worlds.getCurrentWorld().isMembers() ? "true" : "false"));

        //comma-seperated list of group names as String for groups header
        headers.put("groups", botGroup);

        botClient = new BotClient(uri, headers);
        try {
            if (!botClient.connectBlocking(15, TimeUnit.SECONDS)) {
                log.info("[MULE EVENT] Failed to connect to server after 15 seconds!");
                botClient = null;
                return false;
            }
            Static.getEventBus().register(botClient);
            log.info("[MULE EVENT] Connected to main mule server on port: "+port);
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onLoop() {
        if (!connect()) {
            return;
        }
        Sleep.shortSleep();
        log.info("[MULE EVENT] fulfill offered items stage");
        //populate validated items to offer one time upon event start
        if (validatedItemsToOffer == null) {
            validatedItemsToOffer = validateItemsToOffer(offeredItems);
        }

        // withdraw all validated items
        if (!validatedItemsToOffer.isEmpty() && withdrawEvent == null) {
            withdrawEvent = BankWithdrawInventoryEvent.createWithOfferedItems(plugin, validatedItemsToOffer);
            withdrawEvent.execute();
            return;
        }

        if (!validatedItemsToOffer.isEmpty() && !withdrawEvent.fulfilled()) {
            log.info("[MULE EVENT] failed the fulfill stage! Not fulfilled");
            this.setEventFailed(true);
            return;
        }

        //deposit inventory if nothing to offer to mule
        if (validatedItemsToOffer.isEmpty() && !Inventory.isEmpty()) {
            if (!Bank.isOpen()) {
                new OpenBankEvent().execute();
                return;
            }
            Bank.depositInventory();
            return;
        }

        //here we either have no validated items to offer with empty inventory, or we have all of them in our inventory

        //start sub-loop to find and trade mule after fulfilling our inventory loadout of items to give

        log.info("[MULE EVENT] Test");
        Static.getEventBus().register(BotClient.class);
        log.info("[MULE EVENT] Test2");
        this.subEvent = new MuleSubEvent(botClient, validatedItemsToOffer, requiredItems);
        if (this.subEvent.executed()) {
            log.info("[MULE EVENT] Test3 YES");
        } else {
            log.info("[MULE EVENT] Test3 NO");
        }
        log.info("[MULE EVENT] Test4");
        Static.getEventBus().unregister(BotClient.class);
    }
    private static List<OfferedItem> validateItemsToOffer(List<OfferedItem> offeredItems) {
        List<OfferedItem> validatedItemsToOffer = new ArrayList<>();
        for (OfferedItem offered : offeredItems) {
            log.info("[MULE EVENT] Validating item: "+offered.getItemId()+" in qty "+offered.getQuantity());
            ItemComposition itemInstance = Static.getClient().getItemComposition(offered.getItemId());
            //not a valid item ID
            if (itemInstance == null) {
                log.info("[MULE EVENT] Not a valid item ID!");
                continue;
            }
            //find all version of item (nonstackable + notable, or stackable)
            boolean stackable = itemInstance.isStackable();
            int itemID;
            if (stackable) {
                itemID = offered.getItemId();
            } else {
                itemID = OwnedItems.getNotedID(itemInstance);
            }
            if (OwnedItems.containsIncludingNoted(offered.getItemId())) {
                // Add only the quantity of items we actually have, up to the amount we are willing to offer
                validatedItemsToOffer.add(new OfferedItem(itemID, Math.min(OwnedItems.getCountIncludingNoted(itemID), offered.getQuantity())));
            }
        }

        log.info("Finished validation with validated items: ");
        validatedItemsToOffer.stream().forEach(t -> log.info("ID: "+t.getItemId()+", qty: "+t.getQuantity()));
        return validatedItemsToOffer;
    }
}
