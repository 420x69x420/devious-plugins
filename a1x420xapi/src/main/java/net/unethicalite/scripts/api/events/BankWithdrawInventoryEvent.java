package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.api.muling.OfferedItem;
import net.unethicalite.scripts.api.muling.RequiredItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BankWithdrawInventoryEvent extends AbstractEvent {
    private List<Item> itemsToWithdraw = new ArrayList<>();
    public BankWithdrawInventoryEvent() {
        this.setEventCompletedCondition(() -> fulfilled());
        this.setEventTimeoutTicks(200);
    }

    public static BankWithdrawInventoryEvent createWithItems(Plugin plugin,List<Item> items) {
        BankWithdrawInventoryEvent event = new BankWithdrawInventoryEvent();
        for (Item i : items) {
            event.addItem(i.getId(), i.getQuantity());
        }
        return event;
    }
    public static BankWithdrawInventoryEvent createWithOfferedItems(Plugin plugin,List<OfferedItem> items) {
        BankWithdrawInventoryEvent event = new BankWithdrawInventoryEvent();
        for (OfferedItem i : items) {
            event.addItem(i.getItemId(), i.getQuantity());
        }
        return event;
    }
    public static BankWithdrawInventoryEvent createWithRequiredItems(Plugin plugin, List<RequiredItem> items) {
        BankWithdrawInventoryEvent event = new BankWithdrawInventoryEvent();
        for (RequiredItem i : items) {
            event.addItem(i.getItemId(), i.getQuantity());
        }
        return event;
    }
    public BankWithdrawInventoryEvent addItem(int id, int qty) {
        itemsToWithdraw.add(new Item(id, qty));
        return this;
    }
    @Override
    public void onLoop() {
        if (!Bank.isOpen()) {
            log.info("[BANK WITHDRAW INVENTORY EVENT] - Opening bank");
            new OpenBankEvent()
                    .execute();
            return;
        }
        // check extra items
        List<Item> extraItems = getExtraItems(itemsToWithdraw);
        if (!extraItems.isEmpty()) {
            log.info("[BANK WITHDRAW INVENTORY EVENT] - Depositing extra inventory items");
            int depositedThisTick = 1;
            for (Item i : extraItems) {
                if (depositedThisTick >= 10) {
                    depositedThisTick = 0;
                    Time.sleepTick();
                }
                Bank.deposit(i.getId(), i.getQuantity());
                depositedThisTick++;
            }
            return;
        }

        //check needed items - have no extra items here
        log.info("[BANK WITHDRAW INVENTORY EVENT] - Withdrawing inventory");
        int withdrawnThisTick = 1;
        for (Item item : itemsToWithdraw) {
            if (withdrawnThisTick >= 10) {
                withdrawnThisTick = 1;
                Time.sleepTick();
            }
            if (!OwnedItems.containsIncludingNoted(item.getId())) {
                log.info("[BANK WITHDRAW INVENTORY EVENT] Not have id owned: "+item.getId());
                continue;
            }
            int currentQuantity = Bank.Inventory.getCount(true, item.getId());
            int neededQuantity = item.getQuantity() - currentQuantity;
            if (neededQuantity <= 0) {
                log.info("[BANK WITHDRAW INVENTORY EVENT] Already have ID: "+item.getId()+ " in inventory in quantity: "+currentQuantity);
                continue;
            }
            int withdrawID = OwnedItems.getUnnotedID(item.getId());
            Bank.WithdrawMode withdrawMode = (OwnedItems.isNoted(item.getId()) ? Bank.WithdrawMode.NOTED : Bank.WithdrawMode.ITEM);
            log.info("[BANK WITHDRAW INVENTORY EVENT] withdrawing id: "+withdrawID+ " in qty: "+ neededQuantity+" in noted mode: "+withdrawMode.name());
            Bank.withdraw(withdrawID, neededQuantity, withdrawMode);
            withdrawnThisTick++;
        }
    }
    public boolean fulfilled() {
        // check extra items
        List<Item> extraItems = getExtraItems(itemsToWithdraw);
        if (!extraItems.isEmpty()) {
            return false;
        }

        // check missing items
        for (Item item : itemsToWithdraw) {
            if (!OwnedItems.contains(item.getId())) {
                return false;
            }
            Item invyItem = Bank.Inventory.getFirst(item.getId());

            if (Bank.Inventory.getCount(item.getId()) == 0 ||
                    invyItem == null ||
                    invyItem.getId() == -1 ||
                    invyItem.getName() == null ||
                    invyItem.getName().equalsIgnoreCase("null"))
            {
                return false;
            }

            int currentQuantity = Bank.Inventory.getCount(true, item.getId());
            int neededQuantity = item.getQuantity() - currentQuantity;
            if (neededQuantity > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns list<Item> which just represent ID/qty, not actual item composition
     * @param otherThan
     * @return
     */
    public List<Item> getExtraItems(List<Item> otherThan) {
        //log.debug("getExtraItems Inventory");
        List<Item> extraItems = new ArrayList<>();

        // Create a map to hold the total quantity of each item ID in the inventory
        Map<Integer, Integer> inventoryCounts = new HashMap<>();
        for (Item current : (Bank.isOpen() ? Bank.Inventory.getAll() : Inventory.getAll())) {
            inventoryCounts.merge(current.getId(), current.getQuantity(), Integer::sum);
        }

        for (Map.Entry<Integer, Integer> entry : inventoryCounts.entrySet()) {
            boolean isIdFoundInDesired = false;
            int desiredQty = 0;
            for (Item desired : otherThan) {
                int compareId = desired.getId();
                if (entry.getKey() == compareId) {
                    desiredQty = desired.getQuantity();
                    isIdFoundInDesired = true;
                    break;
                }
            }
            if (!isIdFoundInDesired || entry.getValue() > desiredQty) {
                int extraQty = entry.getValue() - (isIdFoundInDesired ? desiredQty : 0);
                //log.debug("Found extra item: "+entry.getKey()+ " in extra qty: "+ extraQty);
                extraItems.add(new Item(entry.getKey(), extraQty));
            }
        }
        return extraItems;
    }
}
