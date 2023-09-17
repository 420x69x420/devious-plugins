package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;
import net.unethicalite.scripts.tasks.general.leaves.Tut;


public class WizardSection {

    private static final String instructor = "Magic Instructor";
    private static final WorldArea chickenCastArea = new WorldArea(3139, 3090, 1,1,0);
    public static final WorldPoint instructorTile = new WorldPoint(3141, 3088, 0);
    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 620:
            case 640:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 630:
                TutorialTab.open(Tab.MAGIC);
                break;
            case 650:
                if (!chickenCastArea.contains(Players.getLocal())) {
                    new MovementEvent()
                            .setDestination(chickenCastArea)
                            .execute();
                } else {
                    attackChicken();
                }
                break;
            case 670:
                if (Dialog.hasOption(o -> o.equalsIgnoreCase("No, I'm not planning to do that.") ||
                        o.equalsIgnoreCase("Yes.") ||
                        o.equalsIgnoreCase("I'm fine, thanks."))) {
                    Dialog.chooseOption("No, I'm not planning to do that.", "Yes.", "I'm fine, thanks.");
                } else {
                    Tut.talkToInstructor(instructorTile, instructor);
                }
                break;
        }
    }
    private static boolean attackChicken() {
        NPC chicken = NPCs.getNearest("Chicken");
        if (chicken != null && chicken.getName() != null) {
            Magic.cast(SpellBook.Standard.WIND_STRIKE, chicken);
            Time.sleepUntil(() -> Tut.mainVarp() != 650, 3000, 600);
            return true;
        }
        return false;
    }
}
