package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;

import net.runelite.api.Actor;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.tasks.general.leaves.Tut;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;

public final class FightingSection {

    private static final WorldPoint LADDER_TILE = new WorldPoint(3111, 9526,0);
    private static final WorldPoint RAT_CAGE_GATES = new WorldPoint(3110, 9519, 0);
    public static final WorldPoint instructorTile = new WorldPoint(3105, 9508, 0);

    public static String instructor = "Combat Instructor";


    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 370:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 390:
                TutorialTab.open(Tab.EQUIPMENT);
                break;
            case 400:
                Widget w = Widgets.get(WidgetInfo.EQUIPMENT_STATS);
                if (w != null && w.isVisible()) {
                    w.interact("View equipment stats");
                    Time.sleepUntil(() -> Tut.mainVarp() != 400, 69, 3000);
                }
                break;
            case 405:
                wieldItem("Bronze dagger");
                break;
            case 410:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 420:
                if (!Equipment.contains("Bronze sword")) {
                    wieldItem("Bronze sword");
                } else if (!Equipment.contains("Wooden shield")) {
                    wieldItem("Wooden shield");
                }
                break;
            case 430:
                TutorialTab.open(Tab.COMBAT);
                break;
            case 440:
                openRatCage();
                break;
            case 450:
            case 460:
                if (!inRatCage()) {
                    openRatCage();
                } else if (!isAttackingRat()) {
                    attackRat();
                }
                break;
            case 470:
                if (inRatCage()) {
                    openRatCage();
                } else {
                    Tut.talkToInstructor(instructorTile, instructor);
                }
                break;
            case 480:
            case 490:
                if (!Equipment.contains("Shortbow")) {
                    wieldItem("Shortbow");
                } else if (!Equipment.contains("Bronze arrow")) {
                    wieldItem("Bronze arrow");
                } else if (!isAttackingRat()) {
                    attackRat();
                }
                break;
            case 500:
                if (LADDER_TILE.distanceTo(Players.getLocal()) < 15) {
                    TileObject ladder = TileObjects.getFirstSurrounding(LADDER_TILE, 4, "Ladder");
                    if (ladder != null && Reachable.isInteractable(ladder)) {
                        ladder.interact("Climb-up");
                        Time.sleepUntil(() -> LADDER_TILE.distanceTo(Players.getLocal()) > 15, () -> Players.getLocal().isMoving(),300, 5000);
                        break;
                    }
                }
                new MovementEvent()
                        .setDestination(LADDER_TILE)
                        .setEventCompletedCondition(() -> {
                            TileObject ladder = TileObjects.getFirstSurrounding(LADDER_TILE, 4, "Ladder");
                            return ladder != null && Reachable.isInteractable(ladder) && LADDER_TILE.distanceTo(Players.getLocal()) < 15;
                        })
                        .execute();
                break;
        }
    }

    private static boolean inRatCage() {
        return !Reachable.isInteractable(NPCs.getNearest("Combat Instructor"));
    }

    private static void openRatCage() {
        TileObject ratCageGate = TileObjects.getFirstSurrounding(RAT_CAGE_GATES, 3, "Gate");
        if (ratCageGate == null || ratCageGate.distanceTo(Players.getLocal()) > 15) {
            new MovementEvent()
                    .setDestination(LADDER_TILE)
                    .setEventCompletedCondition(() -> {
                        TileObject ratCageGate2 = TileObjects.getFirstSurrounding(RAT_CAGE_GATES, 3, "Gate");
                        return ratCageGate2 != null && Reachable.isInteractable(ratCageGate2) && RAT_CAGE_GATES.distanceTo(Players.getLocal()) < 15;
                    })
                    .execute();
        } else if (!Movement.isWalking()){
            ratCageGate.interact("Open");
        }
    }

    private static boolean isAttackingRat() {
        Actor currentTarget = Players.getLocal().getInteracting();
        return currentTarget != null && currentTarget instanceof NPC && currentTarget.getName().equals("Giant rat");
    }

    private static void attackRat() {
        NPC giantRat = Combat.getAttackableNPC("Giant rat");
        if (giantRat != null) {
            giantRat.interact("Attack");
            Time.sleepUntil(() -> isAttackingRat(), 300, 5000);
        }
    }

    private static void wieldItem(String name) {
        Item toWield = Inventory.getFirst(name);
        String action = (toWield.hasAction("Wield") ? "Wield" : "Equip");
        if (toWield != null) {
            toWield.interact(action);
            Time.sleepUntil(() -> Equipment.contains(name), 3000);
        }
    }
}
