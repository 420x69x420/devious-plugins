package net.unethicalite.scripts.api.utils;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.Reachable;

import java.util.Set;

public class AreaUtils {
    public static WorldArea generateSquareAroundPoint(WorldPoint center, int radius) {
        WorldPoint searchAreaCenter = new WorldPoint(center.getX() - radius, center.getY() - radius, center.getPlane());
        int sideLengths = (radius * 2) + 1;
        return searchAreaCenter.createWorldArea(sideLengths, sideLengths);
    }

    public static void generateSquareViaWorldPoints(Set<WorldPoint> areaToModify, WorldPoint center, int radius) {
        areaToModify.clear();
        int x = center.getX();
        int y = center.getY();
        int plane = center.getPlane();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                WorldPoint point = new WorldPoint(x + dx, y + dy, plane);
                if (Reachable.isWalkable(point)) {
                    areaToModify.add(point);
                }
            }
        }
    }
}
