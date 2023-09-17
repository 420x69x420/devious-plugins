package net.unethicalite.scripts.runner;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;

public enum Altar {
    AIR(ItemID.AIR_TIARA, new WorldPoint(2842,4832, 0), new WorldPoint(2986,3294,0)),
    BODY(ItemID.BODY_TIARA, new WorldPoint(2521, 4842, 0), new WorldPoint(3055,3444,0));
    int tiara;
    WorldPoint altarTile;
    WorldPoint entranceTile;
    Altar(int id, WorldPoint altarTile, WorldPoint entranceTile) {
        this.tiara = id;
        this.altarTile = altarTile;
        this.entranceTile = entranceTile;
    }
}
