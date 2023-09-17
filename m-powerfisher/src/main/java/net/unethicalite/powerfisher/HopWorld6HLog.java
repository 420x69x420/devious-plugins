package net.unethicalite.powerfisher;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.World;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.WorldService;
import net.runelite.http.api.worlds.WorldResult;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

@Slf4j
public class HopWorld6HLog
{
	public static long hopTick = 0;
	public static int ticks = 0;

	public static boolean shouldHop() {
		return ticks > hopTick;
	}

	public static void resetHopTick() {
		long ticks2 = Rand.nextInt((int) (60D * 60D * 2D / 0.6D), (int) (60D * 60D * 4D / 0.6D));
		int minutes = (int) (ticks2 * 0.6D / 60D);
		log.info("Reset hop tick timer for: "+ticks2+" from now ("+minutes+" minutes)");
		// Reset timer for 2-4 hours from now in ticks based on current tick
		hopTick = ticks + ticks2;
	}
	public static boolean closeCommonInterfaces() {
		if (Bank.isOpen()) {
			Bank.close();
			return false;
		}
		if (GrandExchange.isOpen()) {
			GrandExchange.close();
			return false;
		}
		if (Dialog.isViewingOptions()) {
			Movement.walk(Players.getLocal());
			Time.sleepTick();
			return false;
		}
		if (Dialog.isEnterInputOpen()) {
			Keyboard.type("1",true);
			return false;
		}
		if (Trade.isOpen()) {
			Trade.accept();
			return false;
		}
		if (isMultiSkillMenuOpen()) {
			Movement.walk(Players.getLocal().getWorldLocation());
			return false;
		}
		return true;
	}
	public static Widget getMultiSkillMenu() {
	return Widgets.get(WidgetInfo.MULTI_SKILL_MENU);
}
	public static boolean isMultiSkillMenuOpen() {
		Widget w = getMultiSkillMenu();
		return w != null && w.isVisible();
	}
	public static boolean openWorldHopper() {
		if (!closeCommonInterfaces()) {
			return false;
		}
		if (Widgets.get(WidgetInfo.WORLD_SWITCHER_LIST) != null) {
			return true;
		}

		Static.getClient().openWorldHopper();
		return Widgets.get(WidgetInfo.WORLD_SWITCHER_LIST) != null;
	}


	/**
	 * Hops to either a F2P or members world
	 * @return
	 */
	public static int hop()
	{
		if (!openWorldHopper()) {
			return -1;
		}
		int oldWorldId = Worlds.getCurrentId();
		World oldWorld = Worlds.getCurrentWorld();
		World randWorld = Worlds.getRandom(w -> oldWorld.getId() != w.getId() &&
				oldWorld.isMembers() == w.isMembers() &&
				oldWorld.isNormal() == w.isNormal() &&
				oldWorld.isAllPkWorld() == w.isAllPkWorld() &&
				oldWorld.isLeague() == w.isLeague() &&
				oldWorld.isSkillTotal() == w.isSkillTotal() &&
				oldWorld.getActivity().toLowerCase().contains("fresh start") == w.getActivity().toLowerCase().contains("fresh start") &&
				oldWorld.getActivity().toLowerCase().contains("beta") == w.getActivity().toLowerCase().contains("beta") &&
				oldWorld.isQuestSpeedRunning() == w.isQuestSpeedRunning() &&
				oldWorld.isTournament() == w.isTournament());
		if (randWorld == null) {
			return -2;
		}
		int randWorldID = randWorld.getId();
		log.info("Hopping from: "+oldWorldId+" to: "+randWorldID);
		Static.getClient().hopToWorld(randWorld);
		if (Time.sleepUntil(() -> Worlds.getCurrentId() != oldWorldId, 100, 10_000)) {
			log.info("Hop successful!");
			resetHopTick();
		} else {
			log.info("Hop failed!");
		}
		return -2;
	}
}
