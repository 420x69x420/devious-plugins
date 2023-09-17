package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.*;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class OpenSettings extends Leaf {
    @Override
    public boolean isValid() {
        return !settingsIsOpen() && (DisableSound.shouldMuteSound() || DisableAid.shouldDisableAid() || DisableHopWarning.shouldDisableHopping() || DisableTradeDelay.shouldDisableTradeDelay() || DisableGEWarnings.shouldDisableGEWarnings() || DisableDropWarning.shouldDisableDropItemWarning());
    }

    @Override
    public int execute() {
        if (GrandExchange.isOpen()) {
            GrandExchange.close();
            return -1;
        }
        if (Bank.isOpen()) {
            Bank.close();
            return -1;
        }
        if (Trade.isOpen()) {
            Trade.decline();
            return -1;
        }
        if (Dialog.canContinue()) {
            Dialog.continueSpace();
            return -1;
        }
        if (Dialog.isOpen() || Production.isOpen()) {
            Movement.walk(Players.getLocal());
            return -1;
        }

        if (!Tabs.isOpen(Tab.OPTIONS)) {
            log.info("Opening options tab");
            Tabs.open(Tab.OPTIONS);
            Time.sleepTicksUntil(() -> Tabs.isOpen(Tab.OPTIONS), 3);
        }

        Widget allSettingsWidget = Widgets.get(116, 32);
        if (allSettingsWidget != null && allSettingsWidget.isVisible()) {
            log.info("Opening settings tab");
            allSettingsWidget.interact("All Settings");
            Time.sleepTicksUntil(this::settingsIsOpen, 3);
        }

        return -1;
    }

    private boolean settingsIsOpen() {
        Widget settingsWidget = Widgets.get(134, 0);
        return settingsWidget != null && settingsWidget.isVisible();
    }
}
