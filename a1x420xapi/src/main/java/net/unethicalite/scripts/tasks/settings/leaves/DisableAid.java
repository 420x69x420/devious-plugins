package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class DisableAid extends Leaf {
    @Override
    public boolean isValid() {
        return shouldDisableAid();
    }

    @Override
    public int execute() {
        if (!gameplaySettingsIsOpen()) {
            Widget gameplaySettingsWidget = Widgets.get(134, 23, 5);
            if (gameplaySettingsWidget == null) {
                log.info("Can't find the widget to open the gameplay settings");
                return -1;
            }

            log.info("Opening gameplay settings");
            gameplaySettingsWidget.interact("Select Gameplay");
            Time.sleepTicksUntil(this::gameplaySettingsIsOpen, 3);
            return -1;
        }

        Widget acceptAidWidget = Widgets.get(134, 19, 1);
        if (acceptAidWidget != null && acceptAidWidget.isVisible()) {
            log.info("Disabling accept aid");
            acceptAidWidget.interact("Toggle");
            Time.sleepTicksUntil(() -> !shouldDisableAid(), 3);
        }

        return -1;
    }

    private boolean gameplaySettingsIsOpen() {
        Widget gameplaySettingsWidget = Widgets.get(134, 23, 5);
        return gameplaySettingsWidget != null && !gameplaySettingsWidget.isVisible();
    }

    public static boolean shouldDisableAid() {
        return Vars.getBit(4180) != 0;
    }
}
