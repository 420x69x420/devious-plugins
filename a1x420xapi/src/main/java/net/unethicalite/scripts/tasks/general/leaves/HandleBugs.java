package net.unethicalite.scripts.tasks.general.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.Leaf;
@Slf4j
public class HandleBugs extends Leaf {
    @Override
    public boolean isValid() {
        return shouldHandleBugs();
    }

    @Override
    public int execute() {
        if (Dialog.isEnterInputOpen()) {
            log.info("[HANDLING BUG] - Closing input screen");
            String random = String.valueOf(Rand.nextInt(0, 9));
            Keyboard.type(random, true);
            return -1;
        }

        if (shouldCloseBankTutorial()) {
            log.info("[HANDLING BUG] - Closing bank tutorial");
            Widget bankTutorialWidget = Widgets.get(664, 29, 0);
            if (bankTutorialWidget == null) {
                log.info("Can't find close bank tutorial widget");
                return -1;
            }

            bankTutorialWidget.interact("Close");
            return -1;
        }

        if (shouldCloseBankSpace()) {
            log.info("[HANDLING BUG] - Closing bank space warning");
            Widget bankSpaceWidget = Widgets.get(289, 7);
            bankSpaceWidget.interact("Not now");
            return -1;
        }

        if (Bank.isOpen()) {
            log.info("[HANDLING BUG] - Closing bank");
            Bank.close();
            return -1;
        }

        if (GrandExchange.isOpen()) {
            log.info("[HANDLING BUG] - Closing Grand Exchange");
            GrandExchange.close();
            return -1;
        }

        if (Dialog.isOpen() && Dialog.canContinue()) {
            log.info("[HANDLING BUG] - Continuing dialog");
            Dialog.continueSpace();
            return -1;
        }

        WorldPoint position = Players.getLocal().getWorldLocation();
        Movement.walkTo(position);

        InterfaceInstance.pluginInterface.handledBug();

        return -1;
    }

    private boolean shouldHandleBugs() {
        return Dialog.isEnterInputOpen() ||
                shouldCloseBankSpace() ||
                shouldCloseBankTutorial() ||
                InterfaceInstance.pluginInterface.shouldHandleBugs();
    }


    private boolean shouldCloseBankTutorial() {
        Widget bankTutorialWidget = Widgets.get(664, 29, 0);
        return bankTutorialWidget != null && bankTutorialWidget.isVisible();
    }

    private boolean shouldCloseBankSpace() {
        Widget bankSpaceWidget = Widgets.get(289, 7);
        return bankSpaceWidget != null && bankSpaceWidget.isVisible();
    }
}
