package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.scripts.api.utils.Sleep;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class MovementEvent extends AbstractEvent {
    private WorldPoint worldPoint;
    private WorldArea worldArea;
    private BankLocation bankLocation;
    private int radius;

    public MovementEvent() {
        this.setEventCompletedCondition(() -> (worldPoint != null && worldPoint.distanceTo(Players.getLocal()) == 0) ||
                (worldArea != null && worldArea.contains(Players.getLocal())) ||
                (bankLocation != null && bankLocation.getArea().contains(Players.getLocal())));
        this.radius = 0;
        this.setEventTimeoutTicks(200);
    }

    public MovementEvent setDestination(WorldPoint worldPoint) {
        this.worldPoint = worldPoint;
        return this;
    }

    public MovementEvent setDestination(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }
    public MovementEvent setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public MovementEvent setDestination(WorldArea worldArea) {
        this.worldArea = worldArea;
        this.setEventCompletedCondition(() -> worldArea != null && worldArea.contains(Players.getLocal()));
        return this;
    }
    public WorldPoint getWorldPoint() {
        return worldPoint;
    }

    public MovementEvent setWorldPoint(WorldPoint worldPoint) {
        this.worldPoint = worldPoint;
        return this;
    }

    public WorldArea getWorldArea() {
        return worldArea;
    }


    public BankLocation getBankLocation() {
        return bankLocation;
    }

    public MovementEvent setBankLocation(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }

    @Override
    public void onLoop() {
        if (Movement.isWalking()) {
            return;
        }

        Sleep.shortSleep();
        WorldArea destinationArea = null;
        if (getWorldArea() != null) {
            destinationArea = getWorldArea();
        } else if (getBankLocation() != null) {
            destinationArea = getBankLocation().getArea();
        } else if (getWorldPoint() != null) {
            destinationArea = getWorldPoint().createWorldArea(1,1);
        }

        if (getWorldPoint() == null) {
            if (destinationArea == null) {
                log.info("[MOVEMENT EVENT] - No destination set!");
                this.setEventFailed(true);
                return;
            }
            setWorldPoint(destinationArea.getRandom());
            log.info("[MOVEMENT EVENT] - Destination: " + getWorldPoint());
        }

        if (getWorldPoint().distanceTo(Players.getLocal()) <= radius) {
            this.setEventCompleted(true);
            return;
        }

        if (getWorldPoint().distanceTo(Players.getLocal()) < 25 && !Reachable.isWalkable(getWorldPoint())) {

            List<WorldPoint> walkablePoints = destinationArea.toWorldPointList()
                    .stream()
                    .filter(Reachable::isWalkable)
                    .collect(Collectors.toList());

            if (!walkablePoints.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(walkablePoints.size());

                setWorldPoint(walkablePoints.get(randomIndex));
                log.info("[MOVEMENT EVENT] - New destination: " + getWorldPoint());
            } else {
                log.info("[MOVEMENT EVENT] - Can't find a walkable tile in the area, walking to original destination point");
                Movement.walkTo(getWorldPoint());
                return;
            }
        }

        Movement.walkTo(getWorldPoint());
    }
}
