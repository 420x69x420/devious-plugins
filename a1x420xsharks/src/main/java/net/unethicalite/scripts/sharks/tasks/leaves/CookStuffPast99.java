package net.unethicalite.scripts.sharks.tasks.leaves;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Prices;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.scripts.api.events.*;
import net.unethicalite.scripts.api.utils.Jewelry;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.framework.Leaf;
import net.unethicalite.scripts.sharks.tasks.data.P2PMeat;
import net.unethicalite.scripts.tasks.general.leaves.UniqueActions;

import static net.unethicalite.scripts.sharks.tasks.leaves.CookStuff.haveAnyRawFood;

public class CookStuffPast99 extends Leaf {


    private boolean useCookingGuild = false;
    private final WorldPoint cookGuildRange = new WorldPoint(3146,3452,0);
    private final WorldPoint roguesDenFire = new WorldPoint(3042,4973, 1);
    @Override
    public boolean isValid() {
        return Skills.getLevel(Skill.COOKING) >= 99;
    }
    @Override
    public int execute() {
        useCookingGuild = UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_7);

        //get some teleportation methods to our location
        int leastOwnedChargedWealthIDID = Jewelry.getLeastOwnedChargedWealthID();
        if (leastOwnedChargedWealthIDID == -1) {
            new BuyItemEvent()
                    .setId(ItemID.RING_OF_WEALTH_5)
                    .setPrice((int)(Prices.getItemPrice(ItemID.RING_OF_WEALTH_5) * 1.5))
                    .setAmount(1)
                    .execute();
            return -1;
        }
        if (!Equipment.contains(leastOwnedChargedWealthIDID)) {
            Item invyItem = (Bank.isOpen() ? Bank.Inventory.getFirst(leastOwnedChargedWealthIDID) : Inventory.getFirst(leastOwnedChargedWealthIDID));
            if (invyItem != null) {
                new CloseGEEvent().execute();
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
            Bank.withdraw(leastOwnedChargedWealthIDID, 1, Bank.WithdrawMode.ITEM);
            return -1;
        }
        if (!useCookingGuild) {
            int leastOwnedGamesID = Jewelry.getLeastOwnedGamesNecklaceID();
            if (leastOwnedGamesID == -1) {
                new BuyItemEvent()
                        .setId(ItemID.GAMES_NECKLACE8)
                        .setPrice((int)(Prices.getItemPrice(ItemID.GAMES_NECKLACE8) * 1.5))
                        .setAmount(1)
                        .execute();
                return -1;
            }
            if (!Equipment.contains(leastOwnedGamesID)) {
                Item invyItem = (Bank.isOpen() ? Bank.Inventory.getFirst(leastOwnedGamesID) : Inventory.getFirst(leastOwnedGamesID));
                if (invyItem != null) {
                    new CloseGEEvent().execute();
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
                Bank.withdraw(leastOwnedGamesID, 1, Bank.WithdrawMode.ITEM);
                return -1;
            }
        }

        //get some meat to cook
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
            if (!Inventory.isEmpty()) {
                Bank.depositInventory();
                return -1;
            }
            if (Bank.contains(highestOwnedMaybeNotedMeat.getRawId())) {
                Bank.withdrawAll(highestOwnedMaybeNotedMeat.getRawId(), Bank.WithdrawMode.ITEM);
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
                    return Rand.nextInt(10,50);
                }
            }
            //open bank at banklocation if not raw in invy and none on ground nearby
            if (useCookingGuild) {
                if (!Reachable.isWalkable(cookGuildRange)) {
                    new MovementEvent()
                            .setDestination(cookGuildRange)
                            .setEventCompletedCondition(() -> Reachable.isWalkable(cookGuildRange))
                            .execute();
                    return Rand.nextInt(50,300);
                }
                TileObject bank = TileObjects.getFirstSurrounding(cookGuildRange, 4, "Bank booth");
                if (bank != null) {
                    bank.interact("Bank");
                    Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);
                }
                return Rand.nextInt(1,100);
            }
            if (!Reachable.isWalkable(roguesDenFire)) {
                new MovementEvent()
                        .setDestination(roguesDenFire)
                        .setEventCompletedCondition(() -> roguesDenFire.distanceTo(Players.getLocal()) <= 3)
                        .execute();
                return Rand.nextInt(1,100);
            }
            NPC banker = NPCs.getNearest(x -> roguesDenFire.distanceTo(x) < 3 && x.hasAction("Bank"));
            if (banker != null) {
                banker.interact("Bank");
                Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);
                return Rand.nextInt(10,50);
            }
            new MovementEvent()
                    .setDestination(roguesDenFire)
                    .setEventCompletedCondition(() -> roguesDenFire.distanceTo(Players.getLocal()) <= 3)
                    .execute();
            return -1;
        }

        //have raw in inventory
        //go to cooking spot
        if (useCookingGuild) {
            if (cookGuildRange.distanceTo(Players.getLocal()) != 0) {
                new MovementEvent()
                        .setDestination(cookGuildRange)
                        .execute();
                return Rand.nextInt(10,50);
            }
        } else if (roguesDenFire.distanceTo(Players.getLocal()) != 0) {
            new MovementEvent()
                    .setDestination(roguesDenFire)
                    .execute();
            return Rand.nextInt(10,50);
        }
        //directly next to fire here
        //check if we have more than 1 of any raw food in invy - drop them
        if (CookStuff.shouldDropFood()) {
            CookStuff.dropAllRawExcept1();
            return -1;
        }
        if (useCookingGuild) {
            TileObject range = TileObjects.getFirstSurrounding(cookGuildRange, 2, "Range");
            if (range != null) {
                range.interact("Cook");
                Time.sleepUntil(() -> !Inventory.contains(highestOwnedMaybeNotedMeat.getRawId()), 69, 4000);
            }
            return Rand.nextInt(10,50);
        }
        TileObject fire = TileObjects.getFirstSurrounding(roguesDenFire, 3, "Fire");
        if (fire != null) {

            fire.interact("Cook");
            Time.sleepUntil(() -> !Inventory.contains(highestOwnedMaybeNotedMeat.getRawId()), 69, 4000);
        }
        return Rand.nextInt(10,50);
    }
}
