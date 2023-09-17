package net.unethicalite.plugins.agility;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.World;
import net.runelite.api.widgets.WidgetInfo;
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

import java.time.Instant;

@Slf4j
public class HopWorld6HLog
{
	public static Instant hopTime = null;


	public static boolean shouldHop() {
		if (hopTime == null) {
			resetHopTime();
		}
		return Instant.now().isAfter(hopTime);
	}

	public static void resetHopTime() {
		long sec = Rand.nextInt((int) (60D * 60D * 2D), (int) (60D * 60D * 4D));
		int minutes = (int) (sec * 0.6D / 60D);
		// Reset timer for 2-4 hours from now in ticks based on current tick
		hopTime = Instant.now().plusSeconds(sec);
		log.info("Reset hop tick timer for: "+minutes+" minutes from now (now == "+Instant.now().toEpochMilli()+ "end == "+hopTime.toEpochMilli()+")");
	}

	private static boolean closeCommonInterfaces() {
		if (Players.getLocal().isMoving() && Players.getLocal().isAnimating()) {
			return false;
		}
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
		return true;
	}

	private static boolean openWorldHopper() {
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
			log.info("not able to find another world to hop to :o currently on world: "+oldWorldId);
			return -2;
		}
		int randWorldID = randWorld.getId();
		log.info("Hopping from: "+oldWorldId+" to: "+randWorldID);
		Static.getClient().hopToWorld(randWorld);
		if (Time.sleepUntil(() -> Worlds.getCurrentId() != oldWorldId, 100, 7000)) {
			log.info("Hop successful!");
			resetHopTime();
		} else {
			log.info("Hop failed!");
		}
		return 100;
	}

	/**
	 * Hops to either a F2P or members world
	 * @return
	 */
	public static int hop(int id)
	{
		if (!openWorldHopper()) {
			return -1;
		}
		int oldWorldId = Worlds.getCurrentId();
		World randWorld = Worlds.getFirst(id);
		if (randWorld == null) {
			log.info("not able to find world: "+id+" to hop to :o currently on world: "+oldWorldId);
			return -2;
		}
		int randWorldID = randWorld.getId();
		log.info("Hopping from: "+oldWorldId+" to: "+randWorldID);
		Worlds.hopTo(randWorld);
		Time.sleepTicks(2);
		if (Worlds.getCurrentId() != oldWorldId) {
			log.info("Hop successful!");
			resetHopTime();
		} else {
			log.info("Hop failed!");
		}
		return 100;
	}
}
