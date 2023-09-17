package net.unethicalite.scripts.tutorial;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.tutorial.loadouts.InventoryLoadout;
import org.pf4j.Extension;

import java.util.Map;


// This annotation is required in order for the client to detect it as a plugin/script.
@Slf4j
@PluginDescriptor(
		name = "420xAuto Wines",
		description = "A Test!",
		enabledByDefault = false,
		tags = {"420xtester"})
@Extension
public class AutoWines extends Script
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;
	private int winesMade;

	@Override
	public void onStart(String... strings) {
		LootTracker.onItemContainerChanged(null);
	}
	@Override
	protected void startUp() {
	}
	@Override
	protected int loop() {
		log.info("loop");
		if (!fermentWine()) {
			if (!withdrawIngredients()) {

			}
		}
		return API.shortReturn();
	}
	private boolean withdrawIngredients() {
		InventoryLoadout invyLoadout = new InventoryLoadout();
		invyLoadout.addItem(ItemID.GRAPES, 14);
		invyLoadout.addItem(ItemID.JUG_OF_WATER, 14);
		if (invyLoadout.fulfilled() && Bank.isOpen()) {
			Bank.close();
			Time.sleep(API.returnTick());
			return true; //sleep for tick and let bank close fully for our regular inventory container to show up
		}
		log.info("fulfilling grapes+jugs of water");
		invyLoadout.fulfill();
		return true;
	}
	private Widget getMultiSkillMenu() {
		return Widgets.get(WidgetInfo.MULTI_SKILL_MENU);
	}
	private boolean isMultiSkillMenuOpen() {
		Widget w = getMultiSkillMenu();
		return w != null && w.isVisible();
	}

	/**
	 * Processes the items, returns false if no more items to process
	 * @return
	 */
	private boolean fermentWine() {
		Item grapes = Inventory.getFirst(ItemID.GRAPES);
		Item jugs = Inventory.getFirst(ItemID.JUG_OF_WATER);
		if (grapes == null || jugs == null) {
			return false;
		}
		if (isMultiSkillMenuOpen()) {
			Keyboard.type(" ");
			if (Time.sleepUntil(() -> !Inventory.contains(ItemID.GRAPES, ItemID.JUG_OF_WATER), () -> Players.getLocal().isAnimating(), 100, 4000)) {
				//no more items - sleepUntil returned true
				return false;
			}
			//timeout
			return true;
		}
		grapes.useOn(jugs);
		Time.sleepUntil(() -> isMultiSkillMenuOpen(), 100, 3000);
		return true;
	}
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		//extract the difference in item qtys associated with ids by only tracking inventory
		for (Map.Entry<Integer, Integer> i : LootTracker.onItemContainerChanged(event).entrySet()) {
			int id = i.getKey();
			int qty = i.getValue();
			log.debug("Found change in invy! ID: "+id+" qty: " +qty);
			//check if changed item is negative in qty (subtracted from invy), is grapes, and bank is not open. Means made wine
			if (id == ItemID.GRAPES && !Bank.isOpen()) {
				winesMade++;
				log.info("Have wines made: "+winesMade);
			}
		}
	}



}
