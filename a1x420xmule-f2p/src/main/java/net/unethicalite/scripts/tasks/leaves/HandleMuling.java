package net.unethicalite.scripts.tasks.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.util.Text;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.events.HopWorldEvent;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.api.extended.ExTrade;
import net.unethicalite.scripts.api.launcher420.Log;
import net.unethicalite.scripts.api.muling.MuleClient;
import net.unethicalite.scripts.api.muling.MuleQueue;
import net.unethicalite.scripts.api.muling.RequiredItem;
import net.unethicalite.scripts.api.muling.messages.OwnedItem;
import net.unethicalite.scripts.api.muling.messages.client.MuleRequestMessage;
import net.unethicalite.scripts.Config;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.Leaf;
import net.unethicalite.scripts.framework.PluginInterface;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Slf4j
public class HandleMuling extends Leaf {
    public MuleClient muleClient = null;
    private WorldPoint muleTile = null;
    private int muleWorld = 0;
    private final int port = 42067;
    private void sendInitialInfoToServer() {
        // Create a map to hold item IDs and their aggregated quantities
        Map<Integer, Integer> itemMap = new HashMap<>();

        // Loop through all items in the container
        for (Item i : Inventory.getAll(i2 -> i2 != null && i2.getName() != null)) {
            // Aggregate item quantities by their IDs
            itemMap.put(i.getId(), itemMap.getOrDefault(i.getId(), 0) + i.getQuantity());
        }

        // Convert the map to a list of OwnedItem objects
        List<OwnedItem> ownedItems = itemMap.entrySet().stream()
                .map(entry -> new OwnedItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        this.muleClient.updateOwnedItems(ownedItems);

        //register to eventbus
        Static.getEventBus().register(this.muleClient);
    }
    private MuleClient getConnectedMuleClient() {
        URI uri = null;
        try {
            uri = new URI("ws://localhost:"+port);
        } catch (URISyntaxException e) {
            log.info("Error getting localhost on port "+port+" in URI parser in script onStart method! websocket client not initiated");
            return null;
        }
        Map<String, String> headers = new HashMap<>();

        //all clients handshake server with these headers
        headers.put("clientUsername", "Devious Client");
        headers.put("playerName", Text.sanitize(Players.getLocal().getName()));
        headers.put("isMule", "true");
        headers.put("isMember", (Game.getMembershipDays() > 0 ? "true" : "false"));

        //comma-seperated list of group names as String for groups header
        headers.put("groups", InterfaceInstance.pluginInterface.botGroup());

        //only sent from mule to broadcast mule's location
        //set tile to local tile on script start
        WorldPoint localTile = Players.getLocal().getWorldLocation();
        headers.put("worldId", String.valueOf(Worlds.getCurrentId()));
        headers.put("tileX", String.valueOf(localTile.getX()));
        headers.put("tileY", String.valueOf(localTile.getY()));
        headers.put("tileZ", String.valueOf(localTile.getPlane()));
        muleTile = Players.getLocal().getWorldLocation();
        muleWorld = Worlds.getCurrentId();
        MuleClient newMuleClient = new MuleClient(uri, headers);
        try {
            if (!newMuleClient.connectBlocking(15, TimeUnit.SECONDS)) {
                log.info("Failed to connect to server! 15s timeout");
                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return newMuleClient;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int execute() {
        if (this.muleClient == null) {
            this.muleClient = getConnectedMuleClient();
            if (this.muleClient == null) {
                return -1;
            }
            sendInitialInfoToServer();
            return -1;
        }
        if (this.muleClient.isClosing()) {
            log.info("Waiting for MuleClient to close");
            return -1;
        }
        if (this.muleClient.isClosed()) {
            log.info("Setting MuleClient to null after closed");
            this.muleClient = null;
            return -1;
        }
        if (this.muleWorld != Worlds.getCurrentId()) {
            new HopWorldEvent(this.muleWorld).execute();
            return -1;
        }
        if (Trade.isOpen()) {
            String currentTraderName = Text.sanitize(Trade.getTradingPlayer());
            MuleRequestMessage validRequest = muleClient.getRequestForBotName(currentTraderName);
            if (validRequest == null) {
                log.info("Declining trade due to name: "+currentTraderName+" not matching any in list of active requests");
                Trade.decline();
                return -1;
            }
            if (Trade.isFirstScreenOpen()) {
                for (RequiredItem requiredItem : validRequest.requiredItems) {
                    Item itemInTradeWindow = Trade.getFirst(false, requiredItem.getItemId());
                    if (itemInTradeWindow == null) {
                        Item itemInInvy = Inventory.getFirst(requiredItem.getItemId());
                        if (itemInInvy != null) {
                            //offer in full quantity because none in trade window
                            Trade.offer(requiredItem.getItemId(),requiredItem.getQuantity());
                            Time.sleepTick();
                            continue;
                        }
                    }
                    //here some required items in trade window
                    // too much
                    if (itemInTradeWindow.getQuantity() > requiredItem.getQuantity()) {
                        ExTrade.remove(requiredItem.getItemId(), itemInTradeWindow.getQuantity() - requiredItem.getQuantity());
                        return -1;
                    }
                    //not enough
                    if (itemInTradeWindow.getQuantity() < requiredItem.getQuantity()) {
                        Trade.offer(requiredItem.getItemId(), requiredItem.getQuantity() - itemInTradeWindow.getQuantity());
                        return -1;
                    }
                    //just right, continue
                }
            }

            //here we know our side of trade window is correctly fulfilled for all items in reqest, accept
            if (!Trade.hasAccepted(false)) {
                Trade.accept();
            }
            return -1;
        }
        int activeTradersSize = MuleQueue.activeTraders.size();
        log.info("Have active trader size: "+activeTradersSize);
        if (!MuleQueue.activeTraders.isEmpty()) {
            Player bot = Players.getNearest(MuleQueue.activeTraders.get(0));
            if (bot == null) {
                log.info("Bot not found, removing! name: " + MuleQueue.activeTraders.remove(0));
                muleClient.sendTradeCompletedMessage(MuleQueue.activeRequests.remove(0), false, "Mule found that bot to trade was null in-game!");
                return 100;
            }
            log.info("Bot to trade found: "+bot.getName());
            bot.interact("Trade with");
            Time.sleepUntil(() -> Trade.isOpen(), 100,8000);
            return 100;
        }

        if (this.muleTile.distanceTo(Players.getLocal()) >= 3 || !Reachable.isWalkable(this.muleTile)) {
            new MovementEvent().setDestination(this.muleTile).execute();
            return -1;
        }
        if (Bank.isOpen()) {
            Bank.close();
            return -1;
        }

        log.info("Waiting on a trader! active traders list is null (populated when receive unknown trader confirmation)");
        return -1;
    }
    private void disposeMuleClientInstance() {
        if (this.muleClient != null) {
            if (this.muleClient.isClosed()) {
                log.info("Setting MuleClient to null after closed");
                this.muleClient = null;
                return;
            }
            if (this.muleClient.isClosing()) {
                log.info("Waiting for MuleClient to close");
                return;
            }
            log.info("Closing MuleClient");
            this.muleClient.close();
        }
    }
    @Subscribe
    public void onWorldChanged (WorldChanged changeEvent) {


        //event is posted after client sets world and world type, so get from client instead of event
        int hoppedTo = Static.getClient().getWorld();
        boolean isMembers = false;
        //find out if we hopped to members world, and if we are set in config to be members, if so, set as members world
        for (WorldType applicableType : Static.getClient().getWorldType()) {
            if (applicableType.equals(WorldType.MEMBERS)) {
                isMembers = true;
            }
        }
        if (isMembers && InterfaceInstance.pluginInterface.shouldBondUp()) {
            this.muleWorld = hoppedTo;
            log.info("Setting new P2P mule world to: "+this.muleWorld);
            disposeMuleClientInstance();
        }
        //if we are in f2p world, then check if we should be in f2p, if so, set to f2p world
        if (!isMembers && !InterfaceInstance.pluginInterface.shouldBondUp()) {
            this.muleWorld = hoppedTo;
            log.info("Setting new F2P mule world to: "+this.muleWorld);
            disposeMuleClientInstance();
        }
    }
}
