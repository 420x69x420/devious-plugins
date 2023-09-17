package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.scripts.api.utils.Sleep;

@Slf4j
public class OpenGEEvent extends AbstractEvent {
    private boolean useROW = true;

    public OpenGEEvent() {
        this.setEventCompletedCondition(GrandExchange::isOpen);
        this.setEventTimeoutTicks(200);
    }

    public OpenGEEvent useROW(boolean useROW) {
        this.useROW = useROW;
        return this;
    }

    public boolean isUseROW() {
        return useROW;
    }

    public void setUseROW(boolean useROW) {
        this.useROW = useROW;
    }

    @Override
    public void onLoop() {
        Sleep.shortSleep();
        BankLocation bankLocation = BankLocation.GRAND_EXCHANGE_BANK;
        if (!bankLocation.getArea().contains(Players.getLocal())) {
            if (isUseROW() && !Inventory.contains(x -> x.getName().contains("Ring of wealth("))) {
                if (!Bank.isOpen()) {
                    log.info("[OPEN GE EVENT] - Opening bank to withdraw ring of wealth");
                    new OpenBankEvent()
                            .execute();
                    return;
                }

                if (Inventory.isFull()) {
                    log.info("[OPEN GE EVENT] - Depositing inventory to withdraw ring of wealth");
                    Bank.depositInventory();
                    Time.sleepTicksUntil(() -> !Inventory.isFull(), 3);
                    return;
                }

                if (Bank.contains(x -> x.getName().contains("Ring of wealth"))) {
                    log.info("[OPEN GE EVENT] - Withdrawing ring of wealth");
                    Bank.withdraw(x -> x.getName().contains("Ring of wealth"), 1, Bank.WithdrawMode.ITEM);
                    Time.sleepTicksUntil(() -> Inventory.contains(x -> x.getName().contains("Ring of wealth")), 3);
                    return;
                }

                log.info("[OPEN GE EVENT] - Can't find ring of wealth, so walking it is");
                setUseROW(false);
                return;
            }

            log.info("[OPEN GE EVENT] - Walking to Grand Exchange");
            new MovementEvent()
                    .setDestination(bankLocation)
                    .execute();
            return;
        }

        if (!GrandExchange.isOpen()) {
            log.info("[OPEN GE EVENT] - Opening Grand Exchange");
            GrandExchange.open();
            Time.sleepTicksUntil(GrandExchange::isOpen, 10);
        }
    }
    public static void openGENpc() {
        NPC exchanger = NPCs.getNearest(x -> x.hasAction(new String[]{"Exchange"}) && BankLocation.GRAND_EXCHANGE_BANK.getArea().contains(x));
        if (exchanger != null) {
            exchanger.interact("Exchange");
        }
    }
}
