package net.unethicalite.scripts.runnermule;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.kit.KitType;
import net.runelite.api.util.Text;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.script.blocking_events.BlockingEventManager;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import org.pf4j.Extension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


// This annotation is required in order for the client to detect it as a plugin/script.
@Slf4j
@PluginDescriptor(
		name = "420xRunnerMule",
		description = "Mules to runecraft runners!",
		enabledByDefault = false,
		tags = {"420xrunner mule"})
@Extension
public class RunnerMulePlugin extends Script
{
	@Inject
	private Client client;
	@Inject
	private ItemManager itemManager;
	@Inject
	private WorldService worldService;
	private int initFPS = 50;
	private final Set<String> runners = new HashSet<>();
	private String runnerToTrade = null;
	@Override
	public void onStart(String... strings) {
		API.ticks = 0;
		HopWorld6HLog.hopTick = 0;
		API.stopScript = false;
		initFPS = client.getFPS();
		client.setUnlockedFps(true);
		client.setUnlockedFpsTarget(1);
		BlockingEventManager blockingManager = this.getBlockingEventManager();
		blockingManager.remove(LoginEvent.class);
		runnerToTrade = null;
	}
	@Override
	public void onStop() {
		client.setUnlockedFpsTarget(initFPS);
		API.stopScript = true;
		while (ourName != null) {
			if (RunnerFileOperations.removeEntry(ourName)) {
				ourName = null;
			}
		}
	}
	private String ourName = null;
	@Override
	protected int loop() {
		if (API.stopScript) {
			log.info("Stopping script!");
			this.stop();
			return 10;
		}
		//login using launched credentials
		if (Login.shouldLogin()) {
			return Login.login(client);
		}
		if (ourName == null || ourName.equalsIgnoreCase("null")) {
			ourName = Players.getLocal().getName();
			if (ourName != null && !ourName.equalsIgnoreCase("null")) {
				if (!RunnerFileOperations.writeEntry(ourName)) {
					ourName = null;
				} else {
					Log.sendKeyValuePairToOutputStream("BOT_GROUP","runner_mule");
				}
			}
		}
		//hop every few hrs
		if (HopWorld6HLog.shouldHop()) {
			return HopWorld6HLog.hop(worldService);
		}
		//hop back to mule world
		if (Worlds.getCurrentId() != 394) {
			return HopWorld6HLog.hop(worldService, 394);
		}
		List<String> validRunners = RunnerFileOperations.readAllRunners();
		runners.clear();
		for (String s : validRunners) {
			s = Text.toJagexName(s);
			if (!s.isEmpty() && !s.equalsIgnoreCase("null") && !runners.contains(s)) {
				runners.add(s);
			}
		}
		Player foundRunner = Players.getNearest(p -> Text.toJagexName(p.getName()).equals(runnerToTrade));
		if (foundRunner == null) {
			runnerToTrade = null;
			return API.shortReturn();
		}
		if (runnerToTrade != null) {
			if (Trade.isOpen()) {
				String tradingPlayer = Text.toJagexName(Trade.getTradingPlayer());
				if ((runnerToTrade != null && !tradingPlayer.equals(runnerToTrade)) || !runners.contains(tradingPlayer)) {
					log.info("Not trading correct player, current: "+tradingPlayer+" and runnerToTrade: "+(runnerToTrade == null ? "null" : runnerToTrade));
					Trade.decline();
					return API.returnTick();
				}
				if (Trade.isFirstScreenOpen()) {
					//detect head gear of trading player and dont give tiaras if wearing already
					int headID = foundRunner.getPlayerComposition().getEquipmentId(KitType.HEAD);
					boolean needToGiveTiaras = (headID != ItemID.BODY_TIARA && headID != ItemID.AIR_TIARA);

					int notedEss = itemManager.getItemComposition(ItemID.PURE_ESSENCE).getLinkedNoteId();
					int notedAirTiara = itemManager.getItemComposition(ItemID.AIR_TIARA).getLinkedNoteId();
					int notedBodyTiara = itemManager.getItemComposition(ItemID.BODY_TIARA).getLinkedNoteId();
					Item notedPutUp = Trade.getFirst(false, notedEss);
					if (notedPutUp == null || notedPutUp.getQuantity() < 20_000) {
						Trade.offer(notedEss, 20_000);
						API.shortSleep();
					}
					if (needToGiveTiaras) {
						notedPutUp = Trade.getFirst(false, notedAirTiara);
						if (notedPutUp == null || notedPutUp.getQuantity() < 1) {
							Trade.offer(notedAirTiara, 1);
							API.shortSleep();
						}
						notedPutUp = Trade.getFirst(false, notedBodyTiara);
						if (notedPutUp == null || notedPutUp.getQuantity() < 1) {
							Trade.offer(notedBodyTiara, 1);
							API.shortSleep();
						}
					}
					if (!Trade.hasAcceptedFirstScreen(false)) {
						Trade.acceptFirstScreen();
					}
					return API.returnTick();
				}
				if (Trade.isSecondScreenOpen()) {
					if (!Trade.hasAcceptedSecondScreen(false)) {
						Trade.acceptSecondScreen();
					}
				}
				return API.returnTick();
			}
			foundRunner.interact("Trade with");
			Time.sleepUntil(() -> Trade.isOpen(), 100, 5000);
		}


		int muleWorld = RunnerFileOperations.getMuleWorld();
		if (muleWorld != Worlds.getCurrentId()) {
			return HopWorld6HLog.hop(worldService, muleWorld);
		}
		return API.returnTick();
	}

	@Subscribe
	public void onChatMessage(final ChatMessage msg) {
		if (msg.getType().equals(ChatMessageType.TRADEREQ)) {
			String name = Text.toJagexName(msg.getName());
			if (runners.contains(name)) {
				log.info("Trade request from approved runner: "+name);
				runnerToTrade = name;
			} else {
				log.info("Trade request from: " +name+" not matching list of approved runners: "+runners);
			}
		}
		if (msg.getType().equals(ChatMessageType.TRADE)) {
			if (msg.getMessage().equals("Accepted trade.") || msg.getMessage().equals("Other player declined trade.")) {
				runnerToTrade = null;
			}
		}
	}

}
