package net.unethicalite.scripts.api.loadouts;

import net.runelite.api.Item;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Loadout {
    private List<LoadoutItem> loadoutItemList = new ArrayList<>();
    private boolean strict;

    public Loadout() {
        this.setStrict(true);
    }

    public Loadout addLoadoutItem(LoadoutItem loadoutItem) {
        loadoutItemList.add(loadoutItem);
        return this;
    }

    public List<LoadoutItem> getLoadoutItemList() {
        return loadoutItemList;
    }

    public void setLoadoutItemList(List<LoadoutItem> loadoutItemList) {
        this.loadoutItemList = loadoutItemList;
    }

    public boolean isStrict() {
        return strict;
    }

    public Loadout setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public List<Integer> getIDs() {
        return loadoutItemList.stream()
                .map(LoadoutItem::getId)
                .collect(Collectors.toList());
    }

    public List<String> getNames() {
        return loadoutItemList.stream()
                .map(LoadoutItem::getName)
                .collect(Collectors.toList());
    }

    public boolean hasLoadout() {
        if (loadoutItemList.stream().anyMatch(loadoutItem -> !loadoutItem.hasItem())) {
            return false;
        }

        if (!strict) {
            return true;
        }

        return hasOnlyLoadout();
    }

    private boolean hasOnlyLoadout() {
        List<LoadoutItem> loadoutItemList = getLoadoutItemList();
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
