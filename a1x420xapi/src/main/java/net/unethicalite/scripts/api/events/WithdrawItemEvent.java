package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.api.utils.Sleep;
import net.unethicalite.scripts.api.loadouts.LoadoutItem;

@Slf4j
public class WithdrawItemEvent extends AbstractEvent {
    private LoadoutItem loadoutItem;
    private BankLocation bankLocation;

    public WithdrawItemEvent() {
        this.setEventCompletedCondition(() -> loadoutItem.hasItem());
        this.setEventTimeoutTicks(200);
    }

    public LoadoutItem getLoadoutItem() {
        return loadoutItem;
    }

    public WithdrawItemEvent setLoadoutItem(LoadoutItem loadoutItem) {
        this.loadoutItem = loadoutItem;
        return this;
    }

    public BankLocation getBankLocation() {
        return bankLocation;
    }

    public WithdrawItemEvent setBankLocation(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }

    @Override
    public void onLoop() {
        Sleep.shortSleep();
        if (getLoadoutItem() == null) {
            log.info("[WITHDRAW ITEM EVENT] - Can't find loadout item");
            return;
        }

        if (!getLoadoutItem().ownsItem()) {
            int targetAmount = getLoadoutItem().getMinAmount() != 0 ? (Bank.isOpen() || GrandExchange.isOpen() ? getLoadoutItem().getMaxAmount() : getLoadoutItem().getMinAmount()) : getLoadoutItem().getAmount();
            int toBuyAmount = getLoadoutItem().getRestock() != 0 ? getLoadoutItem().getRestock() : targetAmount - OwnedItems.getCount(true, getLoadoutItem().getName());
            log.info("[WITHDRAW ITEM EVENT] - I need to buy " + toBuyAmount + " " + getLoadoutItem().getName());
            new BuyItemEvent()
                    .setName(getLoadoutItem().getName())
                    .setAmount(toBuyAmount)
                    .setPrice(getLoadoutItem().getPrice())
                    .execute();
            return;
        }

        if (!Bank.isOpen()) {
            log.info("[WITHDRAW ITEM EVENT] - Opening bank");
            new OpenBankEvent()
                    .setBankLocation(getBankLocation())
                    .execute();
            return;
        }

        int inventoryAmount = Inventory.getCount(true, x -> x.getName().equals(getLoadoutItem().getName()) && ((getLoadoutItem().isNoted() && x.isNoted()) || (!getLoadoutItem().isNoted() && !x.isNoted())));
        int targetAmount = getLoadoutItem().getMinAmount() != 0 ? getLoadoutItem().getMaxAmount() : getLoadoutItem().getAmount();
        int missingAmount = targetAmount - inventoryAmount;
        int neededSpace = getLoadoutItem().isNoted() || Static.getItemManager().getItemComposition(getLoadoutItem().getId()).isStackable() ? 1 : missingAmount;

        if (missingAmount < 0) {
            log.info("[WITHDRAW ITEM EVENT] - Depositing " + Math.abs(missingAmount) + " " + getLoadoutItem().getName());
            Bank.deposit(x -> x.getName().equals(getLoadoutItem().getName()) && ((getLoadoutItem().isNoted() && x.isNoted()) || (!getLoadoutItem().isNoted() && !x.isNoted())), Math.abs(missingAmount));
            Time.sleepTicksUntil(() -> getLoadoutItem().hasItem(), 3);
            return;
        }

        if (Inventory.getFreeSlots() < neededSpace) {
            log.info("[WITHDRAW ITEM EVENT] - Banking inventory to make space");
            new BankInventoryEvent()
                    .execute();
            return;
        }

        if (Bank.getCount(true, getLoadoutItem().getName()) < missingAmount) {
            log.info("[WITHDRAW ITEM EVENT] - I need to bank other form of item");
            Bank.depositAll(x -> x.getName().equals(getLoadoutItem().getName()) && ((getLoadoutItem().isNoted() && !x.isNoted()) || (!getLoadoutItem().isNoted() && x.isNoted())));
            return;
        }

        if (missingAmount != 0) {
            log.info("[WITHDRAW ITEM EVENT] - Withdrawing " + missingAmount + " " + getLoadoutItem().getName());
            Bank.withdraw(getLoadoutItem().getName(), missingAmount, getLoadoutItem().isNoted() ? Bank.WithdrawMode.NOTED : Bank.WithdrawMode.ITEM);
            Time.sleepTicksUntil(() -> getLoadoutItem().hasItem(), 3);
            return;
        }

        if (getLoadoutItem().isEquipment()) {
            String itemName = getLoadoutItem().getName();
            Item item = Bank.isOpen() ? Bank.Inventory.getFirst(x -> x.getName().equals(itemName)) : Inventory.getFirst(x -> x.getName().equals(itemName));

            if (item == null) {
                log.info("[WITHDRAW ITEM EVENT] - Can't find " + itemName + " to equip");
                return;
            }

            String action = item.hasAction("Equip") ? "Equip" : item.hasAction("Wield") ? "Wield" : item.hasAction("Wear") ? "Wear" : null;
            if (action == null) {
                log.info("[WITHDRAW ITEM EVENT] - Can't find equip action for " + itemName);
                return;
            }

            log.info("[WITHDRAW ITEM EVENT] - Equipping " + itemName);
            item.interact(action);
            Time.sleepUntil(() -> getLoadoutItem().hasItem(), 3);
        }
    }
}
