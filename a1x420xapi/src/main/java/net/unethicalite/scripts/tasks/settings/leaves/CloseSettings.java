package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class CloseSettings extends Leaf {
    @Override
    public boolean isValid() {
        return settingsIsOpen();
    }

    @Override
    public int execute() {
        Widget closeWidget = Widgets.get(134, 4);
        if (closeWidget == null || !closeWidget.isVisible()) {
            log.info("Can't find the close settings widget");
            return -1;
        }

        log.info("Closing settings");
        closeWidget.interact("Close");
        Time.sleepTicksUntil(() -> !settingsIsOpen(), 3);

        return -1;
    }

    private boolean settingsIsOpen() {
        Widget settingsWidget = Widgets.get(134, 0);
        return settingsWidget != null && settingsWidget.isVisible();
    }
}
