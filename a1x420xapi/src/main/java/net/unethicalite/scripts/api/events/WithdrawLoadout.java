package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.api.utils.Sleep;
import net.unethicalite.scripts.api.loadouts.Loadout;
import net.unethicalite.scripts.api.loadouts.LoadoutItem;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WithdrawLoadout extends AbstractEvent {
    private Loadout loadout;
    private BankLocation bankLocation;
    private boolean restockAllAtOnce;
    private List<LoadoutItem> loadoutItemsToBuyList = new ArrayList<>();

    public WithdrawLoadout() {
        this.setRestockAllAtOnce(true);
        this.setEventCompletedCondition(() -> loadout.hasLoadout());
        this.setEventTimeoutTicks(200);
    }

    public Loadout getLoadout() {
        return loadout;
    }

    public WithdrawLoadout setLoadout(Loadout loadout) {
        this.loadout = loadout;
        return this;
    }

    public BankLocation getBankLocation() {
        return bankLocation;
    }

    public WithdrawLoadout setBankLocation(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }

    public boolean isRestockAllAtOnce() {
        return restockAllAtOnce;
    }

    public WithdrawLoadout setRestockAllAtOnce(boolean restockAllAtOnce) {
        this.restockAllAtOnce = restockAllAtOnce;
        return this;
    }

    @Override
    public void onLoop() {
        if (getLoadout() == null) {
            log.info("[WITHDRAW LOADOUT EVENT] - Can't find loadout");
            return;
        }
        Sleep.shortSleep();

        if (!isRestockAllAtOnce()) {
            for (LoadoutItem loadoutItem : getLoadout().getLoadoutItemList()) {
                if (!loadoutItem.ownsItem()) {
                    if (!Bank.isOpen() && !GrandExchange.isOpen()) {
                        new OpenGEEvent()
                                .execute();
                        return;
                    }

                    int targetAmount = loadoutItem.getMinAmount() != 0 ? (Bank.isOpen() || GrandExchange.isOpen() ? loadoutItem.getMaxAmount() : loadoutItem.getMinAmount()) : loadoutItem.getAmount();
                    int toBuyAmount = loadoutItem.getRestock() != 0 ? (loadoutItem.getRestock() - OwnedItems.getCount(true, loadoutItem.getName())) : (targetAmount - OwnedItems.getCount(true, loadoutItem.getName()));

                    if (toBuyAmount > 0) {
                        log.info("[WITHDRAW LOADOUT EVENT] - Adding to buy list: " + toBuyAmount + " " + loadoutItem.getName());
                        loadoutItemsToBuyList.add(new LoadoutItem()
                                .setName(loadoutItem.getName())
                                .setAmount(toBuyAmount)
                        );
                    }
                }
            }
        }

        for (LoadoutItem loadoutItem : getLoadout().getLoadoutItemList()) {
            if (!loadoutItem.hasItem() && isRestockAllAtOnce()) {
                for (LoadoutItem item : getLoadout().getLoadoutItemList()) {
                    int targetAmount = item.getMinAmount() != 0 ? item.getMaxAmount() : item.getAmount();
                    int toBuyAmount = item.getRestock() != 0 ? (item.getRestock() - OwnedItems.getCount(true, item.getName())) : (targetAmount - OwnedItems.getCount(true, item.getName()));

                    log.info("Restock: " + item.getRestock());
                    log.info("Owned: " + OwnedItems.getCount(true, item.getName()));
                    if (toBuyAmount > 0) {
                        log.info("[WITHDRAW LOADOUT EVENT] - Adding to buy list: " + toBuyAmount + " " + item.getName());
                        loadoutItemsToBuyList.add(new LoadoutItem()
                                .setName(item.getName())
                                .setAmount(toBuyAmount)
                        );
                    }
                }
                break;
            }
        }

        if (!loadoutItemsToBuyList.isEmpty()) {
            for (LoadoutItem loadoutItem : loadoutItemsToBuyList) {
                log.info("[WITHDRAW LOADOUT EVENT] - Buying " + loadoutItem.getAmount() + " " + loadoutItem.getName());
                new BuyItemEvent()
                        .setName(loadoutItem.getName())
                        .setAmount(loadoutItem.getAmount())
                        .execute();
            }

            log.info("[WITHDRAW LOADOUT EVENT] - Clearing buy list");
            loadoutItemsToBuyList = new ArrayList<>();
            return;
        }

        if (!loadout.hasLoadout()) {
            for (LoadoutItem loadoutItem : getLoadout().getLoadoutItemList()) {
                if (!loadoutItem.hasItem()) {
                    log.info("[WITHDRAW LOADOUT EVENT] - Withdrawing " + loadoutItem.getName());
                    new WithdrawItemEvent()
                            .setLoadoutItem(loadoutItem)
                            .execute();
                }
            }

            if (loadout.isStrict() && !hasOnlyLoadout()) {
                if (!Bank.isOpen()) {
                    log.info("[WITHDRAW LOADOUT EVENT] - Opening bank to deposit unwanted items");
                    new OpenBankEvent()
                            .execute();
                    return;
                }

                for (Item item : Inventory.getAll()) {
                    if (!getLoadout().getNames().contains(item.getName())) {
                        log.info("[WITHDRAW LOADOUT EVENT] - Banking " + item.getName() + " since not in loadout");
                        Bank.depositAll(item.getName());
                    }
                }

                int amountOfExtraEquipment = 0;
                for (Item item : Equipment.getAll()) {
                    if (!getLoadout().getNames().contains(item.getName())) {
                        amountOfExtraEquipment++;
                    }
                }
                for (Item item : Equipment.getAll()) {
                    if (amountOfExtraEquipment > 0) {
                        if (Inventory.getFreeSlots() < amountOfExtraEquipment) {
                            log.info("[WITHDRAW LOADOUT EVENT] - Depositing inventory to unequip unwanted items");
                            Bank.depositInventory();
                            Time.sleepTicksUntil(() -> !Inventory.isEmpty(), 3);
                            return;
                        }

                        Item equipmentItem = Equipment.getFirst(item.getName());
                        if (equipmentItem == null) {
                            log.info("[WITHDRAW LOADOUT EVENT] - Banking " + item.getName() + " since not in loadout");
                            return;
                        }

                        log.info("[WITHDRAW LOADOUT EVENT] - Unequiping " + item.getName() + " to bank");
                        equipmentItem.interact(0);
                    }
                }
            }
            return;
        }

        log.info("I have all the needed items");
    }

    private boolean hasOnlyLoadout() {
        List<LoadoutItem> loadoutItemList = getLoadout().getLoadoutItemList();
        List<String> loadoutNameList = new ArrayList<>();
        for (LoadoutItem loadoutItem : loadoutItemList) {
            loadoutNameList.add(loadoutItem.getName());
        }

        List<Item> inventoryItemList = Inventory.getAll();
        List<String> inventoryNameList = new ArrayList<>();
        for (Item item : inventoryItemList) {
            inventoryNameList.add(item.getName());
        }

        List<Item> equipmentItemList = Equipment.getAll();
        List<String> equipmentNameList = new ArrayList<>();
        for (Item item : equipmentItemList) {
            equipmentNameList.add(item.getName());
        }

        for (LoadoutItem loadoutItem : loadoutItemList) {
            for (String name : inventoryNameList) {
                if (!loadoutItem.isEquipment() && !loadoutNameList.contains(name)) {
                    return false;
                }
            }

            for (String name : equipmentNameList) {
                if (loadoutItem.isEquipment() && !loadoutNameList.contains(name)) {
                    return false;
                }
            }
        }

        return true;
    }
}
