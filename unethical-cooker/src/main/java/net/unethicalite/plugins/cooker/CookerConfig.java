package net.unethicalite.plugins.cooker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("unethicalcooker")
public interface CookerConfig extends Config
{
	@ConfigItem(
			position = 0,
			hidden = true,
			hide = "progressive",
			keyName = "item",
			name = "Item",
			description = ""
	)
	default Meat item()
	{
		return Meat.KARAMBWAN;
	}
	@ConfigItem(
			position = 1,
			keyName = "progressive",
			name = "Progressive",
			description = "cooks all raw food in your bank starting with highest lvl can cook"
	)
	default boolean progressive()
	{
		return true;
	}
	@ConfigItem(
			position = 2,
			keyName = "hop",
			name = "Hop Indefinitely",
			description = "Hops every few hrs to run script long time"
	)
	default boolean hop()
	{
		return true;
	}
}
