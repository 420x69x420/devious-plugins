package net.unethicalite.scripts.runecrafter;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.launcher420.Log;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.PluginInterface;
import net.unethicalite.scripts.framework.Tree;
import net.unethicalite.scripts.runecrafter.leaves.Crafting;
import net.unethicalite.scripts.runecrafter.leaves.CraftingBranch;
import net.unethicalite.scripts.runecrafter.leaves.HopWorlds;
import net.unethicalite.scripts.tasks.bondup.BondUpBranch;
import net.unethicalite.scripts.tasks.bondup.leaves.BondUp;
import net.unethicalite.scripts.tasks.general.GeneralBranch;
import net.unethicalite.scripts.tasks.general.leaves.*;
import net.unethicalite.scripts.tasks.settings.SettingsBranch;
import net.unethicalite.scripts.tasks.settings.leaves.*;
import org.pf4j.Extension;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


// This annotation is required in order for the client to detect it as a plugin/script.
@Slf4j
@PluginDescriptor(
		name = "420xRunecrafter",
		description = "crafts runecrafting essence!",
		enabledByDefault = false,
		tags = {"420xrunecrafter","runecrafting"})
@Extension
public class RunecrafterPlugin extends Script implements PluginInterface
{
	@Override
	public boolean shouldBondUp() {
		return false;
	}
	@Override
	public boolean setSettings() {
		return true;
	}
	@Override
	public String botGroup() {
		return "crafter"; //no muling implemented for crafter script
	}
	@Inject
	private ConfigManager configManager;
	private final Crafting crafting = new Crafting();
	private final Tree tree = new Tree();
	private void makeTree() {
		tree.addBranches(
				new GeneralBranch().addLeaves(
						new CloseCrashedClient(),
						new Login(),
						new ClickHereToPlay(),
						new HandleBugs(),
						new Tut(),
						new UniqueActions(),
						new SayThanksForEgg(),
						new CacheBank()
				),

				new SettingsBranch().addLeaves(
						new DisableSound(),
						new OpenSettings(),
						new DisableAid(),
						new DisableHopWarning(),
						new DisableGEWarnings(),
						new DisableDropWarning(),
						new DisableTradeDelay(),
						new CloseSettings()
				),

				new BondUpBranch().addLeaves(
						new BondUp()
				),

				new CraftingBranch().addLeaves(
						new ContinueAllDialogs(),
						new HopWorlds(),
						crafting
				)
		);
	}

	@Override
	public void onStart(String... strings) {
		InterfaceInstance.setGlobalPlugin(this);

		makeTree();
		Client client = Static.getClient();
		client.setUnlockedFps(true);
		client.setUnlockedFpsTarget(3);
		Static.getEventBus().register(crafting);
		Log.sendKeyValuePairToOutputStream("BOT_GROUP","runecrafter");
		this.getBlockingEventManager().remove(LoginEvent.class);
	}
	@Override
	public void onStop() {
		while (crafting.ourName != null) {
			if (CrafterFileOperations.removeEntry(crafting.ourName)) {
				crafting.ourName = null;
			}
		}
		Static.getEventBus().unregister(crafting);
	}


	@Override
	protected int loop() {
		return tree.execute();
	}

	@Subscribe
	public void onGameTick(final GameTick e) {
	}


	@Override
	public Plugin plugin() {
		return this;
	}


	@Override
	public boolean isBankCached() {
		return Config.cachedBank;
	}

	@Override
	public void cachedBank() {
		Config.cachedBank = true;
	}

	@Override
	public boolean shouldHandleBugs() {
		return Config.handleBugs;
	}

	@Override
	public void handledBug() {
		Config.handleBugs = false;
	}

	@Override
	public boolean loginF2P() {
		return true;
	}

}
