package net.unethicalite.scripts.tasks.general.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.scripts.framework.Leaf;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.sections.*;

@Slf4j
public class Tut extends Leaf {
    public static int mainVarp() {
        return Vars.getVarp(281);
    }
    public static int getSubVarp() {
        return Vars.getVarp(406);
    }

    public static void talkToInstructor(WorldPoint instructorTile, String instructorName) {
        if (Movement.isWalking()) {
            return;
        }
        NPC instructorNpc = NPCs.getNearest(instructorName);
        if (instructorNpc == null || instructorNpc.getName() == null) {
            Movement.walkTo(instructorTile);
        } else if (!Reachable.isInteractable(instructorNpc) || instructorNpc.distanceTo(Players.getLocal()) > 15) {
            Movement.walkTo(instructorNpc.getWorldLocation());
        } else if (!Players.getLocal().isMoving() && Players.getLocal().getInteracting() == null) {
            instructorNpc.interact("Talk-to");
        }
    }

    @Override
    public boolean isValid() {
        return mainVarp() != 1000;
    }

    @Override
    public int execute() {
        if (Dialog.chooseOption("I am brand new! This is my first time here.") ||
                Dialog.chooseOption("Yes.") ||
                Dialog.chooseOption("Yes, send me to the mainland") ||
                Dialog.chooseOption("m ready to move on!")) {
            return -1;
        }
        switch (getSubVarp()) {
            case 0:
            case 1:
                RuneScapeGuideSection.onLoop();
                break;
            case 2:
            case 3:
                SurvivalSection.onLoop();
                break;
            case 4:
            case 5:
                CookingSection.onLoop();
                break;
            case 6:
            case 7:
                QuestSection.onLoop();
                break;
            case 8:
            case 9:
                MiningSection.onLoop();
                break;
            case 10:
            case 11:
            case 12:
                FightingSection.onLoop();
                break;
            case 14:
            case 15:
                BankSection.onLoop();
                break;
            case 16:
            case 17:
                PriestSection.onLoop();
                break;
            case 18:
            case 19:
            case 20:
                WizardSection.onLoop();
                break;
        }
        return -Rand.nextInt(1,3);
    }
}
