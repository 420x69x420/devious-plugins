package net.unethicalite.plugins.cooker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.widgets.Prayers;

import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class API {
    public static long lastDrinkTick = -2;
    public static long ticks = 0;
    public static int bankedKebabs = 0;

    public static Predicate<Item> energyPotFilter = i ->
            i.getId() == ItemID.ENERGY_POTION4 ||
                    i.getId() == ItemID.ENERGY_POTION2 ||
                    i.getId() == ItemID.ENERGY_POTION3 ||
                    i.getId() == ItemID.ENERGY_POTION1;
    public static void checkDrinkEnergyPot() {
        Item vial = Inventory.getFirst(ItemID.VIAL);
        if (vial != null) {
            vial.interact("Drop");
            API.shortSleep();
        }
        if ((ticks - lastDrinkTick) >= 3 && Movement.getRunEnergy() <= 80) {
            Item energy = Inventory.getFirst(energyPotFilter);
            if (energy != null) {
                energy.interact("Drink");
                API.shortSleep();
                lastDrinkTick = ticks;
            }
        }
    }
    public static void shortSleep() {
        Time.sleep(50,250);
    }
    public static void fastSleep() {
        Time.sleep(25,85);
    }
    public static int returnTick() {
        Time.sleepTick();
        return Rand.nextInt(5,50);
    }
    public static int shortReturn() {
        return Rand.nextInt(50,250);
    }
    public static boolean checkToggleRun() {
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() >= 5) {
            shortSleep();
            Movement.toggleRun();
            return true;
        }
        return false;
    }

    public static boolean waitWalking() {
        checkToggleRun();
        checkDrinkEnergyPot();
        return Movement.isWalking();
    }
    public static int clickLocalBank() {
        return clickBank(BankLocation.getNearest());
    }
    public static int clickBank(BankLocation location) {
        log.info("ClickBank");
        if (waitWalking()) {
            return API.shortReturn();
        }
        TileObject booth = TileObjects.getNearest(b -> b.hasAction("Collect") && (b.hasAction("Bank") || b.hasAction("Use")) && location.getArea().contains(b));
        NPC banker = null;
        if (booth == null) {
            banker = NPCs.getNearest(b -> b.hasAction("Collect") && b.hasAction("Bank")  && location.getArea().contains(b));
        }
        if (booth == null && banker == null) {
            booth = TileObjects.getNearest(b -> b.hasAction("Collect") && b.hasAction("Bank") && Players.getLocal().distanceTo(b) < 20);
        }
        if ((booth == null && banker == null) || booth.distanceTo(Players.getLocal()) > 20 || !Reachable.isInteractable(booth)) {
            log.info("Walking towards bank");
            Movement.walkTo(BankLocation.getNearest());
            return API.shortReturn();
        }
        if (booth != null) {
            log.info("Clicking Booth");
            String action = (booth.hasAction("Bank") ? "Bank" : "Use");
            booth.interact(action);
        } else {
            log.info("Clicking Banker");
            banker.interact("Bank");
        }
        return API.shortReturn();
    }
    public static void depositAllExcept(int... ids) {
        List<Integer> itemsDeposited = new ArrayList<>();
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !Arrays.stream(ids).anyMatch(exceptId -> exceptId == i.getId()))) {
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: "+ i.getName());
            Bank.depositAllExcept(ids);
            API.shortSleep();
        }
    }
    public static void enablePrayer(Prayer prayer) {
        Prayer overhead = getOverhead();
        if (overhead == null || overhead != prayer) {
            log.info("switching prayer from: "+(overhead == null ? "off" : overhead.toString()) + " to " +prayer.toString());
            shortSleep();
            Prayers.toggle(prayer);
        }
    }
    public static Prayer getOverhead() {
        HeadIcon ourIcon = Players.getLocal().getOverheadIcon();
        if (ourIcon != null) {
            switch (ourIcon) {
                case MELEE:
                    return Prayer.PROTECT_FROM_MELEE;
                case MAGIC:
                    return Prayer.PROTECT_FROM_MAGIC;
                case RANGED:
                    return Prayer.PROTECT_FROM_MISSILES;
            }
        }
        return null;
    }

    /**
     * You can only interact with an NPC via the adjacent tiles to the NPC, and this method returns distance to closest tile
     * @param npc
     * @return
     */
    public static int getClosestAttackDistance(NPC npc, Client client) {
        Set<WorldPoint> checkedTiles = new HashSet<>();

        List<WorldPoint> interactableTiles = Reachable.getInteractable(npc);
        WorldArea ourArea = Players.getLocal().getWorldArea();
        WorldPoint ourTile = Players.getLocal().getWorldLocation();
        int shortestDistance = Integer.MAX_VALUE;
        for (WorldPoint tile : interactableTiles) {
            if (!checkedTiles.contains(tile)) {
                if (ourArea.hasLineOfSightTo(client, tile)) {
                    int currentDistance = tile.distanceTo2D(ourTile);
                    if (currentDistance < shortestDistance) {
                        shortestDistance = currentDistance;
                    }
                }
                checkedTiles.add(tile);
            }
        }
        return shortestDistance;
    }
}
