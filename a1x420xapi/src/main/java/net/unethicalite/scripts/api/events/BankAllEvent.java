package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.scripts.api.utils.Sleep;

@Slf4j
public class BankAllEvent extends AbstractEvent {
    public BankAllEvent() {
        this.setEventCompletedCondition(() -> Inventory.isEmpty() && Equipment.getAll().isEmpty());
        this.setEventTimeoutTicks(200);
    }

    @Override
    public void onLoop() {
        if (!Bank.isOpen()) {
            log.info("[DEPOSIT ALL EVENT] - Opening bank");
            new OpenBankEvent()
                    .execute();
            return;
        }

        if (!Inventory.isEmpty()) {
            Sleep.shortSleep();
            log.info("[DEPOSIT ALL EVENT] - Depositing inventory");
            Bank.depositInventory();
            Time.sleepTicksUntil(Inventory::isEmpty, 3);
        }

        if (!Equipment.getAll().isEmpty()) {
            Sleep.shortSleep();
            log.info("[DEPOSIT ALL EVENT] - Depositing equipment");
            Bank.depositEquipment();
            Time.sleepTicksUntil(() -> Equipment.getAll().isEmpty(), 3);
        }
    }
}
