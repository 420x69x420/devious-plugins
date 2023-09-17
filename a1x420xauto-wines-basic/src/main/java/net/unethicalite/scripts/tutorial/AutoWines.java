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
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import java.util.Map;


// This annotation is required in order for the client to detect it as a plugin/script.
@Slf4j
@PluginDescriptor(
		name = "420xAuto Wines Basic",
		description = "A Test!",
		enabledByDefault = false,
		tags = {"420xAuto","wines","basic"})
@Extension
public class AutoWines extends LoopedPlugin
{
	@Inject
	private Client client;

	private int winesMade;

	@Override
	protected void startUp() {
		log.info("420xAuto Wines Basic started!");
	}
	@Override
	protected void shutDown() {
		log.info("420xAuto Wines Basic stopped!");
	}
	@Override
	protected int loop() {
		log.info("loop");
		//first check if bank is open to withdraw ingredients
		if (Bank.isOpen()) {
			if (Bank.Inventory.getCount(ItemID.GRAPES) > 0 && Bank.Inventory.getCount(ItemID.JUG_OF_WATER) > 0) {
				Bank.close();
				return -1;
			}
			withdrawIngredients();
			//dont sleep so long after this method call due it already waiting for its completion
			return Rand.nextInt(50,300);
		}

		//ferment wines if we can (method returns true if can ferment)
		if (Inventory.contains(ItemID.GRAPES) && Inventory.contains(ItemID.JUG_OF_WATER)) {
			fermentWine();
			return -1;
		}

		// walk to bank if not near bank and withdraw ingredients
		clickLocalBank();
		return -1;
	}

	/**
	 * withdraws the ingredients. does nothing if no more items in bank. must be called when bank is already open
	 * @return
	 */
	private void withdrawIngredients() {
		// check if we have both required items in bank
		if (!Bank.contains(ItemID.GRAPES) || !Bank.contains(ItemID.JUG_OF_WATER)) {
			//just stop the plugin if no more ingredients in bank
			log.info("No more ingredients to make wines!");
			this.stop();
			return;
		}
		log.info("Start bank deposit + withdraw sequence");
		// simplified bank sequence for simplicity - deposit all items, withdraw both items, wait until have some of both, if so, close bank
		Bank.depositInventory();
		Time.sleep(50,300);
		Bank.withdraw(ItemID.GRAPES, 14, Bank.WithdrawMode.ITEM);
		Time.sleep(50,300);
		Bank.withdraw(ItemID.JUG_OF_WATER, 14, Bank.WithdrawMode.ITEM);
		log.info("End bank sequence, start sleepUntil");
		Time.sleepUntil(() -> Bank.Inventory.getCount(ItemID.GRAPES) > 0 && Bank.Inventory.getCount(ItemID.JUG_OF_WATER) > 0, 100, 3000);
		log.info("end withdraw sleepUntil");
	}


	/**
	 * Clicks local bank if within 3 tiles of nearest BankLocation.
	 * Walks to nearest BankLocation if farther away
	 */
	private void clickLocalBank() {
		BankLocation nearestBankLocation = BankLocation.getNearest();
		if (nearestBankLocation.getArea().distanceTo(Players.getLocal()) > 3) {
			// Here we are far away and need to walk
			if (Movement.isWalking()) {
				// Dont walk while already moving
				return;
			}
			log.info("Walk to local bank: "+nearestBankLocation.name());
			Movement.walkTo(nearestBankLocation);
			return;
		}
		//here we are close to a bank location, get nearest bank in that location with valid actions
		TileObject nearbyBank = TileObjects.getNearest(bank ->
						bank.hasAction("Collect") &&
						nearestBankLocation.getArea().contains(bank) &&
						bank.hasAction("Use","Bank"));
		// check our get method to see if it returned any valid object before interacting it
		if (nearbyBank != null) {
			//check if we can interact it directly or if there is a door in our way
			if (Reachable.isInteractable(nearbyBank)) {
				log.info("Interact with bank object");
				nearbyBank.interact("Use","Bank");
				Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 4000);
				return;
			}

			//here we need to walk to bank directly
			if (!Movement.isWalking()) {
				Movement.walkTo(nearbyBank);
			}

		}
	}
	private Widget getMultiSkillMenu() {
		return Widgets.get(WidgetInfo.MULTI_SKILL_MENU);
	}
	private boolean isMultiSkillMenuOpen() {
		Widget w = getMultiSkillMenu();
		return w != null && w.isVisible();
	}

	/**
	 * Processes the items. Must be called after have both required items in inventory
	 */
	private void fermentWine() {
		Item grapes = Inventory.getFirst(ItemID.GRAPES);
		Item jugs = Inventory.getFirst(ItemID.JUG_OF_WATER);
		if (grapes == null || jugs == null) {
			return;
		}
		if (isMultiSkillMenuOpen()) {
			//grab current count of wines to make
			int toProcessCount = Math.min(Inventory.getCount(ItemID.GRAPES), Inventory.getCount(ItemID.JUG_OF_WATER));
			log.info("Press space to make wines");
			Keyboard.type(" ");
			if (Time.sleepUntil(() -> !Inventory.contains(ItemID.GRAPES, ItemID.JUG_OF_WATER), () -> Players.getLocal().isAnimating(), 100, 4000)) {
				//no more items - sleepUntil returned true - add all wines made from before to wines made counter
				winesMade += toProcessCount;
				log.info("Have made "+winesMade+" wines now!");
				return;
			}
			//timeout - sleepuntil returned false
			return;
		}
		//use items together and wait for skill menu to open
		log.info("Use grapes on jugs of water");
		grapes.useOn(jugs);
		Time.sleepUntil(() -> isMultiSkillMenuOpen(), 100, 4000);
	}

}
