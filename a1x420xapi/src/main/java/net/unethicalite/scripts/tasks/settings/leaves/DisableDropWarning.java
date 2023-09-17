package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class DisableDropWarning extends Leaf {
    @Override
    public boolean isValid() {
        return shouldDisableDropItemWarning();
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

        Widget dropItemWidget = Widgets.get(134, 19, 23);
        if (dropItemWidget != null && dropItemWidget.isVisible()) {
            log.info("Disabling drop item warning");
            dropItemWidget.interact("Toggle");
            Time.sleepTicksUntil(() -> !shouldDisableDropItemWarning(), 3);
        }

        return -1;
    }

    private boolean warningsSettingsIsOpen() {
        Widget warningsSettingsWidget = Widgets.get(134, 23, 7);
        return warningsSettingsWidget != null && !warningsSettingsWidget.isVisible();
    }

    public static boolean shouldDisableDropItemWarning() {
        return Vars.getBit(5411) != 0;
    }
}
