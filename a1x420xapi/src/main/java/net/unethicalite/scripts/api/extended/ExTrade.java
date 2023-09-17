package net.unethicalite.scripts.api.extended;

import net.runelite.api.Item;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.widgets.Dialog;

import java.util.function.Predicate;

public class ExTrade {
    public static void remove(Predicate<Item> filter, int quantity) {
        Item item = Trade.getFirst(false, filter);
        if (item == null) {
            return;
        }

        switch (quantity) {
            case 1:
                item.interact("Remove");
                break;
            case 5:
                item.interact("Remove-5");
                break;
            case 10:
                item.interact("Remove-10");
                break;
            default:
                if (quantity >= Inventory.getCount(true, item.getId())) {
                    item.interact("Remove-All");
                } else {
                    item.interact("Remove-X");
                    Dialog.enterAmount(quantity);
                    Time.sleepTick();
                }
                break;
        }
    }

    public static void remove(int id, int quantity) {
        remove(x -> x.getId() == id, quantity);
    }

    public static void remove(String name, int quantity) {
        remove(x -> x.getName() != null && x.getName().equals(name), quantity);
    }
}
