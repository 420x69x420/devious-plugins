package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class DisableTradeDelay extends Leaf {
    @Override
    public boolean isValid() {
        return shouldDisableTradeDelay();
    }

    @Override
    public int execute() {
        if (!interfacesSettingsIsOpen()) {
            Widget interfacesSettingsWidget = Widgets.get(134, 23, 6);
            if (interfacesSettingsWidget == null) {
                log.info("Can't find the widget to open the interfaces settings");
                return -1;
            }

            log.info("Opening interfaces settings");
            interfacesSettingsWidget.interact("Select Interfaces");
            Time.sleepTicksUntil(this::interfacesSettingsIsOpen, 3);
            return -1;
        }

        Widget tradeDelayWidget = Widgets.get(134, 19, 19);
        if (tradeDelayWidget != null && tradeDelayWidget.isVisible()) {
            log.info("Disabling trade delay");
            tradeDelayWidget.interact("Toggle");
            Time.sleepTicksUntil(() -> !shouldDisableTradeDelay(), 3);
        }

        return -1;
    }

    private boolean interfacesSettingsIsOpen() {
        Widget interfacesSettingsWidget = Widgets.get(134, 23, 6);
        return interfacesSettingsWidget != null && !interfacesSettingsWidget.isVisible();
    }

    public static boolean shouldDisableTradeDelay() {
        return Vars.getBit(13130) != 1;
    }
}
