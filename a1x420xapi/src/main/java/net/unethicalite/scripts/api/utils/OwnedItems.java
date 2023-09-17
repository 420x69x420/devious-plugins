package net.unethicalite.scripts.api.utils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;

@Slf4j
public class OwnedItems {
    public static int getCount(int... ids) {
        return (Bank.isOpen() ? Bank.Inventory.getCount(true, ids) : Inventory.getCount(true, ids)) + Equipment.getCount(true, ids) + Bank.getCount(true, ids);
    }

    public static int getCount(String... names) {
        return (Bank.isOpen() ? Bank.Inventory.getCount(names) : Inventory.getCount(names)) + Equipment.getCount(true, names) + Bank.getCount(true, names);
    }

    public static int getCount(boolean stacks, int... ids) {
        return (Bank.isOpen() ? Bank.Inventory.getCount(stacks, ids) : Inventory.getCount(stacks, ids)) + Equipment.getCount(stacks, ids) + Bank.getCount(stacks, ids);
    }

    public static int getCount(boolean stacks, String... names) {

        return (Bank.isOpen() ? Bank.Inventory.getCount(stacks, names) : Inventory.getCount(stacks, names)) + Equipment.getCount(stacks, names) + Bank.getCount(stacks, names);
    }

    public static boolean contains(int... ids) {
        return OwnedItems.getCount(ids) != 0;
    }

    public static boolean contains(String... names) {
        return OwnedItems.getCount(names) != 0;
    }

    public static boolean containsIncludingNoted(int id) {
        return getCountIncludingNoted(id) > 0;
    }

    public static int getCountIncludingNoted(int id) {
        ItemComposition itemComp = Static.getClient().getItemComposition(id);
        if (itemComp == null) {
            log.info("Invalid item id passed to evaluated OwnedItems count of: " + id);
            return 0;
        }
        int unnotedId = getUnnotedID(itemComp);
        int notedId = getNotedID(itemComp);
        int unnotedCount = 0;
        int notedCount = 0;
        int totalCount = 0;
        if (Bank.isOpen()) {
            unnotedCount = Bank.Inventory.getCount(true, unnotedId) + Bank.getCount(true, unnotedId) + Equipment.getCount(true, unnotedId);
            notedCount = Bank.Inventory.getCount(true, notedId);
        } else {
            unnotedCount = Inventory.getCount(true, unnotedId) + Bank.getCount(true, unnotedId) + Equipment.getCount(true, unnotedId);
            notedCount = Inventory.getCount(true, notedId);
        }
        totalCount = unnotedCount + notedCount;
        //log.info("Total owneditem count: " + totalCount +" unnoted: "+ unnotedCount+" noted: "+ notedCount);
        return totalCount;
    }

    public static int getNotedID(ItemComposition itemComposition) {
        if (!isNoted(itemComposition)) {
            return itemComposition.getLinkedNoteId();
        } else {
            return itemComposition.getId();
        }
    }

    public static int getNotedID(int id) {
        ItemComposition itemComposition = Static.getItemManager().getItemComposition(id);
        if (!isNoted(itemComposition)) {
            return itemComposition.getLinkedNoteId();
        } else {
            return itemComposition.getId();
        }
    }

    public static int getUnnotedID(int id) {
        ItemComposition itemComposition = Static.getItemManager().getItemComposition(id);
        if (!isNoted(itemComposition)) {
            return itemComposition.getId();
        } else {
            return itemComposition.getLinkedNoteId();
        }
    }

    public static int getUnnotedID(ItemComposition itemComposition) {
        if (!isNoted(itemComposition)) {
            return itemComposition.getId();
        } else {
            return itemComposition.getLinkedNoteId();
        }
    }

    public static boolean isNoted(ItemComposition i) {
        return i.getNote() == 799;
    }
    public static boolean isNoted(int id) {
        ItemComposition i = Static.getItemManager().getItemComposition(id);
        return i.getNote() == 799;
    }
}

