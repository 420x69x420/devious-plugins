package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;

@Slf4j
public class BankInventoryEvent extends AbstractEvent {
    public BankInventoryEvent() {
        this.setEventCompletedCondition(Inventory::isEmpty);
        this.setEventTimeoutTicks(200);
    }

    @Override
    public void onLoop() {
        if (!Bank.isOpen()) {
            log.info("[DEPOSIT INVENTORY EVENT] - Opening bank");
            new OpenBankEvent()
                    .execute();
            return;
        }

        log.info("[DEPOSIT INVENTORY EVENT] - Depositing inventory");
        Bank.depositInventory();
        Time.sleepTicksUntil(Inventory::isEmpty, 3);
    }
}
