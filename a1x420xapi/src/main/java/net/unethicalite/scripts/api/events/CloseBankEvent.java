package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.items.Bank;

@Slf4j
public class CloseBankEvent extends AbstractEvent {
    public CloseBankEvent() {
        this.setEventCompletedCondition(() -> !Bank.isOpen());
        this.setEventTimeoutTicks(3);
    }

    @Override
    public void onLoop() {
        log.info("[CLOSE BANK EVENT] - Closing bank");
        Bank.close();
    }
}
