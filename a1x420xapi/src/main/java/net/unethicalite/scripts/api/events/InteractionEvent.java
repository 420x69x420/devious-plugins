package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.scripts.api.utils.Sleep;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class InteractionEvent extends AbstractEvent {
    private SceneEntity sceneEntity;
    private String action;
    private WorldPoint worldPoint;

    public InteractionEvent() {
        this.setEventTimeoutTicks(3);
    }

    public SceneEntity getSceneEntity() {
        return sceneEntity;
    }

    public InteractionEvent setSceneEntity(SceneEntity sceneEntity) {
        this.sceneEntity = sceneEntity;
        return this;
    }

    public String getAction() {
        return action;
    }

    public InteractionEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public WorldPoint getWorldPoint() {
        return worldPoint;
    }

    public InteractionEvent setWorldPoint(WorldPoint worldPoint) {
        this.worldPoint = worldPoint;
        return this;
    }

    @Override
    public void onLoop() {
        Sleep.shortSleep();
        if (getSceneEntity() == null || !Reachable.isInteractable(getSceneEntity())) {
            if (getSceneEntity() != null) {
                log.info("[INTERACT EVENT] - Walking closer to scene entity");
                new MovementEvent()
                        .setDestination(getSceneEntity().getWorldLocation())
                        .execute();
                return;
            }

            if (getWorldPoint() != null) {
                log.info("[INTERACT EVENT] - Walking to scene entity");
                new MovementEvent()
                        .setDestination(getWorldPoint())
                        .execute();
                return;
            }

            log.info("[INTERACT EVENT] - Can't find the scene entity to interact with");
            return;
        }

        if (getAction() == null) {
            setAction(Arrays.stream(getSceneEntity().getActions()).filter(Objects::nonNull).findFirst().orElse(null));
            if (getAction() == null) {
                log.info("[INTERACT EVENT] - Can't find action");
                return;
            }
        }

        log.info("[INTERACT EVENT] - Interacting with '" + getSceneEntity().getName() + "' using '" + getAction() + "' action");
        getSceneEntity().interact(getAction());

        String sleepDescription = (this.getEventCompletedCondition() != null) ?
                "[INTERACT EVENT] - Sleeping until complete condition is met with a timeout of " + this.getEventTimeoutTicks().getAsInt() + " ticks" :
                "[INTERACT EVENT] - Sleeping for " + this.getEventTimeoutTicks().getAsInt() + " ticks";

        log.info(sleepDescription);
        if (this.getEventCompletedCondition() != null) {
            Time.sleepTicksUntil(this.getEventCompletedCondition(), this.getEventTimeoutTicks().getAsInt());
        } else {
            Time.sleepTicks(this.getEventTimeoutTicks().getAsInt());
        }
    }
}
