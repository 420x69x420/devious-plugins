package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;

import net.runelite.api.Item;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.scripts.tasks.general.leaves.UniqueActions;
import net.unethicalite.scripts.tasks.general.leaves.Tut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CookingSection {
    private static final WorldPoint instructorTile = new WorldPoint(3074, 3085, 0);
    private static final String instructor = "Master Chef";
    private static final WorldPoint exitDoor = new WorldPoint(3071, 3090, 0);
    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 130:
            case 140:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 150:
                makeDough();
                break;
            case 160:
                bakeDough();
                break;
            case 170:
                TileObject door = TileObjects.getFirstSurrounding(exitDoor, 3, g -> g.getName().equals("Door") && g.hasAction("Open"));
                if (door != null) {
                    door.interact("Open");
                    Time.sleepUntil(() -> Tut.mainVarp() != 170, 300, 5000);
                }
                break;
        }
    }

    private static void makeDough() {
        Item flour = Inventory.getFirst("Pot of flour");
        Item water = Inventory.getFirst("Bucket of water");
        List<Item> itemsToUse = new ArrayList<>();
        itemsToUse.add(flour);
        itemsToUse.add(water);
        Collections.shuffle(itemsToUse);
        itemsToUse.get(0).useOn(itemsToUse.get(1));
        Time.sleepUntil(() -> Inventory.contains("Bread dough"),  300, 5000);
    }

    private static void bakeDough() {
        TileObject range = TileObjects.getNearest("Range");

        if (range != null) {
            if (UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_5)) {
                Item bread = Inventory.getFirst("Bread dough");
                bread.useOn(range);
            } else {
                range.interact("Cook");
            }
        }
    }
}
