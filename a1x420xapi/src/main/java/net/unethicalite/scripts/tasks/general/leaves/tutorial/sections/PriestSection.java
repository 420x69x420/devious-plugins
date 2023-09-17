package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;


import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.tasks.general.leaves.Tut;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;

public final class PriestSection  {

    static final WorldPoint instructorTile = new WorldPoint(3124, 3107, 0);

    static final String instructor = "Brother Brace";

    public static void onLoop() {

        switch (Tut.mainVarp()) {
            case 550:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 560:
                TutorialTab.open(Tab.PRAYER);
                break;
            case 570:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 580:
                TutorialTab.open(Tab.FRIENDS);
                break;
            case 590:
                //seems to be deprecated LOL no more learning about angry ignore listing
                //TutorialTab.open(Tab.CLAN_CHAT);
                break;
            case 600:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 610:
                new MovementEvent()
                        .setDestination(WizardSection.instructorTile)
                        .setEventCompletedCondition(() -> Tut.mainVarp() != 610 || Players.getLocal().getWorldLocation().equals(WizardSection.instructorTile))
                        .execute();
                break;
        }
    }
}
