package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class DisableHopWarning extends Leaf {
    @Override
    public boolean isValid() {
        return shouldDisableHopping();
    }

    @Override
    public int execute() {
        if (!warningsSettingsIsOpen()) {
            Widget warningsSettingsWidget = Widgets.get(134, 23, 7);
            if (warningsSettingsWidget == null) {
                log.info("Can't find the widget to open the warnings settings");
                return -1;
            }

            log.info("Opening warnings settings");
            warningsSettingsWidget.interact("Select Warnings");
            Time.sleepTicksUntil(this::warningsSettingsIsOpen, 3);
            return -1;
        }

        Widget worldSwitchWidget = Widgets.get(134, 19, 32);
        if (worldSwitchWidget != null && worldSwitchWidget.isVisible()) {
            log.info("Disabling world switching confirmation");
            worldSwitchWidget.interact("Toggle");
            Time.sleepTicksUntil(() -> !shouldDisableHopping(), 3);
        }

        return -1;
    }

    private boolean warningsSettingsIsOpen() {
        Widget warningsSettingsWidget = Widgets.get(134, 23, 7);
        return warningsSettingsWidget != null && !warningsSettingsWidget.isVisible();
    }

    public static boolean shouldDisableHopping() {
        return Vars.getBit(4100) != 1;
    }
}
