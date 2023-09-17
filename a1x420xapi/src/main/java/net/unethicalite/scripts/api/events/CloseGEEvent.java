package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.items.GrandExchange;

@Slf4j
public class CloseGEEvent extends AbstractEvent {
    public CloseGEEvent() {
        this.setEventCompletedCondition(() -> !GrandExchange.isOpen());
        this.setEventTimeoutTicks(3);
    }

    @Override
    public void onLoop() {
        log.info("[CLOSE GE EVENT] - Closing grand exchange");
        GrandExchange.close();
    }
}
