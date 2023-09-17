package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;


import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.scripts.tasks.general.leaves.UniqueActions;
import net.unethicalite.scripts.tasks.general.leaves.Tut;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;

public final class QuestSection {
    private static final WorldPoint instructorTile = new WorldPoint(3086, 3122, 0);

    private static final String instructor = "Quest Guide";
    private static boolean toggledRun = false;

    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 200:
                if (UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_1) && !toggledRun) {
                    Movement.toggleRun();
                    toggledRun = true;
                    break;
                }
            case 210:
            case 220:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 230:
                TutorialTab.open(Tab.QUESTS);
                break;
            case 240:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 250:
                TileObject ladder = TileObjects.getNearest(g -> g.getName().equals("Ladder") && g.hasAction("Climb-down"));
                if (Reachable.isInteractable(ladder)) {
                    ladder.interact("Climb-down");
                    Time.sleepUntil(() -> Tut.mainVarp() != 250, 300, 6000);
                } else if (!Movement.isWalking()) {
                    Movement.walkTo(ladder.getWorldArea());
                }
                break;
        }
    }
}
