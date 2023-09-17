package net.unethicalite.plugins.cooker;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Meat
{
	MEAT(ItemID.RAW_BEEF, ItemID.COOKED_MEAT, 4, 1),
	SHRIMPS(ItemID.RAW_SHRIMPS, ItemID.SHRIMPS, 4, 1),
	CHICKEN(ItemID.RAW_CHICKEN, ItemID.COOKED_CHICKEN, 4, 1),
	RABBIT(ItemID.RAW_RABBIT, ItemID.RABBIT, 4, 1),
	ANCHOVIES(ItemID.RAW_ANCHOVIES, ItemID.ANCHOVIES, 4, 1),
	SARDINE(ItemID.RAW_SARDINE, ItemID.SARDINE, 4, 1),
	HERRING(ItemID.RAW_HERRING, ItemID.HERRING, 4,5),
	MACKEREL(ItemID.RAW_MACKEREL, ItemID.MACKEREL, 4,10),
	TROUT(ItemID.RAW_TROUT, ItemID.TROUT, 4,15),
	COD(ItemID.RAW_COD, ItemID.COD, 4,18),
	PIKE(ItemID.RAW_PIKE, ItemID.PIKE, 4,20),
	SALMON(ItemID.RAW_SALMON, ItemID.SALMON, 4,25),
	TUNA(ItemID.RAW_TUNA, ItemID.TUNA, 4,30),
	LOBSTER(ItemID.RAW_LOBSTER, ItemID.LOBSTER, 4,40),
	BASS(ItemID.RAW_BASS, ItemID.BASS, 4,43),
	SWORDFISH(ItemID.RAW_SWORDFISH, ItemID.SWORDFISH, 4,45),
	MONKFISH(ItemID.RAW_MONKFISH, ItemID.MONKFISH, 4,62),
	KARAMBWAN(ItemID.RAW_KARAMBWAN, ItemID.COOKED_KARAMBWAN, 4, 1,30),
	SHARK(ItemID.RAW_SHARK, ItemID.SHARK, 4,80),
	SEA_TURTLE(ItemID.RAW_SEA_TURTLE, ItemID.SEA_TURTLE, 4,82),
	ANGLERFISH(ItemID.RAW_ANGLERFISH, ItemID.ANGLERFISH, 4,84),
	DARK_CRAB(ItemID.RAW_DARK_CRAB, ItemID.DARK_CRAB, 4,90),
	MANTA_RAY(ItemID.RAW_MANTA_RAY, ItemID.MANTA_RAY, 4,91),

	;

	private final int rawId;
	private final int cookedId;
	private final int cookTicks;
	private final int cookLvl;
	private final int productionIndex;

	Meat(int rawId, int cookedId, int cookTicks, int productionIndex, int cookLvl)
	{
		this.rawId = rawId;
		this.cookedId = cookedId;
		this.cookTicks = cookTicks;
		this.productionIndex = productionIndex;
		this.cookLvl = cookLvl;
	}

	Meat(int rawId, int cookedId, int cookTicks, int cookLvl)
	{
		this(rawId, cookedId, cookTicks, 0, cookLvl);
	}
}
