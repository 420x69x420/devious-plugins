package net.unethicalite.scripts.runnermule;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.World;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.WorldService;
import net.runelite.http.api.worlds.WorldResult;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.widgets.Widgets;

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
		if (!API.closeCommonInterfaces()) {
			return false;
		}
		if (Widgets.get(WidgetInfo.WORLD_SWITCHER_LIST) != null) {
			return true;
		}

		API.shortSleep();
		Worlds.openHopper();
		return Worlds.isHopperOpen();
	}

	/**
	 * Hops to either a F2P or members world
	 * @return
	 */
	public static int hop(WorldService worldService)
	{
		WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null)
		{
			return API.shortReturn();
		}

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
		Worlds.hopTo(randWorld);
		Time.sleepTicks(2);
		if (Worlds.getCurrentId() != oldWorldId) {
			log.info("Hop successful!");
			resetHopTick();
		} else {
			log.info("Hop failed!");
		}
		return -2;
	}

	/**
	 * Hops to either a F2P or members world
	 * @return
	 */
	public static int hop(WorldService worldService, int id)
	{
		WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null)
		{
			return API.shortReturn();
		}
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
			resetHopTick();
		} else {
			log.info("Hop failed!");
		}
		return -2;
	}
}
