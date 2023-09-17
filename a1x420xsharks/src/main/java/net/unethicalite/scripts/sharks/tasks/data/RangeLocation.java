package net.unethicalite.scripts.sharks.tasks.data;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

import java.util.Random;

public enum RangeLocation {
    AL_KHARID(new WorldPoint(3272, 3180, 0), BankLocation.AL_KHARID_BANK),
    CATHERBY(new WorldPoint(2817, 3443, 0), BankLocation.CATHERBY_BANK),
    EDGEVILLE(new WorldPoint(3078, 3495, 0), BankLocation.EDGEVILLE_BANK);

    public WorldPoint rangeTile;
    public BankLocation bankLocation;
    RangeLocation(WorldPoint rangeTile, BankLocation bankLocation) {
        this.rangeTile = rangeTile;
        this.bankLocation = bankLocation;
    }
    public static RangeLocation getRandomNoobRangeLocation() {
        Random random = new Random();
        int enumSize = RangeLocation.values().length;
        System.out.println("Enum size: " + enumSize);
        int randomIndex = random.nextInt(enumSize);
        System.out.println("Random index: " + randomIndex);
        RangeLocation randomRangeLocation = RangeLocation.values()[randomIndex];
        System.out.println(randomRangeLocation);
        return randomRangeLocation;
    }


}
