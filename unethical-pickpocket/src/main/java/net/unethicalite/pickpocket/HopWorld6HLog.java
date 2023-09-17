package net.unethicalite.pickpocket;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

@Slf4j
public class HopWorld6HLog
{
	public static long hopTick = 0;


	public static boolean shouldHop() {
		return API.ticks > hopTick;
	}

	public static void resetHopTick() {
		long ticks = Rand.nextInt((int) (60D * 60D * 2D / 0.6D), (int) (60D * 60D * 4D / 0.6D));
		int minutes = (int) (ticks * 0.6D / 60D);
		log.info("Reset hop tick timer for: "+ticks+" from now ("+minutes+" minutes)");
		// Reset timer for 2-4 hours from now in ticks based on current tick
		hopTick = API.ticks + ticks;
	}

	public static boolean openWorldHopper() {
		API.shortSleep();
		if (Bank.isOpen()) {
			Bank.close();
			return false;
		}
		if (GrandExchange.isOpen()) {
			GrandExchange.close();
		}
		if (Dialog.canContinue()) {
			Dialog.continueSpace();
			return false;
		}
		if (Dialog.isViewingOptions()) {
			Keyboard.type("1");
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
	public static int hop(boolean members)
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
			log.info("not able to find another world to hop to :o currently on world: "+oldWorldId);
			return -2;
		}
		int randWorldID = randWorld.getId();
		log.info("Hopping from: "+oldWorldId+" to: "+randWorldID);
		Static.getClient().hopToWorld(randWorld);
		if (Time.sleepUntil(() -> Worlds.getCurrentId() != oldWorldId, () -> Game.getState() == GameState.HOPPING, 100, 4000)) {
			log.info("Hop successful!");
			resetHopTick();
		} else {
			log.info("Hop failed!");
		}
		return -2;
	}
}
