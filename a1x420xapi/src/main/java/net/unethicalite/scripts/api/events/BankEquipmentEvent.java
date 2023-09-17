package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;

@Slf4j
public class BankEquipmentEvent extends AbstractEvent {
    public BankEquipmentEvent() {
        this.setEventCompletedCondition(() -> Equipment.getAll().isEmpty());
        this.setEventTimeoutTicks(200);
    }

    @Override
    public void onLoop() {
        if (!Bank.isOpen()) {
            log.info("[DEPOSIT EQUIPMENT EVENT] - Opening bank");
            new OpenBankEvent()
                    .execute();
            return;
        }

        log.info("[DEPOSIT EQUIPMENT EVENT] - Depositing equipment");
        Bank.depositEquipment();
        Time.sleepTicksUntil(() -> Equipment.getAll().isEmpty(), 3);
    }
}
