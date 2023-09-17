package net.unethicalite.scripts.sharks.tasks.leaves;

import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.scripts.api.events.*;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.framework.Leaf;

public class GetCookingCape extends Leaf {
    private final WorldPoint cookingGuy = new WorldPoint(3142,3449,0);
    @Override
    public boolean isValid() {
        return Skills.getLevel(Skill.COOKING) >= 99 && !Equipment.contains(ItemID.COOKING_CAPE, ItemID.COOKING_CAPET);
    }
    @Override
    public int execute() {
        int ownedCapeID = -1;
        if (OwnedItems.contains(ItemID.COOKING_CAPET)) {
            ownedCapeID = ItemID.COOKING_CAPET;
        } else if (OwnedItems.contains(ItemID.COOKING_CAPE)) {
            ownedCapeID = ItemID.COOKING_CAPE;
        }
        //have a cape already, just not equipped it yet
        if (ownedCapeID != -1) {
            //interact it in inventory
            Item capeInInvy = (Bank.isOpen() ? Bank.Inventory.getFirst(ownedCapeID) : Inventory.getFirst(ownedCapeID));
            if (capeInInvy != null) {
                capeInInvy.interact("Wear");
                return -1;
            }
            //get it from bank
            if (Bank.contains(ownedCapeID)) {
                new BankWithdrawInventoryEvent().addItem(ownedCapeID, 1).execute();
                return -1;
            }
        }

        //mule over some gold if we need some to start
        if (OwnedItems.getCount(true, ItemID.COINS_995) < 100_000) {
            new MuleEvent()
                    .addRequiredItem(ItemID.COINS_995, 100_000 - OwnedItems.getCount(true, ItemID.COINS_995) + Rand.nextInt(1,1000))
                    .execute();
            return 100;
        }
        if (!Equipment.contains(ItemID.CHEFS_HAT)) {
            Item chefsHat = (Bank.isOpen() ? Bank.Inventory.getFirst(ItemID.CHEFS_HAT) : Inventory.getFirst(ItemID.CHEFS_HAT));
            if (chefsHat != null) {
                new CloseGEEvent().execute();
                chefsHat.interact("Wear");
                return -1;
            }
            if (Bank.contains(ItemID.CHEFS_HAT)) {
                new BankWithdrawInventoryEvent()
                        .addItem(ItemID.CHEFS_HAT, 1)
                        .execute();
                return -1;
            }
            new BuyItemEvent()
                    .setId(ItemID.CHEFS_HAT)
                    .setAmount(1)
                    .execute();
            return -1;
        }
        //withdraw coins if not enough in our invy or too much stuff in inventory
        if (Inventory.getCount(true, ItemID.COINS_995) < 100_000 || Inventory.getFreeSlots() < 2) {
            new BankWithdrawInventoryEvent().addItem(ItemID.COINS_995, 100_000).execute();
            return -1;
        }


        //go to cooking guy
        if (Reachable.isWalkable(cookingGuy)) {
            //handle dialog options
            if (Dialog.isViewingOptions()) {
                if (Dialog.chooseOption("Yes please.") ||
                        Dialog.chooseOption("Sure.") ||
                        Dialog.chooseOption("Skillcape")) {
                    System.out.println("Chose dialog option towards cooking skillcape");
                } else {
                    System.out.println("Unscripted dialog options!");
                    Dialog.getOptions().stream().forEach(w -> System.out.println("[OPTION] "+w.getText()));
                }
                return -1;
            }

            NPC headChef = NPCs.getNearest(x -> x.getName().equals("Head chef") && x.getWorldLocation().distanceTo(cookingGuy) < 8);
            if (headChef != null) {
                headChef.interact("Talk-to");
                Time.sleepUntil(() -> Dialog.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);
                return 100;
            }
        }

        new MovementEvent()
                .setDestination(cookingGuy)
                .setEventCompletedCondition(() -> Reachable.isWalkable(cookingGuy))
                .execute();
        return -1;
    }

}
