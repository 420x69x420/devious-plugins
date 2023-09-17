package net.unethicalite.scripts.sharks.tasks.leaves;

import net.runelite.api.*;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Prices;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.scripts.api.utils.Jewelry;
import net.unethicalite.scripts.sharks.SharksPlugin;
import net.unethicalite.scripts.api.events.*;
import net.unethicalite.scripts.api.extended.ExBank;
import net.unethicalite.scripts.api.extended.ExInvy;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.framework.Leaf;
import net.unethicalite.scripts.sharks.tasks.data.P2PMeat;
import net.unethicalite.scripts.sharks.tasks.data.RangeLocation;
import net.unethicalite.scripts.tasks.general.leaves.UniqueActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.unethicalite.scripts.sharks.tasks.data.RangeLocation.*;

public class CookStuff extends Leaf {


    private RangeLocation rangeLocation = null;
    @Override
    public boolean isValid() {
        return true;
    }
    @Override
    public int execute() {
        //get some teleportation methods to our location
        int leastOwnedChargedWealthID = Jewelry.getLeastOwnedChargedWealthID();
        if (leastOwnedChargedWealthID == -1) {
            new BuyItemEvent()
                    .setId(ItemID.RING_OF_WEALTH_5)
                    .setPrice((int)(Prices.getItemPrice(ItemID.RING_OF_WEALTH_5) * 1.5))
                    .setAmount(1)
                    .execute();
            return -1;
        }
        if (!Equipment.contains(leastOwnedChargedWealthID)) {
            Item invyItem = (Bank.isOpen() ? Bank.Inventory.getFirst(leastOwnedChargedWealthID) : Inventory.getFirst(leastOwnedChargedWealthID));
            if (invyItem != null) {
                if (GrandExchange.isOpen()) {
                    GrandExchange.close();
                    return -1;
                }
                invyItem.interact("Wear");
                return -1;
            }
            if (!Bank.isOpen()) {
                new OpenBankEvent()

                        .execute();
                return -1;
            }
            if (Inventory.isFull()) {
                Bank.depositInventory();
                return -1;
            }
            Bank.withdraw(leastOwnedChargedWealthID, 1, Bank.WithdrawMode.ITEM);
            return -1;
        }
        if (rangeLocation == null) {
            if (UniqueActions.isActionAbovePercent(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_7, 70)) {
                rangeLocation = AL_KHARID;
            } else if (UniqueActions.isActionAbovePercent(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_7, 34)) {
                rangeLocation = EDGEVILLE;
            } else {
                rangeLocation = CATHERBY;
            }
        }
        switch(rangeLocation) {
            case CATHERBY:{
                if (CATHERBY.bankLocation.getArea().distanceTo(Players.getLocal()) > 70) {
                    if (!OwnedItems.contains(ItemID.CATHERBY_TELEPORT)) {
                        new BuyItemEvent()
                                .setId(ItemID.CATHERBY_TELEPORT)
                                .setPrice((int)(Prices.getItemPrice(ItemID.CATHERBY_TELEPORT) * 1.5))
                                .setAmount(Rand.nextInt(7,14))
                                .execute();
                    }

                    if (Bank.Inventory.getCount(ItemID.CATHERBY_TELEPORT) == 0 || Inventory.getCount(ItemID.CATHERBY_TELEPORT) == 0) {
                        if (!Bank.isOpen()) {
                            new OpenBankEvent().execute();
                            return -1;
                        }
                        BankWithdrawInventoryEvent event = new BankWithdrawInventoryEvent()
                                .addItem(ItemID.CATHERBY_TELEPORT,1);
                        if (!event.fulfilled()) {
                            event.execute();
                        }
                        return -1;
                    }
                }
            }
            break;
            case AL_KHARID: case EDGEVILLE:{
                int leastOwnedGloryID = Jewelry.getLeastOwnedChargedGloryID();
                if (leastOwnedGloryID == -1) {
                    new BuyItemEvent()
                            .setId(ItemID.AMULET_OF_GLORY6)
                            .setPrice((int)(Prices.getItemPrice(ItemID.AMULET_OF_GLORY6) * 1.5))
                            .setAmount(1)
                            .execute();
                    return -1;
                }
                if (!Equipment.contains(leastOwnedGloryID)) {
                    Item invyItem = (Bank.isOpen() ? Bank.Inventory.getFirst(leastOwnedGloryID) : Inventory.getFirst(leastOwnedGloryID));
                    if (invyItem != null) {
                        if (GrandExchange.isOpen()) {
                            GrandExchange.close();
                            return -1;
                        }
                        invyItem.interact("Wear");
                        return -1;
                    }
                    if (!Bank.isOpen()) {
                        new OpenBankEvent().execute();
                        return -1;
                    }
                    if (Inventory.isFull()) {
                        Bank.depositInventory();
                        return -1;
                    }
                    Bank.withdraw(leastOwnedGloryID, 1, Bank.WithdrawMode.ITEM);
                    return -1;
                }
            }
        }

        //get the highest meat to cook
        P2PMeat highestOwnedMaybeNotedMeat = P2PMeat.getHighestOwnedRawMeatIncludingNoted();
        //check if we have any noted in inventory via discrepancy in noted  / non noted owned items count
        if (!OwnedItems.contains(highestOwnedMaybeNotedMeat.getRawId())) {
            new BankInventoryEvent().execute();
            return -1;
        }

        //withdraw all raw food if bank is open
        if (Bank.isOpen()) {
            if (Bank.Inventory.getCount(highestOwnedMaybeNotedMeat.getRawId()) > 0) {
                Bank.close();
                return -1;
            }
            List<Integer> approvedIds = new ArrayList<>();
            if (rangeLocation == CATHERBY && rangeLocation.bankLocation.getArea().distanceTo(Players.getLocal()) > 70) {
                approvedIds.add(ItemID.CATHERBY_TELEPORT);
            }
            approvedIds.add(highestOwnedMaybeNotedMeat.getRawId());
            if (!ExInvy.containsOnly(approvedIds)) {
                ExBank.depositAllExcept(approvedIds);
                return -1;
            }
            if (Bank.contains(highestOwnedMaybeNotedMeat.getRawId())) {
                Bank.withdrawAll(highestOwnedMaybeNotedMeat.getRawId(), Bank.WithdrawMode.ITEM);
            }
            return -1;
        }

        if (rangeLocation == CATHERBY && rangeLocation.bankLocation.getArea().distanceTo(Players.getLocal()) > 70) {
            if (GrandExchange.isOpen()) {
                GrandExchange.close();
                return -1;
            }
            Item teleTab = null;
            if (Bank.isOpen()) {
                teleTab = Bank.Inventory.getFirst(ItemID.CATHERBY_TELEPORT);
            } else {
                teleTab = Inventory.getFirst(ItemID.CATHERBY_TELEPORT);
            }
            if (teleTab != null) {
                teleTab.interact("Break");
                Time.sleepUntil(() -> rangeLocation.bankLocation.getArea().distanceTo(Players.getLocal()) <= 70, ()-> Players.getLocal().isAnimating(), 100,3000);
            }
            return -1;
        }
        //no more food
        if (!haveAnyRawFood()) {
            //pickup more food if we can
            if (!Inventory.isFull()) {
                TileItem nearbyFoodOnGround = TileItems.getFirstAt(Players.getLocal().getWorldLocation(), x -> P2PMeat.sortedMeats.stream().anyMatch(i -> i.getRawId() == x.getId()));
                if (nearbyFoodOnGround != null) {
                    int expectedID = nearbyFoodOnGround.getId();
                    nearbyFoodOnGround.pickup();
                    Time.sleepUntil(() -> Inventory.contains(expectedID), 69, 2000);
                    return Rand.nextInt(1,100);
                }
            }
            //open bank at banklocation if not raw in invy and none on ground nearby
            new OpenBankEvent()
                    .setBankLocation(rangeLocation.bankLocation)
                    .execute();
            return Rand.nextInt(1,100);
        }

        //have raw in inventory
        //go to cooking spot
        if (rangeLocation.rangeTile.distanceTo(Players.getLocal()) != 0) {
            new MovementEvent()
                    .setDestination(rangeLocation.rangeTile)
                    .execute();
            return -1;
        }
        //check if we have more than 1 of any raw food in invy - drop them
        if (shouldDropFood()) {
            dropAllRawExcept1();
            return -1;
        }
        TileObject fire = TileObjects.getFirstSurrounding(rangeLocation.rangeTile, 3, x -> x.getName().equals("Stove") || x.getName().equals("Range"));
        if (fire != null) {
            fire.interact("Cook");
            Time.sleepUntil(() -> !Inventory.contains(highestOwnedMaybeNotedMeat.getRawId()), 69, 4000);
        }
        return Rand.nextInt(1,100);
    }
    public static boolean haveAnyRawFood() {
        for (P2PMeat sortedMeat : P2PMeat.sortedMeats) {
            if (Inventory.contains(sortedMeat.getRawId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldDropFood() {
        HashMap<Integer, Integer> itemCount = new HashMap<>();
        for (Item i : Inventory.getAll()) {
            for (P2PMeat sortedMeat : P2PMeat.sortedMeats) {
                int rawId = sortedMeat.getRawId();
                if (i.getId() == rawId) {
                    itemCount.put(rawId, itemCount.getOrDefault(rawId, 0) + 1);
                    break;
                }
            }
        }

        for (int count : itemCount.values()) {
            if (count > 1) {
                return true;
            }
        }
        return false;
    }

    static void dropAllRawExcept1() {
        if (!shouldDropFood()) {
            return;
        }

        int highestRawId = P2PMeat.getHighestOwnedRawMeatIncludingNoted().getRawId();
        for (P2PMeat sortedMeat : P2PMeat.sortedMeats) {
            highestRawId = sortedMeat.getRawId();  // Assume sortedMeats is sorted in descending order of levels
        }
        int itemsDroppedThisTick = 0;
        for (Item i : Inventory.getAll()) {
            if (!Plugins.isEnabled(new SharksPlugin())) {
                break;
            }
            for (P2PMeat sortedMeat : P2PMeat.sortedMeats) {
                int rawId = sortedMeat.getRawId();
                if (i == null) {
                    break;
                }
                if (i.getId() != sortedMeat.getRawId()) {
                    continue;
                }
                if (itemsDroppedThisTick >= 10) {
                    Time.sleepTick();
                    itemsDroppedThisTick = 0;
                }
                if (rawId == highestRawId) {
                    highestRawId = -1;  // Skip dropping the highest level item only once
                } else {
                    i.drop();
                    itemsDroppedThisTick++;
                    break;
                }
            }
        }

    }


}
