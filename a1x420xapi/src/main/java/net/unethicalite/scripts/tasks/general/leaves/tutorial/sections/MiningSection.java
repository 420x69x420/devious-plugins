package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;

import net.runelite.api.Item;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.tasks.general.leaves.UniqueActions;
import net.unethicalite.scripts.tasks.general.leaves.Tut;


public final class MiningSection {
    private static final WorldPoint WEST_TIN_ROCKS = new WorldPoint(3077, 9502, 0);

    private static final WorldPoint EAST_COPPER_ROCKS = new WorldPoint(3083, 9501, 0);
    private static final WorldPoint ANVIL_TILE = new WorldPoint(3083, 9498, 0);
    private static final WorldPoint instructorTile = new WorldPoint(3082, 9504, 0);
    private static final String instructor = "Mining Instructor";

    public static void onLoop()  {
        switch (Tut.mainVarp()) {
            case 260:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 270:
                mine(true); //tin = true
                break;
            case 280:
                mine(false);
                break;
            case 290:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 300:
                mine(true);
                break;
            case 310:
                mine(false);
                break;
            case 320:
                smelt();
                break;
            case 330:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 340:
            case 350:
                Widget daggerWidget = getDaggerWidget();
                if (daggerWidget != null && daggerWidget.isVisible()) {
                    daggerWidget.interact("Smith");
                    Time.sleepUntil(() -> Inventory.contains("Bronze dagger"), () -> Players.getLocal().isAnimating(), 300, 6000);
                } else {
                    smith();
                }
                break;
            case 360:
                Tut.talkToInstructor(FightingSection.instructorTile,FightingSection.instructor);
                break;
        }
    }

    private static void smith() {
        TileObject anvil = TileObjects.getNearest("Anvil");

        if (anvil != null) {
            if (UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_3)) {
                Item bar = Inventory.getFirst("Bronze bar");
                bar.useOn(anvil);
            } else {
                anvil.interact("Smith");
            }
            Time.sleepUntil(() -> {
                Widget d = getDaggerWidget();
                return d != null && d.isVisible();
            }, 300, 6000);
        }
    }

    private static Widget getDaggerWidget() {
        return Widgets.get(312, w -> w.getName().contains("Bronze dagger"));
    }

    private static void smelt() {
        TileObject furnace = TileObjects.getNearest("Furnace");

        if (furnace != null) {
            if (UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_4)) {
                Item ore = Inventory.getFirst("Tin ore");
                ore.useOn(furnace);
            } else {
                furnace.interact("Use");
            }
            Time.sleepUntil(() -> Inventory.contains("Bronze bar"), 300, 6000);
        }
    }

    private static void mine(boolean tin) {
        TileObject rock = TileObjects.getNearest((tin ? WEST_TIN_ROCKS : EAST_COPPER_ROCKS), g -> g.hasAction("Mine"));
        if (rock != null) {
            int slots = Inventory.getFreeSlots();
            rock.interact("Mine");
            Time.sleepUntil(() -> Inventory.getFreeSlots() > slots, () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(), 300, 6000);
        }
    }
}
