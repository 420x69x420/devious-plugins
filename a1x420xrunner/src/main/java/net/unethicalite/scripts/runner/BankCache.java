package net.unethicalite.scripts.runner;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.unethicalite.api.items.Bank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BankCache {
    public static List<Item> cache = null;
    public static void update() {
        if (!Bank.isOpen()) {
            return;
        }
        if (cache == null) {
            cache = new ArrayList<>();
        }
        cache.clear();
        int itemCounter = 0;
        for (Item i : Bank.getAll()) {
            if (i == null) {
                log.info("Null item at Item eval #"+ itemCounter);
            }
            if (i.getName() == null) {
                log.info("Item name considered by Java to be null at Item eval #"+itemCounter);
            }
            if (i.getName().equals("null")) {
                log.info("Item name EQUALS null at Item eval #"+itemCounter);
            }
            if (i.getId() < 1) {
                log.info("Item ID less than 1 ("+i.getId()+") at Item eval #"+itemCounter);
            }
            log.debug("Bank item: "+i.getName() + " (qty="+i.getQuantity()+")");
            cache.add(i);
        }
    }

    /**
     * Checks bank cache for initialization or empty contents
     * @return false if empty or not initialized, true if stuff in bank
     */
    public static boolean checkCache() {
        if (cache == null) {
            log.debug("Bank cache null");
            return false;
        }
        log.debug("Bank cache initialized already");
        return true;
    }

    /**
     * Returns true if at least one of EACH id passed is contained within the bank cache.
     */
    public static boolean containsAll(int... ids) {
        if (!checkCache()) {
            return false;
        }
        return Arrays.stream(ids).allMatch((i) -> cache.stream().anyMatch(i2 -> i2.getId() == i));
    }
    /**
     * Returns true if at least one of ANY id passed is contained within the bank cache.
     */
    public static boolean containsAny(int... ids) {
        if (!checkCache()) {
            return false;
        }
        return Arrays.stream(ids).anyMatch((i) -> cache.stream().anyMatch(i2 -> i2.getId() == i));
    }

    /**
     * Returns true if at least one of ANY string passed is contained within the bank cache.
     */
    public static boolean containsAny(String... ids) {
        if (!checkCache()) {
            return false;
        }
        return Arrays.stream(ids).anyMatch((i) -> cache.stream().anyMatch(i2 -> i2.getName().toLowerCase().contains(i.toLowerCase())));
    }
    public static int getCount(int id) {
        if (!containsAny(id)) {
            return 0;
        }
        Optional<Item> tt = cache.stream().filter(c -> c.getId() == id).findFirst();
        if (tt.isPresent()) {
            return tt.get().getQuantity();
        }
        return 0;
    }
}
