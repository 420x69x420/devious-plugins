package net.unethicalite.scripts.runner;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.util.Text;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.script.blocking_events.BlockingEventManager;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import org.pf4j.Extension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// This annotation is required in order for the client to detect it as a plugin/script.
@Slf4j
@PluginDescriptor(
		name = "420xRunner",
		description = "Runs runecrafting essence!",
		enabledByDefault = false,
		tags = {"420xrunner","runecrafting"})
@Extension
public class RunnerPlugin extends Script
{
	@Inject
	private Client client;
	@Inject
	private ItemManager itemManager;
	@Inject
	private RunnerConfig config;
	@Inject
	private WorldService worldService;
	@Inject
	private ConfigManager configManager;
	private int initFPS = 50;
	private String ourName = null;
	private WorldPoint MULE_TILE = new WorldPoint(3082, 3513, 1);
	private boolean crafterBusy = false;

	public static Altar altar = Altar.AIR;
	public static int craftWorld = 301;
	public static int muleWorld = 301;
	@Provides
	RunnerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(RunnerConfig.class);
	}

	@Override
	public void onStart(String... strings) {
		API.ticks = 0;
		API.stopScript = false;
		initFPS = client.getFPS();
		client.setUnlockedFps(true);
		client.setUnlockedFpsTarget(3);
		BlockingEventManager blockingManager = this.getBlockingEventManager();
		blockingManager.remove(LoginEvent.class);
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
					Log.sendKeyValuePairToOutputStream("BOT_GROUP","runner");
				}
			}
		}
		//change off trade delay and hop worlds settings
		if (CustomizeSettings.validate()) {
			return CustomizeSettings.execute(client);
		}
		crafterBusy = false;
		RunnerFileOperations.updateAllSettings();
		int notedEss = itemManager.getItemComposition(ItemID.PURE_ESSENCE).getLinkedNoteId();
		int notedAirTiara = itemManager.getItemComposition(ItemID.PURE_ESSENCE).getLinkedNoteId();
		int notedBodyTiara = itemManager.getItemComposition(ItemID.PURE_ESSENCE).getLinkedNoteId();
		int otherTiara = (altar.tiara == ItemID.AIR_TIARA ? ItemID.BODY_TIARA : ItemID.AIR_TIARA);

		if (!BankCache.checkCache()) {
			if (Bank.isOpen()) {
				BankCache.update();
				return API.fastReturn();
			} else {
				if (inAnAltar()) {
					return exitPortal();
				}
				return BankHelper.clickLocalBank();
			}
		}
		if (Bank.isOpen()) {
			BankCache.update();
			if (Bank.Inventory.getCount(notedAirTiara, notedBodyTiara, notedEss) > 0) {
				Bank.depositInventory();
				return API.returnTick();
			}
			if (!OwnedItems.contains(ItemID.PURE_ESSENCE) || !OwnedItems.contains(altar.tiara)) {
				if (!API.invyOnlyContains(notedBodyTiara, notedAirTiara, notedEss)) {
					BankHelper.depositAllExcept(notedBodyTiara, notedAirTiara, notedEss);
					return API.returnTick();
				}
				if (Bank.contains(ItemID.PURE_ESSENCE)) {
					Bank.withdraw(ItemID.PURE_ESSENCE, Integer.MAX_VALUE, Bank.WithdrawMode.NOTED);
				}
				Bank.close();
				return API.returnTick();
			}
			if (Equipment.fromSlot(EquipmentInventorySlot.WEAPON) != null) {
				Bank.depositEquipment();
				return API.returnTick();
			}
			if (!Equipment.contains(altar.tiara)) {
				Item tiara = Bank.Inventory.getFirst(altar.tiara);
				if (tiara != null) {
					tiara.interact("Wear");
					return API.returnTick();
				}
				BankHelper.depositAllExcept(altar.tiara);
				Bank.withdraw(altar.tiara, 1, Bank.WithdrawMode.ITEM);
				return API.returnTick();
			}
			if (Bank.Inventory.getCount(otherTiara) > 1) {
				Bank.depositInventory();
				return API.returnTick();
			}
			if (Bank.Inventory.getCount(otherTiara) == 0) {
				if (Inventory.isFull()) {
					Bank.depositInventory();
				}
				BankHelper.bankWithdraw(otherTiara, 1, Bank.WithdrawMode.ITEM);
				return API.returnTick();
			}
			if (Bank.Inventory.getCount(ItemID.PURE_ESSENCE) > 0) {
				Bank.close();
				return API.returnTick();
			}
			BankHelper.depositAllExcept(ItemID.PURE_ESSENCE, ItemID.AIR_TIARA, ItemID.BODY_TIARA);
			Bank.withdraw(ItemID.PURE_ESSENCE, 27, Bank.WithdrawMode.ITEM);
			return API.returnTick();
		}

		//have any products of muling, go bank
		if (Inventory.contains(notedBodyTiara, notedAirTiara, notedEss)) {
			if (muleWorld == Worlds.getCurrentId()) {
				return HopWorld.hop(worldService);
			}
			return BankHelper.clickLocalBank();
		}

		//equip tiara if have one unequipped outside of bank
		if (!Equipment.contains(altar.tiara) || !Inventory.contains(otherTiara)) {
			Item tiara = Inventory.getFirst(altar.tiara);
			if (tiara != null) {
				tiara.interact("Wear");
				return API.returnTick();
			}
			//not have any tiara equipped but have some, go bank
			if (OwnedItems.contains(altar.tiara) && OwnedItems.contains(ItemID.PURE_ESSENCE)) {
				return BankHelper.clickLocalBank();
			}
		}

		// not have essence, start muling
		// but skip this codeblock if in trade (very last trade will empty inventory and signal owneditems count = 0 due to not counting in trade window)
		if (!Inventory.contains(ItemID.PURE_ESSENCE)) {
			//accept everything, give nothing
			if (Trade.isOpen()) {
				if (!Trade.hasAccepted(false)) {
					Trade.accept();
				}
				return API.shortReturn();
			}
			//if have some more running supplies, go to bank
			if (OwnedItems.contains(ItemID.PURE_ESSENCE) && OwnedItems.contains(altar.tiara)) {
				//walker wont handle coming out of altar area, we must exit ourself
				if (altar.altarTile.distanceTo(Players.getLocal()) < 40) {
					//hop worlds off craft world
					if (craftWorld == Worlds.getCurrentId()) {
						return HopWorld.hop(worldService);
					}
					if (inAnAltar()) {
						exitPortal();
					}
					return API.returnTick();
				}
				return BankHelper.clickLocalBank();
			}
			if (MULE_TILE.distanceTo(Players.getLocal()) > 5) {
				if (muleWorld == Worlds.getCurrentId()) {
					return HopWorld.hop(worldService);
				}
				if (API.shouldWalk()) {
					if (inAnAltar()) {
						return exitPortal();
					}
					return API.walkTo(MULE_TILE);
				}
				return API.returnTick();
			}

			List<String> validMules = RunnerFileOperations.readAllMules();
			Player mule = Players.getNearest(p -> validMules.contains(p.getName()));
			if (mule != null) {
				mule.interact("Trade with");
				Time.sleepUntil(() -> Trade.isOpen(), 100, 5000);
				return API.shortReturn();
			}
			if (muleWorld != Worlds.getCurrentId()) {
				return HopWorld.hop(worldService, muleWorld);
			}
			Time.sleepTicks(Rand.nextInt(3,10));
			return API.returnTick();
		}

		//have essence, start running
		//inside altar
		if (altar.altarTile.distanceTo(Players.getLocal()) < 40) {
			//need to walk to tile and hop worlds
			if (craftWorld != Worlds.getCurrentId()) {
				if (altar.altarTile.equals(Players.getLocal().getWorldLocation())) {
					return HopWorld.hop(worldService, craftWorld);
				}
				if (API.shouldWalk()) {
					Movement.walk(altar.altarTile);
				}
				return API.returnTick();
			}

			//handle trading
			if (Trade.isFirstScreenOpen()) {
				if (Inventory.contains(ItemID.PURE_ESSENCE)) {
					Trade.offer(ItemID.PURE_ESSENCE, Inventory.getCount(ItemID.PURE_ESSENCE));
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
				return API.returnTick();
			}

			//find a random crafter and trade them
			List<String> validCrafterNames = RunnerFileOperations.readAllCrafters();
			List<Player> crafters = Players.getAll(p -> validCrafterNames.contains(Text.toJagexName(p.getName())));
			if (crafters.size() > 0) {
				Collections.shuffle(crafters);
				crafters.get(0).interact("Trade with");
				Time.sleepUntil(() -> Trade.isOpen() || crafterBusy, 100, 10000);
				if (crafterBusy) {
					Time.sleepTicks(1);
				}
				return API.shortReturn();
			}
			return API.returnTick();
		}

		//outside of our chosen altar
		if (craftWorld == Worlds.getCurrentId()) {
			return HopWorld.hop(worldService);
		}

		//near outside entrance
		if (altar.entranceTile.distanceToHypotenuse(Players.getLocal().getWorldLocation()) < 15) {
			TileObject entrance = TileObjects.getNearest(g -> g.getName().equals("Mysterious ruins") && g.hasAction("Enter"));
			if (entrance != null) {
				entrance.interact("Enter");
				Time.sleepUntil(() -> altar.altarTile.distanceTo(Players.getLocal()) < 40, () -> Players.getLocal().isMoving(), 100, 3000);
				return API.shortReturn();
			}
		}
		if (API.shouldWalk()) {
			if (inAnAltar()) {
				return exitPortal();
			}
			return API.walkTo(altar.entranceTile);
		}
		return API.returnTick();
	}
	public boolean inAnAltar() {
		return Altar.AIR.altarTile.distanceTo(Players.getLocal()) < 40 || Altar.BODY.altarTile.distanceTo(Players.getLocal()) < 40;
	}
	public int exitPortal() {
		TileObject exitPortal = TileObjects.getNearest(t -> t.hasAction("Use") && t.getName().equals("Portal") && (t.distanceTo(Altar.AIR.altarTile) < 40 || t.distanceTo(Altar.BODY.altarTile) < 40));
		if (exitPortal != null) {
			if (exitPortal.distanceTo(Players.getLocal()) > 15) {
				if (API.shouldWalk()) {
					return API.walkTo(exitPortal);
				}
			} else {
				exitPortal.interact("Use");
				Time.sleepUntil(() -> TileObjects.getNearest(t -> t.hasAction("Use") && t.getName().equals("Portal")) == null, () -> Players.getLocal().isMoving(), 100, 4000);
			}
		}
		return API.shortReturn();
	}
	@Subscribe
	public void onGameTick(final GameTick e) {
		API.ticks++;
		CustomizeSettings.hopWorldsWarningVarbit = Vars.getBit(CustomizeSettings.HOP_WORLDS_WARNING_VARBIT);
		CustomizeSettings.acceptTradeDelayVarbit = Vars.getBit(CustomizeSettings.ACCEPT_TRADE_DELAY_VARBIT);
		CustomizeSettings.settingsTabVisibleVarbit = Vars.getBit(CustomizeSettings.SETTINGS_TAB_VISIBLE_VARBIT);
	}
	@Subscribe
	public void onChatMessage(final ChatMessage msg) {
		if (msg.getType().equals(ChatMessageType.TRADE)) {
			if (msg.getMessage().contains("is busy at the moment.") && !Trade.isOpen()) {
				crafterBusy = true;
			}
		}
	}

}
