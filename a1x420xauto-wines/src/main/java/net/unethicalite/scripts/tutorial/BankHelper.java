package net.unethicalite.scripts.tutorial;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class BankHelper {
    public static int withdrawItemFromBank(String name, int quantity) {
        if (!Bank.isOpen()) {
            return clickLocalBank();
        }

        if (Inventory.getCount(name) < quantity) {
            int toWithdrawQty = quantity - Inventory.getCount(name);
            bankWithdraw(name,toWithdrawQty, Bank.WithdrawMode.ITEM);
        }
        return API.returnTick();
    }
    public static int withdrawItemFromBank(int id, int quantity) {
        if (!Bank.isOpen()) {
            return clickLocalBank();
        }

        if (Inventory.getCount(id) < quantity) {
            int toWithdrawQty = quantity - Inventory.getCount(id);
            bankWithdraw(id,toWithdrawQty, Bank.WithdrawMode.ITEM);
        }
        return API.returnTick();
    }
    public static int clickLocalBank() {
        if (Worlds.getCurrentWorld().isMembers()) {
            return clickBank(BankLocation.getNearest());
        }
        BankLocation nearest = Arrays.stream(API.f2pBanks)
                .min(Comparator.comparingInt(x -> x.getArea().distanceTo2D(Players.getLocal().getWorldLocation())))
                .orElse(null);
        return clickBank(nearest);
    }

    public static int clickBank(BankLocation location) {
        if (!API.shouldWalk()) {
            return API.shortReturn();
        }
        TileObject booth = TileObjects.getNearest(b -> b.hasAction("Collect") && (b.hasAction("Bank") || b.hasAction("Use")) && location.getArea().distanceTo(b) < 5);

        if (booth != null) {
            if (booth.distanceTo(Players.getLocal()) >= 20 || !Reachable.isInteractable(booth)) {
                log.info("Walking towards booth");
                if (API.shouldWalk()) {
                    API.walkTo(booth.getWorldArea());
                }
                return API.returnTick();
            }
            log.info("Clicking Booth");
            String action = (booth.hasAction("Bank") ? "Bank" : "Use");
            if (API.shouldWalk()) {
                booth.interact(action);
                Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);
            }
            return API.returnTick();
        }
        NPC banker = NPCs.getNearest(b -> b.hasAction("Collect") && b.hasAction("Bank")  && location.getArea().distanceTo(b) < 5);
        if (banker != null) {
            if (banker.distanceTo(Players.getLocal()) < 20 && Reachable.isInteractable(banker)) {
                log.info("Clicking Banker");
                banker.interact("Bank");
                Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);
                return API.shortReturn();
            }

            log.info("Walking towards banker");
            if (API.shouldWalk()) {
                API.walkTo(banker.getWorldArea());
            }
            return API.returnTick();
        }
        log.info("Walking towards bank");
        if (API.shouldWalk()) {
            API.walkTo(location.getArea());
        }
        return API.returnTick();
    }

    public static void depositAllExcept(int... ids) {
        List<Integer> itemsDeposited = new ArrayList<>();
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !Arrays.stream(ids).anyMatch(exceptId -> exceptId == i.getId()))) {
            if (API.stopScript) {
                break;
            }
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: "+ i.getName());
            Bank.depositAll(i.getId());
            API.shortSleep();
        }
    }

    public static void depositAllExcept(String... names) {
        List<Integer> itemsDeposited = new ArrayList<>();
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !Arrays.stream(names).anyMatch(exceptName -> exceptName.equals(i.getName())))) {
            if (API.stopScript) {
                break;
            }
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: "+ i.getName());
            Bank.depositAll(i.getId());
            API.shortSleep();
        }
    }

    private static String getAction(Item item, int amount, Boolean withdraw) {
        String action = withdraw ? "Withdraw" : "Deposit";
        if (amount == 1) {
            action = action + "-1";
        } else if (amount == 5) {
            action = action + "-5";
        } else if (amount == 10) {
            action = action + "-10";
        } else if (withdraw && amount >= item.getQuantity()) {
            action = action + "-All";
        } else if (!withdraw && amount >= Bank.Inventory.getCount(true, item.getId())) {
            action = action + "-All";
        } else if (item.hasAction(new String[]{action + "-" + amount})) {
            action = action + "-" + amount;
        } else {
            action = action + "-X";
        }

        return action;
    }

    public static void bankWithdraw(int id, int qty, Bank.WithdrawMode withdrawMode) {
        Item item = Bank.getFirst((x) -> {
            return x.getId() == id && !x.isPlaceholder();
        });
        if (item != null) {
            String action = getAction(item, qty, true);
            int actionIndex = item.getActionIndex(action);
            if (withdrawMode == Bank.WithdrawMode.NOTED && !Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(true);
                Time.sleepUntil(Bank::isNotedWithdrawMode, 1200);
            }

            if (withdrawMode == Bank.WithdrawMode.ITEM && Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(false);
                Time.sleepUntil(() -> {
                    return !Bank.isNotedWithdrawMode();
                }, 1200);
            }

            item.interact(actionIndex + 1);
            if (action.equals("Withdraw-X")) {
                Dialog.enterAmount(qty);
                Time.sleepTick();
            }

        }
    }

    public static void bankWithdraw(String name, int qty, Bank.WithdrawMode withdrawMode) {
        Item item = Bank.getFirst(x -> x.getName().equalsIgnoreCase(name) && !x.isPlaceholder());
        if (item != null) {
            String action = getAction(item, qty, true);
            int actionIndex = item.getActionIndex(action);
            if (withdrawMode == Bank.WithdrawMode.NOTED && !Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(true);
                Time.sleepUntil(Bank::isNotedWithdrawMode, 1200);
            }

            if (withdrawMode == Bank.WithdrawMode.ITEM && Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(false);
                Time.sleepUntil(() -> {
                    return !Bank.isNotedWithdrawMode();
                }, 1200);
            }

            item.interact(actionIndex + 1);
            if (action.equals("Withdraw-X")) {
                Dialog.enterAmount(qty);
                Time.sleepTick();
            }

        }
    }

    public static void closeBankTutorial() {
        if (Bank.isOpen()) {
            Widget w = Widgets.get(664,29,0);
            if (w != null && w.isVisible()) {
                w.interact("Close");
                Time.sleepTick();
                return;
            }
            w = Widgets.get(289,7);
            if (w != null && w.isVisible()) {
                w.interact("Not now");
                Time.sleepTick();
            }
        }
    }

    public static void depositItem(int itemID, int quantity) {
        if (Bank.Inventory.getCount(true, itemID) <= 0) {
            log.debug("Missing itemID from invy in queue, skipping: "+itemID);
            return;
        }

        //Withdraw by directly interacting for any clickable-action quantities and return immediately
        Item invyItem = Bank.Inventory.getFirst(itemID);
        int foundActionIndex = -1;
        int currentIndex = 0;
        for (String action : invyItem.getActions()) {
            if (currentIndex <= 0) {
                currentIndex++;
                continue;
            }
            if (action.contains("Deposit-")) {
                if (action.equals("Deposit-X")) {
                    currentIndex++;
                    continue;
                }
                if (action.equals("Deposit-All")) {
                    if (quantity != 1 && quantity != 5 && quantity != 10) {
                        log.debug("Invy count of: " +invyItem.getName() +" not 1, 5, or 10, so withdraw-x");
                        foundActionIndex = currentIndex;
                        break;
                    }
                    currentIndex++;
                    continue;
                }
                int availableActionQty = Integer.parseInt(action.replace("Deposit-",""));
                if (availableActionQty == quantity) {
                    log.debug("found action index with quantity: "+availableActionQty+" equal to deposit desired qty: "+quantity);
                    foundActionIndex = currentIndex;
                    break;
                }
            }
            currentIndex++;
        }
        if (foundActionIndex > -1) {
            log.info("Interacting action index "+foundActionIndex+" correlating to action: "+invyItem.getActions()[foundActionIndex]+" on item: "+invyItem.getName());
            invyItem.interact(foundActionIndex);
            return;
        }

        //Call API method to handle withdraw-x quantities and wait for finish
        log.debug("Deposit-x of item/qty: "+invyItem.getName()+"/"+quantity);
        Bank.deposit(itemID, quantity);
    }
}
