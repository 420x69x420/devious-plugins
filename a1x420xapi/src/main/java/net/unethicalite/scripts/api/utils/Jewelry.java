package net.unethicalite.scripts.api.utils;

import net.runelite.api.ItemID;

public class Jewelry {
    public static int[] skillsNecklaceIDs = {
            ItemID.SKILLS_NECKLACE1,
            ItemID.SKILLS_NECKLACE2,
            ItemID.SKILLS_NECKLACE3,
            ItemID.SKILLS_NECKLACE4,
            ItemID.SKILLS_NECKLACE5,
            ItemID.SKILLS_NECKLACE6,
    };
    public static int[] gamesNecklaceIDs = {
            ItemID.GAMES_NECKLACE1,
            ItemID.GAMES_NECKLACE2,
            ItemID.GAMES_NECKLACE3,
            ItemID.GAMES_NECKLACE4,
            ItemID.GAMES_NECKLACE5,
            ItemID.GAMES_NECKLACE6,
            ItemID.GAMES_NECKLACE7,
            ItemID.GAMES_NECKLACE8
    };
    public static int[] chargedGloryIDs = {
            ItemID.AMULET_OF_GLORY1,
            ItemID.AMULET_OF_GLORY2,
            ItemID.AMULET_OF_GLORY3,
            ItemID.AMULET_OF_GLORY4,
            ItemID.AMULET_OF_GLORY5,
            ItemID.AMULET_OF_GLORY6
    };
    public static int[] chargedWealthIDs = {
            ItemID.RING_OF_WEALTH_1,
            ItemID.RING_OF_WEALTH_2,
            ItemID.RING_OF_WEALTH_3,
            ItemID.RING_OF_WEALTH_4,
            ItemID.RING_OF_WEALTH_5
    };
    public static int[] passageNecklaceIDsAbove2 = {
            ItemID.NECKLACE_OF_PASSAGE2,
            ItemID.NECKLACE_OF_PASSAGE3,
            ItemID.NECKLACE_OF_PASSAGE4,
            ItemID.NECKLACE_OF_PASSAGE5
    };
    public static int[] passageNecklaceAllIDs = {
            ItemID.NECKLACE_OF_PASSAGE1,
            ItemID.NECKLACE_OF_PASSAGE2,
            ItemID.NECKLACE_OF_PASSAGE3,
            ItemID.NECKLACE_OF_PASSAGE4,
            ItemID.NECKLACE_OF_PASSAGE5
    };
    public static int getLeastOwnedGamesNecklaceID() {
        for (int id : gamesNecklaceIDs) {
            if (OwnedItems.containsIncludingNoted(id)) {
                return id;
            }
        }
        return -1;
    }
    public static int getLeastOwnedSkillsNecklaceID() {
        for (int id : skillsNecklaceIDs) {
            if (OwnedItems.containsIncludingNoted(id)) {
                return id;
            }
        }
        return -1;
    }
    public static int getLeastOwnedChargedGloryID() {
        for (int id : chargedGloryIDs) {
            if (OwnedItems.containsIncludingNoted(id)) {
                return id;
            }
        }
        return -1;
    }
    public static int getLeastOwnedChargedPassageID() {
        for (int id : passageNecklaceAllIDs) {
            if (OwnedItems.containsIncludingNoted(id)) {
                return id;
            }
        }
        return -1;
    }
    public static int getLeastOwnedChargedPassageAbove2ID() {
        for (int id : passageNecklaceIDsAbove2) {
            if (OwnedItems.containsIncludingNoted(id)) {
                return id;
            }
        }
        return -1;
    }
    public static int getLeastOwnedChargedWealthID() {
        for (int id : chargedWealthIDs) {
            if (OwnedItems.containsIncludingNoted(id)) {
                return id;
            }
        }
        return -1;
    }
}
