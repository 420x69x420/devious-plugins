package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class DisableGEWarnings extends Leaf {
    @Override
    public boolean isValid() {
        return shouldDisableGEWarnings();
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

        if (shouldDisableBuyWarning()) {
            Widget buyWarningWidget = Widgets.get(134, 19, 39);
            if (buyWarningWidget != null && buyWarningWidget.isVisible()) {
                log.info("Disabling GE buy warning");
                buyWarningWidget.interact("Toggle");
                Time.sleepTicksUntil(() -> !shouldDisableBuyWarning(), 3);
            }
        }

        if (shouldDisableSellWarning()) {
            Widget sellWarningWidget = Widgets.get(134, 19, 40);
            if (sellWarningWidget != null && sellWarningWidget.isVisible()) {
                log.info("Disabling GE sell warning");
                sellWarningWidget.interact("Toggle");
                Time.sleepTicksUntil(() -> !shouldDisableSellWarning(), 3);
            }
        }

        return -1;
    }

    private boolean warningsSettingsIsOpen() {
        Widget warningsSettingsWidget = Widgets.get(134, 23, 7);
        return warningsSettingsWidget != null && !warningsSettingsWidget.isVisible();
    }

    public static boolean shouldDisableGEWarnings() {
        return shouldDisableBuyWarning() || shouldDisableSellWarning();
    }

    private static boolean shouldDisableBuyWarning() {
        return Vars.getBit(14700) != 1;
    }

    private static boolean shouldDisableSellWarning() {
        return Vars.getBit(14701) != 1;
    }
}
