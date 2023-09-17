package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.framework.InterfaceInstance;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
@Slf4j
public abstract class AbstractEvent {
    public Plugin plugin;
    private Integer eventStartTicks;
    private IntSupplier eventTimeoutTicks;
    private boolean eventCompleted;
    private boolean eventFailed;
    private BooleanSupplier eventCompletedCondition;
    private BooleanSupplier eventInterruptCondition;

    public AbstractEvent() {
        if (InterfaceInstance.pluginInterface == null) {
            throw new IllegalStateException("Plugin interface not set");
        }
        this.setEventStartTicks(Static.getClient().getTickCount());
    }

    public Integer getEventStartTicks() {
        return eventStartTicks;
    }

    public AbstractEvent setEventStartTicks(Integer eventStartTicks) {
        this.eventStartTicks = eventStartTicks;
        return this;
    }
    public AbstractEvent resetEventStartTicks() {
        this.eventStartTicks = Static.getClient().getTickCount();
        return this;
    }

    public IntSupplier getEventTimeoutTicks() {
        return eventTimeoutTicks;
    }

    public AbstractEvent setEventTimeoutTicks(IntSupplier eventTimeoutTicks) {
        this.eventTimeoutTicks = eventTimeoutTicks;
        return this;
    }

    public AbstractEvent setEventTimeoutTicks(Integer eventTimeoutTicks) {
        this.eventTimeoutTicks = () -> eventTimeoutTicks;
        return this;
    }

    public boolean getEventCompleted() {
        return eventCompleted;
    }

    public AbstractEvent setEventCompleted(boolean eventCompleted) {
        this.eventCompleted = eventCompleted;
        return this;
    }

    public boolean getEventFailed() {
        return eventFailed;
    }

    public AbstractEvent setEventFailed(boolean eventFailed) {
        this.eventFailed = eventFailed;
        return this;
    }

    public BooleanSupplier getEventCompletedCondition() {
        return eventCompletedCondition;
    }

    public AbstractEvent setEventCompletedCondition(BooleanSupplier eventCompletedCondition) {
        this.eventCompletedCondition = eventCompletedCondition;
        return this;
    }

    public BooleanSupplier getEventInterruptCondition() {
        return eventInterruptCondition;
    }

    public AbstractEvent setEventInterruptCondition(BooleanSupplier eventInterruptCondition) {
        this.eventInterruptCondition = eventInterruptCondition;
        return this;
    }

    public boolean isEventCompleted() {
        if (getEventCompletedCondition() != null && getEventCompletedCondition().getAsBoolean()) {
            log.info("[ABSTRACT EVENT] - Event completed");
            return true;
        }
        if (this.eventCompleted) {
            return true;
        }
        return false;
    }

    public boolean isEventFailed() {
        if (Static.getClient().getTickCount() >= (getEventStartTicks() + (getEventTimeoutTicks() != null ? getEventTimeoutTicks().getAsInt() : 200))) {
            log.info("[ABSTRACT EVENT] - Event timed out");
            return true;
        }
        if (getEventInterruptCondition() != null && getEventInterruptCondition().getAsBoolean()) {
            log.info("[ABSTRACT EVENT] - Event interrupted");
            return true;
        }
        if (this.eventFailed) {
            return true;
        }
        return false;
    }

    public abstract void onLoop();

    public void execute() {
        executed();
    }

    public boolean executed() {
        while (true) {
            if (!isScriptRunning()) {
                log.info("[ABSTRACT EVENT] - Plugin stopped");
                return false;
            }

            if (isEventCompleted()) {
                setEventCompleted(true);
                return true;
            }

            if (isEventFailed()) {
                setEventFailed(true);
                return false;
            }

            Time.sleepTick();
            Time.sleep(1, 100);

            onLoop();

        }
    }
    private boolean isScriptRunning() {
        return Plugins.isEnabled(InterfaceInstance.pluginInterface.plugin());
    }
}
