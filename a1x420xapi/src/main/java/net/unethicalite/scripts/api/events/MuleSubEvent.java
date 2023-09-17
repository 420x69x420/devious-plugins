package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.util.Text;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.scripts.api.utils.Sleep;
import net.unethicalite.scripts.api.muling.BotClient;
import net.unethicalite.scripts.api.muling.OfferedItem;
import net.unethicalite.scripts.api.muling.RequiredItem;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MuleSubEvent extends AbstractEvent {
    private List<RequiredItem> requiredItems = new ArrayList<>();
    private List<OfferedItem> validatedItemsToOffer = new ArrayList<>();
    private final BotClient botClient;
    private boolean inTradeOneTime;

    public MuleSubEvent(BotClient botClient, List<OfferedItem> validatedItemsToOffer, List<RequiredItem> requiredItems) {
        this.validatedItemsToOffer = validatedItemsToOffer;
        this.requiredItems = requiredItems;
        this.botClient = botClient;
        this.inTradeOneTime = false;
        this.setEventTimeoutTicks(200);
    }

    @Override
    public void onLoop() {
        Sleep.shortSleep();
        log.info("[MULE SUB EVENT] walk + find mule + trade items stage");
        // make initial request if not make one for this event already
        if (botClient.muleRequestMessage == null) {
            botClient.sendMuleRequest(requiredItems, validatedItemsToOffer);
            return;
        }

        //we have received a mule response
        if (botClient.muleResponseMessage != null) {
            if (!botClient.muleResponseMessage.success) {
                log.info("[MULE SUB EVENT] Failed to get successful mule response! Error: "+botClient.muleResponseMessage.errorMessage);
                this.setEventFailed(true);
                return;
            }
            WorldPoint muleTile = new WorldPoint(
                    botClient.muleResponseMessage.location.x,
                    botClient.muleResponseMessage.location.y,
                    botClient.muleResponseMessage.location.z
            );
            int muleWorldID = botClient.muleResponseMessage.world;
            // walk to be directly adjacent to mule
            if (muleTile.distanceTo(Players.getLocal()) > 2) {
                if (!Movement.isWalking()) {
                    Movement.walkTo(muleTile);
                }
                return;
            }

            // detect finished mule by having all requested items in inventory in at exact quantity
            BankWithdrawInventoryEvent muleFinishedInventory = BankWithdrawInventoryEvent.createWithRequiredItems(plugin, requiredItems);
            if (muleFinishedInventory.fulfilled() || botClient.tradeCompletedMessage != null) {
                if (Trade.isOpen()) {
                    if (!Trade.hasAccepted(false)) {
                        Trade.accept();
                    }
                    return;
                }
                if (muleWorldID == Worlds.getCurrentId()) {
                    new HopWorldEvent().execute();
                    return;
                }
                log.info("[MULE SUB EVENT] Finished mule event with items: ");
                requiredItems.stream().forEach(i -> log.info("[MULE SUB EVENT] "+i.getItemId() +":"+i.getQuantity()));
                if (muleFinishedInventory.fulfilled()){
                    botClient.sendTradeCompletedMessage(true, "Have all our items expected from mule event!");
                }
                if (botClient.tradeCompletedMessage != null) {
                    if (botClient.tradeCompletedMessage.success) {
                        log.info("[MULE SUB EVENT] Successful mule!");
                        this.setEventCompleted(true);
                    } else {
                        log.info("[MULE SUB EVENT] Failed mule! Trade completed reason: "+ botClient.tradeCompletedMessage.reason);
                        this.setEventFailed(true);
                    }
                    return;
                }
                //wait for trade completed message to arrive from server
                return;
            }
            if (muleWorldID != Worlds.getCurrentId()) {
                new HopWorldEvent(muleWorldID).execute();
                return;
            }
            if (botClient.tradeResponseMessage != null) {
                if (!botClient.tradeResponseMessage.success) {
                    log.info("[MULE SUB EVENT] Failed to get mule name from trade request! Error in server-sided response: "+botClient.tradeResponseMessage.errorMessage);
                    this.setEventFailed(true);
                    return;
                }

                Player mule = Players.getNearest(botClient.tradeResponseMessage.playerName);
                if (mule == null) {
                    log.info("[MULE SUB EVENT] Waiting for mule to show up at their reported location from mule request handshake");
                    Time.sleepTicks(2);
                    return;
                }
                // Somehow our injection client interacted with another player than our mule maybe lol
                if (Trade.isOpen() && !Text.toJagexName(mule.getName()).equals(Text.toJagexName(Trade.getTradingPlayer()))) {
                    Trade.decline();
                    return;
                }
                if (Trade.isFirstScreenOpen()) {
                    inTradeOneTime = true;
                    if (!Inventory.isEmpty()) {
                        Trade.offer(i -> i.getName() != null, Integer.MAX_VALUE);
                        return;
                    }
                    if (Inventory.isEmpty() && !Trade.hasAcceptedFirstScreen(false)) {
                        Trade.accept();
                    }
                    return;
                }
                if (Trade.isSecondScreenOpen()) {
                    inTradeOneTime = true;
                    if (!Trade.hasAcceptedSecondScreen(false)) {
                        Trade.acceptSecondScreen();
                    }
                    return;
                }
                if (inTradeOneTime) {
                    botClient.sendTradeCompletedMessage(false, "Out of trade after being in trade one time");
                    return;
                }
                if (!Reachable.isInteractable(mule)) {
                    if (!Movement.isWalking()) {
                        Movement.walkTo(mule);
                    }
                    return;
                }
                log.info("[MULE SUB EVENT] Interact 'Trade with' on mule: "+mule.getName());
                mule.interact("Trade with");
                if (!Time.sleepUntil(() -> Trade.isOpen(), 100, 10000)) {
                    this.setEventFailed(true);
                }
                return;
            }

            // Here we have not yet received a trade response from server
            // Check if we have sent a request yet
            if (botClient.tradeRequestMessage == null) {
                log.info("[MULE SUB EVENT] Sending trade request message to obtain mule name");
                botClient.sendTradeRequestMessage();
            }
            // Wait for server to respond with trade response
        }

        // Wait for server to respond with mule response
    }

}
