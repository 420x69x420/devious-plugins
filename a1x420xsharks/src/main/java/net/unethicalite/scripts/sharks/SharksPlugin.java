package net.unethicalite.scripts.sharks;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.PluginInterface;
import net.unethicalite.scripts.api.launcher420.Log;
import net.unethicalite.scripts.framework.Tree;
import net.unethicalite.scripts.sharks.tasks.leaves.*;
import net.unethicalite.scripts.tasks.bondup.BondUpBranch;
import net.unethicalite.scripts.tasks.bondup.leaves.BondUp;
import net.unethicalite.scripts.tasks.general.GeneralBranch;
import net.unethicalite.scripts.tasks.general.leaves.*;
import net.unethicalite.scripts.sharks.tasks.MethodBranch;
import net.unethicalite.scripts.tasks.settings.SettingsBranch;
import net.unethicalite.scripts.tasks.settings.leaves.*;
import org.pf4j.Extension;

@PluginDescriptor(name = "Sharks")
@Extension
public class SharksPlugin extends Script implements PluginInterface {

    @Override
    public Plugin plugin() {
        return this;
    }
    @Override
    public boolean shouldBondUp() {
        return true; //p2p script?
    }
    @Override
    public boolean setSettings() {
        return true; //want the script to customize some settings?
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
        return Config.loginF2P;
    }

    @Override
    public String botGroup() {
        return "p2p"; //this tells the mule server what group(s) of mules the bot should mule to - comma-seperated String if multiple groups
    }

    private final Tree tree = new Tree();

    private void makeTree() {
        tree.addBranches(
                new GeneralBranch().addLeaves(
                        new Login(),
                        new ClickHereToPlay(),
                        new ContinueAllDialogs(),
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

                new MethodBranch().addLeaves(
                        new HopWorlds(),
                        new GetCookingCape(),
                        new Restock(),
                        new CookStuffPast99(),
                        //new WinesTo99(),
                        new CookStuff()
                )
        );
    }

    @Override
    public void onStart(String... args) {
        //give our script context to API for required inter-dependent fields
        //plugin officially depends on API, and API depends on plugin but not officially, isntead thru this interface
        InterfaceInstance.setGlobalPlugin(this);

        makeTree();

        if (!Bank.isEmpty()) {
            Config.cachedBank = true;
        }

        //send update for 420's launcher acc credentials managing
        Log.sendKeyValuePairToOutputStream("BOT_GROUP","sharks");

        //remove the standard login manager for our own copy of it with ban/lock/update client closing + 420 acc credential management
        this.getBlockingEventManager().remove(LoginEvent.class);
    }

    @Override
    public int loop() {
        return tree.execute();
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {

    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType().equals(ChatMessageType.GAMEMESSAGE) && chatMessage.getMessage().contains("Please finish what you")) {
            System.out.println("I should handle bugs");
            Config.handleBugs = true;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        String loginMessage = Static.getClient().getLoginMessage();
        if (loginMessage.contains("You need a members account to login to this world. Please subscribe, or use a different world")) {
            Config.loginF2P = true;
        }
        if (loginMessage.contains("To access this free world, log into")) {
            Config.loginF2P = false;
        }
    }

}
