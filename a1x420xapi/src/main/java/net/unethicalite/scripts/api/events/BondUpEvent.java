package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Prices;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.api.utils.Sleep;

@Slf4j
public class BondUpEvent extends AbstractEvent {
    private HopWorldEvent F2PHopEvent;
    private HopWorldEvent P2PHopEvent;
    private int initialMembershipDays;
    private boolean interactedConfirmBond = false;
    public BondUpEvent() {
        initialMembershipDays = Game.getMembershipDays();
        this.setEventCompletedCondition(() -> Game.getMembershipDays() > initialMembershipDays && Worlds.getCurrentWorld().isMembers() && Game.getState() == GameState.LOGGED_IN);
        this.setEventTimeoutTicks(200);
    }

    @Override
    public void onLoop() {
        if (Game.getState() != GameState.LOGGED_IN) {
            return;
        }

        if (interactedConfirmBond) {
            if (F2PHopEvent == null) {
                F2PHopEvent = new HopWorldEvent(Worlds.getRandom(w -> w.isNormal() && !w.isMembers()).getId());
                F2PHopEvent.execute();
                return;
            }
            if (F2PHopEvent.isEventFailed()) {
                log.info("[BOND UP EVENT] Failed to hop to F2P world after using bond!");
                setEventFailed(true);
                return;
            }
            if (P2PHopEvent == null) {
                P2PHopEvent = new HopWorldEvent(Worlds.getRandom(w -> w.isNormal() && w.isMembers()).getId());
                P2PHopEvent.execute();
                return;
            }
            if (P2PHopEvent.isEventFailed()) {
                log.info("[BOND UP EVENT] Failed to hop to P2P world after using bond and hopping to F2P world!");
                setEventFailed(true);
                return;
            }
            log.info("[BOND UP EVENT] Unknown failure reason! Membership days var not updated?");
            this.setEventFailed(true);
            return;
        }

        //check if we have a bond (tradeable or non-tradeable) already owned
        int bondID = -1;
        if (OwnedItems.contains(ItemID.OLD_SCHOOL_BOND_UNTRADEABLE)) {
            bondID = ItemID.OLD_SCHOOL_BOND_UNTRADEABLE;
        } else if (OwnedItems.contains(ItemID.OLD_SCHOOL_BOND)) {
            bondID = ItemID.OLD_SCHOOL_BOND;
        }
        //we do own a bond already
        if (bondID != -1) {
            if (Bank.isOpen()) {
                if (Bank.Inventory.getCount(bondID) > 0) {
                    Bank.close();
                    return;
                }
                if (Inventory.isFull()) {
                    Bank.depositInventory();
                    return;
                }
                log.info("[BOND UP EVENT] Withdraw bond");
                Bank.withdraw(bondID,1, Bank.WithdrawMode.ITEM);
                return;
            }
            if (GrandExchange.isOpen()) {
                GrandExchange.close();
                return;
            }
            Widget bondButton = get1BondButton();
            if (bondButton != null) {
                log.info("[BOND UP EVENT] Start bond sequence: interact '1 Bond'");
                get1BondButton().interact("1 Bond");
                Sleep.shortSleep();
                log.info("[BOND UP EVENT] interact 'Confirm'");
                getBondConfirmButton().interact("Confirm");
                Time.sleepTicks(2);
                Sleep.shortSleep();
                log.info("[BOND UP EVENT] interact local tile to cancel waiting dialogue");
                Movement.walk(Players.getLocal());
                interactedConfirmBond = true;
            }
            Item bond = Inventory.getFirst(bondID);
            if (bond != null) {
                bond.interact("Redeem");
            }
            return;
        }

        //check bond price + 15% and mule over coins for this purpose if required
        int bondPrice = (int) (Prices.getItemPrice(ItemID.OLD_SCHOOL_BOND) * 1.15);
        if (OwnedItems.getCount(true, ItemID.COINS_995) < bondPrice) {
            new MuleEvent()
                    .setBotGroup("f2p")
                    .addRequiredItem(ItemID.COINS_995, bondPrice)
                    .addOfferedItem(ItemID.COINS_995, OwnedItems.getCount(true, ItemID.COINS_995))
                    .execute();
            return;
        }

        new BuyItemEvent()
                .setId(ItemID.OLD_SCHOOL_BOND)
                .setAmount(1)
                .setPrice(bondPrice)
                .setEventCompletedCondition(() -> OwnedItems.contains(ItemID.OLD_SCHOOL_BOND_UNTRADEABLE))
                .execute();
    }
    private Widget get1BondButton() {
        return Widgets.get(66,7);
    }
    private Widget getBondConfirmButton() {
        return Widgets.get(66,24);
    }
}
