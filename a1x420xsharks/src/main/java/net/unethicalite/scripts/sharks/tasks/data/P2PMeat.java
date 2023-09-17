package net.unethicalite.scripts.sharks.tasks.data;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.unethicalite.api.game.Skills;
import net.unethicalite.scripts.api.utils.OwnedItems;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum P2PMeat
{
	SHRIMPS(ItemID.RAW_SHRIMPS, ItemID.SHRIMPS, 4, 1),
	TROUT(ItemID.RAW_TROUT, ItemID.TROUT, 4,15),
	SALMON(ItemID.RAW_SALMON, ItemID.SALMON, 4,25),
	TUNA(ItemID.RAW_TUNA, ItemID.TUNA, 4,30),
	LOBSTER(ItemID.RAW_LOBSTER, ItemID.LOBSTER, 4,40),
	BASS(ItemID.RAW_BASS, ItemID.BASS, 4,43),
	SWORDFISH(ItemID.RAW_SWORDFISH, ItemID.SWORDFISH, 4,45),
	MONKFISH(ItemID.RAW_MONKFISH, ItemID.MONKFISH, 4,62),
	SHARK(ItemID.RAW_SHARK, ItemID.SHARK, 4,80)
	;
	public static final int[] allBurntIDs = {
			323,
			343,
			357,
			367,
			369,
			375,
			381,
			387,
			393,
			399,
			1903,
			2005,
			2013,
			2144,
			2146,
			2175,
			2199,
			2247,
			2305,
			2311,
			2329,
			2345,
			2426,
			3127,
			3148,
			3375,
			3383,
			4258,
			4259,
			5002,
			5990,
			6301,
			6699,
			7090,
			7092,
			7094,
			7222,
			7226,
			7520,
			7531,
			7570,
			7948,
			7954,
			10140,
			11938,
			13443,
			20854,
			20869,
			23873
	};
	private final int rawId;
	private final int cookedId;
	private final int cookTicks;
	private final int cookLvl;
	private final int productionIndex;

	P2PMeat(int rawId, int cookedId, int cookTicks, int productionIndex, int cookLvl)
	{
		this.rawId = rawId;
		this.cookedId = cookedId;
		this.cookTicks = cookTicks;
		this.productionIndex = productionIndex;
		this.cookLvl = cookLvl;
	}
	public static List<P2PMeat> sortedMeats;
	static {
		//sort meats highest -> lowest cooking lvl one time
		sortedMeats = Arrays.stream(P2PMeat.values())
				.sorted((meat1, meat2) -> Integer.compare(meat2.getCookLvl(), meat1.getCookLvl()))
				.collect(Collectors.toList());
	}
	public static P2PMeat getHighestLvlMeat(){
		return sortedMeats.stream().filter(m -> Skills.getLevel(Skill.COOKING) >= m.getCookLvl()).findFirst().orElse(null);
	}
	public static P2PMeat getHighestOwnedRawMeatIncludingNoted(){
		return sortedMeats.stream().filter(m -> Skills.getLevel(Skill.COOKING) >= m.getCookLvl() && OwnedItems.containsIncludingNoted(m.getRawId())).findFirst().orElse(null);
	}

	P2PMeat(int rawId, int cookedId, int cookTicks, int cookLvl)
	{
		this(rawId, cookedId, cookTicks, 0, cookLvl);
	}
}
