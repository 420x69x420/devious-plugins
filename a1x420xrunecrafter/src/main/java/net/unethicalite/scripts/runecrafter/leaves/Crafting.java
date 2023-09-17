package net.unethicalite.scripts.runecrafter.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.scripts.api.events.HopWorldEvent;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.api.events.OpenBankEvent;
import net.unethicalite.scripts.api.extended.ExBank;
import net.unethicalite.scripts.api.extended.ExInvy;
import net.unethicalite.scripts.api.launcher420.Log;
import net.unethicalite.scripts.api.utils.Sleep;
import net.unethicalite.scripts.framework.Leaf;
import net.unethicalite.scripts.runecrafter.Altar;
import net.unethicalite.scripts.runecrafter.CrafterFileOperations;
import net.unethicalite.scripts.tasks.general.leaves.Login;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
@Slf4j
public class Crafting extends Leaf {

    private final Set<String> runners = new HashSet<>();
    private final ConcurrentLinkedQueue<String> runnersToTradeQueue = new ConcurrentLinkedQueue<>();
    public String ourName = null;
    private static String lastRunnerToTrade = null;
    public static Altar altar = Altar.AIR;
    public static int craftWorld = 301;
    public boolean runnerBusy  = false;
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int execute() {
        runnerBusy = false;
        if (Skills.getLevel(Skill.RUNECRAFT) >= 91) {
            Log.sendKeyValuePairToOutputStream("BOT_GROUP","nature_runecrafter");
            System.exit(0);
            return 100;
        }
        if (ourName == null) {
            ourName = Players.getLocal().getName();
            if (CrafterFileOperations.writeEntry(ourName)) {
            } else {
                ourName = null;
            }
        }
        //read file every loop for which altar to craft at and world to hop to
        CrafterFileOperations.updateAllSettings();
        //hop back to crafter world
        if (craftWorld != Worlds.getCurrentId()) {
            new HopWorldEvent(craftWorld).execute();
        }
        if (Dialog.canContinue()) {
            Dialog.continueSpace();
            return -1;
        }
        int otherTiara = (altar.tiara == ItemID.AIR_TIARA ? ItemID.BODY_TIARA : ItemID.AIR_TIARA);
        //equip tiara if have one unequipped outside of bank
        if (!Trade.isOpen() && (!Equipment.contains(altar.tiara) || Inventory.contains(otherTiara))) {
            if (Bank.isOpen()) {
                if (Bank.Inventory.getCount(altar.tiara) > 1) {
                    Bank.depositInventory();
                    return Sleep.returnTick();
                }
                if (Bank.Inventory.getCount(altar.tiara) == 1) {
                    Item tiara = Bank.Inventory.getFirst(altar.tiara);
                    if (tiara != null) {
                        tiara.interact("Wear");
                    }
                }
                if (!ExInvy.containsOnly(ItemID.PURE_ESSENCE, altar.tiara)) {
                    Bank.depositInventory();
                    return Sleep.returnTick();
                }
                if (Bank.contains(altar.tiara)) {
                    Bank.withdraw(altar.tiara, 1, Bank.WithdrawMode.ITEM);
                } else {
                    log.info("Not have any tiara in bank: "+altar);
                }
                return Sleep.returnTick();
            }

            Item tiara = Inventory.getFirst(altar.tiara);
            if (tiara != null) {
                tiara.interact("Wear");
            }
            if (inAnAltar()) {
                return exitPortal();
            }
            new OpenBankEvent().execute();
            return Sleep.returnTick();
        }
        //inside altar
        if (altar.altarTile.distanceTo(Players.getLocal()) < 40) {
            if (Inventory.contains(ItemID.PURE_ESSENCE)) {
                TileObject altarObj = TileObjects.getNearest(g -> g.getName().equals("Altar") && g.hasAction("Craft-rune"));
                if (altarObj != null) {
                    altarObj.interact("Craft-rune");
                    Time.sleepUntil(() -> !Inventory.contains(ItemID.PURE_ESSENCE), () -> Players.getLocal().isMoving() || Players.getLocal().isAnimating(), 100, 3000);
                    Time.sleepTick();
                }
                return Sleep.returnTick();
            }
            List<String> validRunners = CrafterFileOperations.readAllRunners();
            for (String s : validRunners) {
                s = Text.toJagexName(s);
                if (!s.isEmpty() && !s.equalsIgnoreCase("null") && !runners.contains(s)) {
                    runners.add(s);
                }
            }
            Player foundRunner = Players.getNearest(p -> runnersToTradeQueue.contains(Text.toJagexName(p.getName())));
            //no runners found that request to trade with us
            if (foundRunner == null || foundRunner.getName() == null) {
                if (!runnersToTradeQueue.isEmpty()) {
                    log.info("Clearing some mis-handled runner queue names still existing when no runners here");
                    runnersToTradeQueue.clear();
                }
                return Sleep.returnShort();
            }
            if (Trade.isOpen()) {
                log.debug("Trade open");
                String tradingPlayer = Text.toJagexName(Trade.getTradingPlayer());
                if (!runnersToTradeQueue.contains(tradingPlayer)) {
                    log.debug("Not trading correct player - declining, current: "+tradingPlayer+" and runnerToTradeQueue: "+runnersToTradeQueue);
                    Trade.decline();
                    return Sleep.returnTick();
                }
                lastRunnerToTrade = tradingPlayer;
                //accept everything in trades blindy
                if (!Trade.hasAccepted(false)) {
                    log.debug("Accepting trade");
                    Trade.accept();
                }
                return Sleep.returnShort();
            }
            String name = foundRunner.getName();
            log.debug("Interact 'Trade with' on "+name);
            foundRunner.interact("Trade with");
            Time.sleepTicksUntil(() -> Trade.isOpen(), 2);
            if (!Trade.isOpen()) {
                runnersToTradeQueue.remove(Text.toJagexName(name));
            }
            return Sleep.returnTick();
        }

        //near outside entrance
        if (altar.entranceTile.distanceTo(Players.getLocal().getWorldLocation()) <= 15) {
            TileObject entrance = TileObjects.getNearest(g -> g.getName().equals("Mysterious ruins") && g.hasAction("Enter"));
            if (entrance != null && Reachable.isInteractable(entrance)) {
                entrance.interact("Enter");
                Time.sleepUntil(() -> altar.altarTile.distanceTo(Players.getLocal()) < 40, () -> Players.getLocal().isMoving(), 100, 3000);
                return Sleep.returnShort();
            }
        }

        //somewhere in the world not where we want to be
        if (Movement.isWalking()) {
            if (inAnAltar()) {
                return exitPortal();
            }
            new MovementEvent()
                    .setDestination(altar.entranceTile)
                    .setRadius(15)
                    .execute();
            return Sleep.returnShort();
        }

        return Sleep.returnShort();
    }

    public boolean inAnAltar() {
        return Altar.AIR.altarTile.distanceTo(Players.getLocal()) < 40 || Altar.BODY.altarTile.distanceTo(Players.getLocal()) < 40;
    }
    public int exitPortal() {
        TileObject exitPortal = TileObjects.getNearest(t -> t.hasAction("Use") && t.getName().equals("Portal"));
        if (exitPortal != null) {
            if (exitPortal.distanceTo(Players.getLocal()) > 15) {
                if (!Movement.isWalking()) {
                    Movement.walkTo(exitPortal);
                    return Sleep.returnTick();
                }
            } else {
                exitPortal.interact("Use");
                Time.sleepUntil(() -> TileObjects.getNearest(t -> t.hasAction("Use") && t.getName().equals("Portal")) == null, () -> Players.getLocal().isMoving(), 100, 4000);
            }
        }
        return Sleep.returnShort();
    }
    @Subscribe
    public void onChatMessage(final ChatMessage msg) {
        String name = Text.toJagexName(msg.getName());
        if (msg.getType().equals(ChatMessageType.TRADEREQ)) {
            if (runners.contains(name) && !runnersToTradeQueue.contains(name)) {
                runnersToTradeQueue.add(name);
            }
        }
        if (msg.getType().equals(ChatMessageType.TRADE)) {
            if (msg.getMessage().equals("Accepted trade.") || msg.getMessage().equals("Other player declined trade.")) {
                runnersToTradeQueue.remove(lastRunnerToTrade);
                lastRunnerToTrade = null;
            }
        }
        if (msg.getType().equals(ChatMessageType.TRADE)) {
            if (msg.getMessage().contains("is busy at the moment.")) {
                runnersToTradeQueue.remove(name);
                runnerBusy = true;
            }
        }
    }
}
