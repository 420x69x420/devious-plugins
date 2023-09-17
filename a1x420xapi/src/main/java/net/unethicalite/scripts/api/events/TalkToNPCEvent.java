package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;

import java.util.function.BooleanSupplier;

@Slf4j
public class TalkToNPCEvent extends AbstractEvent {
    private int destinationRadius;
    private String npcName;
    private WorldPoint npcTile;
    private boolean walkable;
    private String action;
    private BooleanSupplier afterInteractSleepUntilCondition;
    public TalkToNPCEvent setAfterInteractSleepUntilCondition(BooleanSupplier condition) {
        this.afterInteractSleepUntilCondition = condition;
        return this;
    }
    public TalkToNPCEvent setNPCAction(String action) {
        this.action = action;
        return this;
    }
    public TalkToNPCEvent(WorldPoint npcTile, String npcName, int destinationRadius, boolean walkable) {
        this.npcTile = npcTile;
        this.npcName = npcName;
        this.destinationRadius = destinationRadius;
        this.walkable = walkable;
        this.action = "Talk-to";
        this.afterInteractSleepUntilCondition = Dialog::isOpen;
        this.setEventCompletedCondition(() -> {
            NPC npc = NPCs.getNearest((x) -> x.getWorldLocation().distanceTo(npcTile) <= destinationRadius && x.getName().equals(npcName));
            return npc != null &&
                    (this.walkable && Reachable.isWalkable(npc.getWorldLocation()) || !this.walkable) &&
                    this.afterInteractSleepUntilCondition.getAsBoolean();
        });
        this.setEventTimeoutTicks(200);
    }
    @Override
    public void onLoop() {
        NPC npc = NPCs.getNearest((x) -> x.getWorldLocation().distanceTo(npcTile) <= destinationRadius && x.getName().equals(npcName));
        if (npc != null && Reachable.isWalkable(npc.getWorldLocation())) {
            npc.interact("Talk-to");
            Time.sleepUntil(() -> this.afterInteractSleepUntilCondition.getAsBoolean(), () -> Players.getLocal().isMoving(), 100, 3000);
            return;
        }
        new MovementEvent()
                .setDestination(this.npcTile)
                .setEventCompletedCondition(() -> {
                    NPC npc2 = NPCs.getNearest((x) -> x.getWorldLocation().distanceTo(npcTile) <= destinationRadius && x.getName().equals(npcName));
                    return npc2 != null && (this.walkable ? Reachable.isWalkable(npc2.getWorldLocation()) : true);
                })
                .execute();
    }
}
