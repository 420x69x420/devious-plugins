package net.unethicalite.plugins.cooker.tasks;

import net.runelite.api.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.chat.ChatColorType;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Production;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.plugins.cooker.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Cook extends CookerTask
{
	public Cook(CookerPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return true;
	}

	public static Meat meat = null;
	public static List<Meat> sortedMeats;
	static {
		//sort meats highest -> lowest cooking lvl one time
		sortedMeats = Arrays.stream(Meat.values())
				.sorted((meat1, meat2) -> Integer.compare(meat2.getCookLvl(), meat1.getCookLvl()))
				.collect(Collectors.toList());
	}
	@Override
	public int execute()
	{
		if (getConfig().hop() && HopWorld6HLog.shouldHop()) {
			return HopWorld6HLog.hop();
		}
		int cookingLvl = Skills.getLevel(Skill.COOKING);

		if (!getConfig().progressive()) {
			meat = getConfig().item();
		}
		Item raw = null;
		if (meat != null) {
			raw = Inventory.getFirst(meat.getRawId());
		}

		Player local = Players.getLocal();

		if (raw == null)
		{
			if (Bank.isOpen())
			{
				if (getConfig().progressive()) {
					boolean foundMeat = false;
					for (Meat fesh : sortedMeats) {
						if (cookingLvl >= fesh.getCookLvl() && (Bank.Inventory.getCount(fesh.getRawId()) > 0 || Bank.getCount(fesh.getRawId()) > 0)) {
							meat = fesh;
							foundMeat = true;
							break;
						}
					}
					if (!foundMeat) {
						MessageUtils.addMessage("All out of food to cook!", ChatColorType.HIGHLIGHT);
						this.stop();
						return -1;
					}
				}
				Meat finalMeat = meat;
				Predicate<Item> rawPredicate = x -> x.getId() == finalMeat.getRawId();
				if (Inventory.contains(rawPredicate.negate()))
				{
					Bank.depositInventory();
					return -2;
				}

				Bank.withdrawAll(meat.getRawId(), Bank.WithdrawMode.ITEM);
				return -2;
			}

			NPC banker = NPCs.getNearest(npc -> npc.hasAction("Collect"));
			if (banker != null)
			{
				banker.interact("Bank");
				return -3;
			}

			TileObject bank = TileObjects.getFirstSurrounding(local.getWorldLocation(), 10, obj -> obj.hasAction("Collect") || obj.getName().startsWith("Bank"));
			if (bank != null)
			{
				bank.interact("Bank", "Use");
				return -3;
			}

			MessageUtils.addMessage("Bank not found.", ChatColorType.HIGHLIGHT);
			return -1;
		}

		if (local.getAnimation() == AnimationID.COOKING_RANGE || local.getAnimation() == AnimationID.COOKING_FIRE)
		{
			taskCooldown = getClient().getTickCount() + meat.getCookTicks();
			return -1;
		}

		if (getClient().getTickCount() < taskCooldown)
		{
			return -1;
		}

		TileObject cookingObject = TileObjects.getFirstSurrounding(
				local.getWorldLocation(),
				10,
				x -> x.hasAction("Cook")
		);
		if (cookingObject == null)
		{
			MessageUtils.addMessage("Fire/cooking range not found.", ChatColorType.HIGHLIGHT);
			return -1;
		}


		if (Production.isOpen())
		{
			Widgets.get(WidgetID.MULTISKILL_MENU_GROUP_ID, 14 + meat.getProductionIndex()).interact(0);
			return -meat.getCookTicks();
		}

		cookingObject.interact("Cook");
		return 1000;
	}

	@Override
	public boolean subscribe()
	{
		return true;
	}
}
