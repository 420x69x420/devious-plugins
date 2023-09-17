package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;

import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.api.utils.AreaUtils;
import net.unethicalite.scripts.tasks.general.leaves.Tut;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;

import java.util.*;

public final class SurvivalSection {
    private static String instructor = "Survival Expert";
    private static WorldPoint instructorTile = new WorldPoint(3103, 3095, 0);
    private static WorldPoint gateTile = new WorldPoint(3090, 3092, 0);

    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 20:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 30:
                TutorialTab.open(Tab.INVENTORY);
                break;
            case 40:
                fish();
                break;
            case 50:
                TutorialTab.open(Tab.SKILLS);
                break;
            case 60:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 70:
                chopTree();
                break;
            case 80:
            case 90:
            case 100:
            case 110:
                if (!Inventory.contains("Raw shrimps")) {
                    fish();
                } else {
                    Widget lightFire = Widgets.get(162, w -> w.getText().toLowerCase().contains("time to light a fire"));
                    TileObject fire = TileObjects.getFirstSurrounding(Players.getLocal().getWorldLocation(), 5, "Fire");
                    if (lightFire != null || fire == null) {
                        if (!Inventory.contains("Logs")) {
                            chopTree();
                        } else {
                            lightFire();
                        }
                    } else {
                        cook();
                    }
                }
                break;
            case 120:
                TileObject gate = TileObjects.getFirstSurrounding(gateTile, 5, g -> g.getName().equals("Gate") && g.hasAction("Open"));
                if (gate != null && gate.distanceTo(Players.getLocal()) < 15) {
                    gate.interact("Open");
                    Time.sleepUntil(() -> Tut.mainVarp() == 130, () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(), 300, 5000);
                } else {
                    new MovementEvent()
                            .setDestination(gateTile)
                            .setEventCompletedCondition(() -> {
                                TileObject gate1 = TileObjects.getFirstSurrounding(gateTile, 5, g -> g.getName().equals("Gate") && g.hasAction("Open"));
                                return gate1 != null && gateTile.distanceTo(Players.getLocal()) < 15;
                            })
                            .execute();
                }
                break;
        }
    }

    private static void chopTree() {
        TileObject tree = TileObjects.getNearest(g -> g.getName().equals("Tree") && g.hasAction("Chop down"));
        if (tree != null) {
            tree.interact("Chop down");
            Time.sleepUntil(() -> Inventory.contains("Logs") || tree == null || tree.getName() == null, () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(), 300, 5000);
        }
    }

    private static void fish() {
        NPC fishingSpot = NPCs.getNearest("Fishing spot");
        if (fishingSpot != null) {
            long rawShrimpCount = Inventory.getCount("Raw shrimps");
            fishingSpot.interact("Net");
            Time.sleepUntil(() -> Inventory.getCount("Raw shrimps") > rawShrimpCount, () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(), 300, 5000);
        }
    }

    private static void lightFire() {
        if (standingOnFire()) {
            getEmptyPosition().ifPresent(position -> {
                new MovementEvent().setDestination(position).execute();
            });
        } else {
            Item a = Inventory.getFirst("Tinderbox");
            Item b = Inventory.getFirst("Logs");
            List<Item> itemsToUse = new ArrayList<>();
            itemsToUse.add(a);
            itemsToUse.add(b);
            Collections.shuffle(itemsToUse);
            WorldPoint fireInitPoint = Players.getLocal().getWorldLocation();
            itemsToUse.get(0).useOn(itemsToUse.get(1));
            Time.sleepUntil(() -> !fireInitPoint.equals(Players.getLocal().getWorldLocation()), () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(),  300, 5000);
        }
    }

    private static boolean standingOnFire() {
        TileObject fire = TileObjects.getNearest("Fire");
        return fire != null && fire.getWorldLocation().equals(Players.getLocal().getWorldLocation());
    }

    private static Optional<WorldPoint> getEmptyPosition() {
        Set<WorldPoint> allPositions = new HashSet<>();
        AreaUtils.generateSquareViaWorldPoints(allPositions, Players.getLocal().getWorldLocation(), 6);
        // Remove any position with an object (except ground decorations, as they can be walked on)
        for (TileObject object : TileObjects.getAll("Fire")) {
            allPositions.removeIf(position -> object.getWorldLocation().equals(position));
        }
        allPositions.removeIf(p -> !Reachable.isWalkable(p));

        return allPositions.stream().min(Comparator.comparingInt(p -> Players.getLocal().distanceTo(p)));
    }

    private static void cook() {
        Item skrimps = Inventory.getFirst("Raw shrimps");
        TileObject fire = TileObjects.getNearest("Fire");
        if (skrimps != null) {
            long rawShrimpCount = Inventory.getCount("Raw shrimps");
            skrimps.useOn(fire);
            Time.sleepUntil(() -> Inventory.getCount("Raw shrimps") < rawShrimpCount, () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(), 300, 5000);
        }
    }
}
