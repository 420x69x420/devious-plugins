package net.unethicalite.scripts.api.extended;

import net.runelite.api.Item;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExInvy {
    public static boolean containsOnly(String... names) {

        List<Item> allItems = (Bank.isOpen() ? Bank.Inventory.getAll() : Inventory.getAll());
        List<String> approvedNames = new ArrayList<>();
        for (String name : names) {
            approvedNames.add(name.toLowerCase(Locale.ROOT));
        }
        for (Item i : allItems) {
            if (i == null || i.getName() == null) {
                continue;
            }
            if (!approvedNames.stream().anyMatch(aprName -> i.getName().toLowerCase(Locale.ROOT).contains(aprName))) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsOnly(List<Integer> ids) {

        List<Item> allItems = (Bank.isOpen() ? Bank.Inventory.getAll() : Inventory.getAll());
        for (Item i : allItems) {
            if (i == null || i.getName() == null) {
                continue;
            }
            if (!ids.stream().anyMatch(aprID -> aprID == i.getId())) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsOnly(int... ids) {

        List<Item> allItems = (Bank.isOpen() ? Bank.Inventory.getAll() : Inventory.getAll());
        List<Integer> approvedIDs = new ArrayList<>();
        for (int id : ids) {
            approvedIDs.add(id);
        }
        for (Item i : allItems) {
            if (i == null || i.getName() == null) {
                continue;
            }
            if (!approvedIDs.stream().anyMatch(aprID -> aprID == i.getId())) {
                return false;
            }
        }
        return true;
    }
}
