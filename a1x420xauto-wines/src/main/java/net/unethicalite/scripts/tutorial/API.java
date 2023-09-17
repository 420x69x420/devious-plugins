package net.unethicalite.scripts.tutorial;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.*;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.api.widgets.Widgets;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class API {
    @Inject
    private ClientThread clientThread;
    public static boolean stopScript = false;
    public static int bankedKebabs = 0;
    public static int tutProg = 0;
    public static int tutSubProg = 0;
    public static int smithingMakeAllVarplayer = 0;
    public static int smithingAutoMakeItemVarbit = 0;
    public static int smithingAutoMakeMetalVarbit = 0;
    public static int doricsQuestCompletionVarplayer = 0;
    public static int wizardDudeTinderboxUnlockVarplayer = 0;
    public static int nameVarbitValue = 0;
    public static int worldSwitcherWarningVarbit = 0;
    public static int tradeAcceptDelayVarbit = 0;
    public static String taskInfo = "";
    public static String lastDialog = "";

    public static int questSubTabVisibleVarbit = 0;
    public static final Set<WorldPoint> walkTile = new HashSet<>();
    private static int walkingRandomPoint = 6;
    public static boolean useItemsInsteadOfClickingDamnThing = false;
    public static boolean randToggleRun = false;
    public static int gametimeHours = 0;
    public static String timeUntil(Instant targetInstant) {
        if (targetInstant == null) {
            return "None";
        }
        Instant now = Instant.now();
        Duration duration = Duration.between(now, targetInstant);

        // If the duration is negative (i.e., targetInstant is in the past),
        // then the result should be prefixed with a negative sign.
        String sign = "";
        if (duration.isNegative()) {
            sign = "-";
            duration = duration.abs();
        }

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%s%dm, %ds", sign, minutes, seconds);
    }

    public static double getPythagoreanDistance(WorldPoint a, WorldPoint b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        double squared = (dx * dx) + (dy * dy);
        double rooted = Math.sqrt(squared);
        return rooted;
    }
    public static boolean invyContainsAll(int... ids) {
        for (int i : ids) {
            if (Bank.isOpen()) {
                if (Bank.Inventory.getCount(i) == 0) {
                    return false;
                }
            } else if (!Inventory.contains(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pass hex string representing color and returns int value of Color.RGB
     * @param hex
     * @return
     */
    public static int hexToRGBPackedInt(String hex) {
        if (!hex.contains("#")) {
            hex = "#"+hex;
        }
        Color color = Color.decode(hex);
        return color.getRGB();
    }
    public static boolean invyOnlyContains(String... names) {

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
    public static boolean invyOnlyContains(int... ids) {

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
    public static boolean shouldWalk() {
        WorldPoint walkTileCurrent = Movement.getDestination();
        if (walkTileCurrent == null) {
            return true;
        }
        if (walkTileCurrent.distanceTo(Players.getLocal().getWorldLocation()) <= walkingRandomPoint) {
            return true;
        }
        if (!Movement.isWalking()) {
            return true;
        }
        return false;
    }
    /**
     * sleep-handling walk to global map that resets randomized shouldWalk variable
     * @param p
     * @return
     */
    public static int walkTo(WorldPoint p) {
        if (!API.shouldWalk()) {
            return API.shortReturn();
        }
        API.fastSleep();
        Movement.walkTo(p);
        walkingRandomPoint = Rand.nextInt(0, 6);
        Time.sleepTick();
        return API.shortReturn();
    }
    /**
     * sleep-handling walk to global map that resets randomized shouldWalk variable
     * @param l
     * @return
     */
    public static int walkTo(Locatable l) {
        if (!API.shouldWalk()) {
            return API.shortReturn();
        }
        API.fastSleep();
        Movement.walkTo(l);
        walkingRandomPoint = Rand.nextInt(0, 6);
        Time.sleepTick();
        return API.shortReturn();
    }
    /**
     * sleep-handling walk to global map that resets randomized shouldWalk variable
     * @param a
     * @return time to sleep after walk
     */
    public static int walkTo(WorldArea a) {
        if (!API.shouldWalk()) {
            return API.shortReturn();
        }
        API.fastSleep();
        Movement.walkTo(a.getRandom());
        walkingRandomPoint = Rand.nextInt(0, 6);
        Time.sleepTick();
        return API.shortReturn();
    }
    public static WorldArea generateSquare(WorldPoint center, int radius) {
        WorldPoint searchAreaCenter = new WorldPoint(center.getX() - radius, center.getY() - radius, center.getPlane());
        int sideLengths = (radius * 2) + 1;
        return searchAreaCenter.createWorldArea(sideLengths, sideLengths);
    }

    public static void generateSquare(Set<WorldPoint> areaToModify, WorldPoint center, int radius) {
        areaToModify.clear();
        int x = center.getX();
        int y = center.getY();
        int plane = center.getPlane();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                WorldPoint point = new WorldPoint(x + dx, y + dy, plane);
                if (Reachable.isWalkable(point)) {
                    areaToModify.add(point);
                }
            }
        }
    }


    public static Predicate<Item> energyPotFilter = i ->
            i.getId() == ItemID.ENERGY_POTION4 ||
                    i.getId() == ItemID.ENERGY_POTION2 ||
                    i.getId() == ItemID.ENERGY_POTION3 ||
                    i.getId() == ItemID.ENERGY_POTION1;
    public static void shortSleep() {
        Time.sleep(150,325);
    }
    public static void fastSleep() {
        sleepClientTick();
        Time.sleep(25,55);
    }
    public static int fastReturn() {
        sleepClientTick();
        return Rand.nextInt(25,55);
    }
    public static World getOtherRandomSimilarWorld(World oldWorld) {
        return Worlds.getRandom(w -> oldWorld.getId() != w.getId() &&
                oldWorld.isMembers() == !w.isMembers() &&
                oldWorld.isNormal() == w.isNormal() &&
                oldWorld.isAllPkWorld() == w.isAllPkWorld() &&
                oldWorld.isLeague() == w.isLeague() &&
                oldWorld.isSkillTotal() == w.isSkillTotal() &&
                oldWorld.getActivity().toLowerCase().contains("fresh start") == w.getActivity().toLowerCase().contains("fresh start") &&
                oldWorld.getActivity().toLowerCase().contains("beta") == w.getActivity().toLowerCase().contains("beta") &&
                oldWorld.isQuestSpeedRunning() == w.isQuestSpeedRunning() &&
                oldWorld.isTournament() == w.isTournament());
    }
    public static World getRandomWorld(boolean f2p) {
        return Worlds.getRandom(w ->
                f2p != w.isMembers() &&
                w.isNormal() &&
                !w.isAllPkWorld() &&
                !w.isLeague() &&
                !w.isSkillTotal() &&
                !w.getActivity().toLowerCase().contains("fresh start") &&
                !w.getActivity().toLowerCase().contains("beta") &&
                !w.isQuestSpeedRunning() &&
                !w.isTournament());
    }
    public static boolean waitClientTick = false;
    public static void sleepClientTick() {
        waitClientTick = true;
        Time.sleepUntil(() -> !waitClientTick,10, 600);
    }
    public static int returnTick() {
        Time.sleepTick();
        return fastReturn();
    }
    public static int shortReturn() {
        return Rand.nextInt(200,325);
    }
    public static boolean checkToggleRun() {
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() >= 5) {
            shortSleep();
            Movement.toggleRun();
            return true;
        }
        return false;
    }


    public static BankLocation[] f2pBanks = {
            BankLocation.AL_KHARID_BANK,
            BankLocation.DRAYNOR_BANK,
            BankLocation.LUMBRIDGE_BANK,
            BankLocation.EDGEVILLE_BANK,
            BankLocation.FALADOR_EAST_BANK,
            BankLocation.FALADOR_WEST_BANK,
            BankLocation.VARROCK_WEST_BANK,
            BankLocation.VARROCK_EAST_BANK,
            BankLocation.GRAND_EXCHANGE_BANK,
            BankLocation.DUEL_ARENA_BANK,
            BankLocation.FEROX_ENCLAVE_BANK
    };

    public static boolean closeCommonInterfaces() {
        if (Bank.isOpen()) {
            API.shortSleep();
            Bank.close();
            return false;
        }
        if (GrandExchange.isOpen()) {
            API.shortSleep();
            GrandExchange.close();
        }
        if (Dialog.isViewingOptions()) {
            API.shortSleep();
            Movement.walk(Players.getLocal());
            Time.sleepTick();
            return false;
        }
        if (Dialog.isEnterInputOpen()) {
            API.shortSleep();
            Keyboard.type("1",true);
            return false;
        }
        if (Trade.isOpen()) {
            API.shortSleep();
            Trade.accept();
            return false;
        }
        return true;
    }

    public static void enablePrayer(boolean cursed, Prayer prayerIcon) {
        Prayer overhead = getOverhead();
        if (cursed) {
            if (prayerIcon.equals(Prayer.PROTECT_FROM_MAGIC) && (overhead == null || overhead != Prayer.PROTECT_FROM_MISSILES)) {
                log.info("[CURSED] switching prayer from: "+(overhead == null ? "off" : overhead.toString()) + " to " + prayerIcon);
                Prayers.toggle(prayerIcon);
                fastSleep();
            } else if (prayerIcon.equals(Prayer.PROTECT_FROM_MELEE) && (overhead == null || overhead != Prayer.PROTECT_FROM_MAGIC)) {
                log.info("[CURSED] switching prayer from: "+(overhead == null ? "off" : overhead.toString()) + " to " + prayerIcon);
                Prayers.toggle(prayerIcon);
                fastSleep();
            }  else if (prayerIcon.equals(Prayer.PROTECT_FROM_MISSILES) && (overhead == null || overhead != Prayer.PROTECT_FROM_MELEE)) {
                log.info("[CURSED] switching prayer from: "+(overhead == null ? "off" : overhead.toString()) + " to " + prayerIcon);
                Prayers.toggle(prayerIcon);
                fastSleep();
            }
        } else if (overhead == null || overhead != prayerIcon) {
            log.info("switching prayer from: "+(overhead == null ? "off" : overhead.toString()) + " to " +prayerIcon);
            Prayers.toggle(prayerIcon);
            fastSleep();
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
    public static int getClosestLocalAttackDistance(NPC npc, Client client) {
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

    public static void pressEsc() {
        log.info("Pressed esc");
        Keyboard.pressed(KeyEvent.VK_ESCAPE);
        /* (is only sent if a valid Unicode character could be generated.)
        log.info("Typed esc");
        Keyboard.typed(KeyEvent.VK_ESCAPE); */
        log.info("Released esc");
        Keyboard.released(KeyEvent.VK_ESCAPE);
    }
    public static Widget getSmithingWidget() {
        return Widgets.get(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER);
    }
    public static boolean isSmithingOpen() {
        Widget w = getSmithingWidget();
        return w != null && w.isVisible();
    }
    public static void pressSpecialKey(int keyEvent) {
        log.debug("Pressed keyEvent: " + keyEvent);
        Keyboard.pressed(keyEvent);
        log.debug("Release keyEvent: " + keyEvent);
        Keyboard.released(keyEvent);
    }
    public static String convertToRSUnits(int number) {
        String postfix = "";
        int divisor = 1;

        if (number >= 1_000_000) {
            divisor = 1_000_000;
            postfix = "M";
        } else if (number >= 1_000) {
            divisor = 1_000;
            postfix = "K";
        }

        int formattedNumber = (int) Math.ceil((double) number / divisor);

        // Using the NumberFormat class to add commas
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(formattedNumber) + postfix;
    }
    public static int getClosestAttackDistance(NPC npc, WorldPoint tileToEval, Client client) {
        Set<WorldPoint> checkedTiles = new HashSet<>();

        List<WorldPoint> interactableTiles = Reachable.getInteractable(npc);
        WorldArea ourArea = new WorldArea(tileToEval.getX(), tileToEval.getY(), 1, 1, 1);
        int shortestDistance = Integer.MAX_VALUE;
        for (WorldPoint tile : interactableTiles) {
            if (!checkedTiles.contains(tile)) {
                if (ourArea.hasLineOfSightTo(client, tile)) {
                    int currentDistance = tile.distanceTo2D(tileToEval);
                    if (currentDistance < shortestDistance) {
                        shortestDistance = currentDistance;
                    }
                }
                checkedTiles.add(tile);
            }
        }
        return shortestDistance;
    }


    public int getAmmoCount() {
        Item ammo = Equipment.fromSlot(EquipmentInventorySlot.AMMO);
        if (ammo == null || ammo.getName() == null) {
            return 0;
        }
        return ammo.getQuantity();
    }

    public static void equipItem(int itemID) {
        if (Bank.Inventory.getCount(true, itemID) <= 0) {
            log.debug("Missing itemID from invy in queue, skipping: "+itemID);
            return;
        }
        //Withdraw by directly interacting for any clickable-action quantities and return immediately
        Item invyItem = Bank.Inventory.getFirst(itemID);
        int foundActionIndex = -1;
        int currentIndex = 0;
        for (String action : invyItem.getActions()) {
            log.debug("FOUND DAMN ACTION: "+ action+" WITH DAMN INDEX:" + currentIndex);
            if (currentIndex == 0 || action == null || action.equalsIgnoreCase("null")) {
                currentIndex++;
                continue;
            }
            if (action.equals("Wield") || action.equals("Equip") || action.equals("Wear")) {
                foundActionIndex = currentIndex;
                break;
            }
            currentIndex++;
        }
        if (foundActionIndex > 0) {
            log.debug("Interacting action index "+foundActionIndex+" correlating to action: "+invyItem.getActions()[foundActionIndex]+" on item: "+invyItem.getName());
            invyItem.interact(foundActionIndex);
            sleepClientTick();
            return;
        }
        log.debug("Not found any actionable action to equip item: "+ invyItem.getName());
    }
}