package net.unethicalite.scripts.sharks.tasks.leaves;

import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.game.Prices;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.scripts.api.events.BuyItemEvent;
import net.unethicalite.scripts.api.events.MuleEvent;
import net.unethicalite.scripts.api.events.SellItemEvent;
import net.unethicalite.scripts.api.muling.OfferedItem;
import net.unethicalite.scripts.api.muling.RequiredItem;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.framework.Leaf;
import net.unethicalite.scripts.sharks.tasks.data.P2PMeat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Restock extends Leaf {
    private final int[] wineSuppliesIDs = {
            ItemID.GRAPES,
            ItemID.JUG_OF_WATER,
            ItemID.JUG,
            ItemID.JUG_OF_WINE
    };
    @Override
    public boolean isValid() {
        /*if (Skills.getLevel(Skill.COOKING) >= 35 && Skills.getLevel(Skill.COOKING) < 99) {
            return OwnedItems.getCountIncludingNoted(ItemID.GRAPES) == 0 || OwnedItems.getCountIncludingNoted(ItemID.JUG_OF_WATER) == 0;
        }*/
        return P2PMeat.getHighestOwnedRawMeatIncludingNoted() == null;
    }

    @Override
    public int execute() {
        if (GrandExchange.isOpen() && !GrandExchange.isEmpty()) {
            if (GrandExchange.canCollect()) {
                GrandExchange.collect(false);
            }
            return -1;
        }
        //mule over some gold if we need some to start
        List<OfferedItem> potentialItemsToMule = new ArrayList<>();
        for (int i : P2PMeat.allBurntIDs) {
            if (OwnedItems.containsIncludingNoted(i)) {
                potentialItemsToMule.add(new OfferedItem(i, OwnedItems.getCountIncludingNoted(i)));
            }
        }
        //collect list of potential items to mule when all out of things to cook
        if (OwnedItems.containsIncludingNoted(ItemID.CHAOTIC_HANDEGG)) {
            potentialItemsToMule.add(new OfferedItem(ItemID.CHAOTIC_HANDEGG, OwnedItems.getCountIncludingNoted(ItemID.CHAOTIC_HANDEGG)));
        }
        if (OwnedItems.containsIncludingNoted(ItemID.HOLY_HANDEGG)) {
            potentialItemsToMule.add(new OfferedItem(ItemID.HOLY_HANDEGG, OwnedItems.getCountIncludingNoted(ItemID.HOLY_HANDEGG)));
        }
        if (OwnedItems.containsIncludingNoted(ItemID.PEACEFUL_HANDEGG)) {
            potentialItemsToMule.add(new OfferedItem(ItemID.PEACEFUL_HANDEGG, OwnedItems.getCountIncludingNoted(ItemID.PEACEFUL_HANDEGG)));
        }
        List<Integer> itemsToSell = new ArrayList<>();
        for (P2PMeat m : P2PMeat.values()) {
            if (OwnedItems.containsIncludingNoted(m.getRawId())) {
                itemsToSell.add(m.getRawId());
            }
            if (OwnedItems.containsIncludingNoted(m.getCookedId())) {
                itemsToSell.add(m.getCookedId());
            }
            for (int i : wineSuppliesIDs) {
                if (OwnedItems.containsIncludingNoted(i)) {
                    itemsToSell.add(i);
                }
            }
        }
        if (OwnedItems.containsIncludingNoted(ItemID.RING_OF_WEALTH)) {
            itemsToSell.add(ItemID.RING_OF_WEALTH);
        }
        if (OwnedItems.containsIncludingNoted(ItemID.AMULET_OF_GLORY)) {
            itemsToSell.add(ItemID.AMULET_OF_GLORY);
        }
        //sell all items that we can sell
        if (!itemsToSell.isEmpty()) {
            for (int i : itemsToSell) {
                //set all wine supplies sell price to 1 for insta sell
                int sellPrice = (Arrays.stream(wineSuppliesIDs).anyMatch(w -> w == i) ? 1 : (int)(Prices.getItemPrice(i) * 0.5));
                new SellItemEvent()
                        .setId(i)
                        .setAmount(OwnedItems.getCountIncludingNoted(i))
                        .setPrice(sellPrice)
                        .execute();
            }
            return -1;
        }

        int ownedCoinsCount = OwnedItems.getCount(true, ItemID.COINS_995);
        int cookingLvl = Skills.getLevel(Skill.COOKING);
        if (!potentialItemsToMule.isEmpty() || ownedCoinsCount >= 3_100_000 || ownedCoinsCount < 900_000) {
            potentialItemsToMule.add(new OfferedItem(ItemID.COINS_995, ownedCoinsCount));
            List<RequiredItem> required = new ArrayList<>();
            int coinsRequired = (cookingLvl < 99 ? 900_000 + Rand.nextInt(0,200_000) : 2_900_000 + Rand.nextInt(0,200_000));
            required.add(new RequiredItem(
                    ItemID.COINS_995,
                    coinsRequired
                    )
            );
            new MuleEvent()
                    .setOfferedItems(potentialItemsToMule)
                    .setRequiredItems(required)
                    .execute();
            return 100;
        }

        //discriminate against cooking lvl to start buying sharks
        if (Skills.getLevel(Skill.COOKING) >= 99) {
            int sharkPrice = (int)(Prices.getItemPrice(ItemID.RAW_SHARK) * 1.15);
            int maxSharksCanBuy = ownedCoinsCount / sharkPrice;
            maxSharksCanBuy -= 5;
            new BuyItemEvent()
                    .setId(ItemID.RAW_SHARK)
                    .setAmount(maxSharksCanBuy)
                    .setPrice(sharkPrice)
                    .execute();
            return -1;
        }
        int highestRawID = P2PMeat.getHighestLvlMeat().getRawId();
        int rawFoodPrice = (int)(Prices.getItemPrice(highestRawID) * 1.5);
        int maxFoodCanBuy = ownedCoinsCount / rawFoodPrice;
        maxFoodCanBuy -= 5;
        int randMaxCap = (highestRawID == P2PMeat.SHRIMPS.getRawId() ? Rand.nextInt(400,1000) : Rand.nextInt(2000,6000));
        int foodQtyToBuy = Math.min(randMaxCap, maxFoodCanBuy);
        new BuyItemEvent()
                .setId(highestRawID)
                .setAmount(foodQtyToBuy)
                .setPrice(rawFoodPrice)
                .execute();
        return 100;
        /*
        int maxQty = (UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_4) ? 7000 : 9000);
        double wineSuppliesBuyMultiplier = (UniqueActions.isActionEnabled(UniqueActions.Actionz.SCRIPT_CUSTOM_ACTION_5) ? 2 : 2.3);
        int neededGrapes = maxQty - OwnedItems.getCountIncludingNoted(ItemID.GRAPES);
        if (neededGrapes > 0) {
            new BuyItemEvent()
                    .setId(ItemID.GRAPES)
                    .setAmount(neededGrapes)
                    .setPrice((int)(Prices.getItemPrice(ItemID.GRAPES) * wineSuppliesBuyMultiplier))
                    .execute();
            return -1;
        }
        int neededJugsOfWater = maxQty - OwnedItems.getCountIncludingNoted(ItemID.JUG_OF_WATER);

        if (neededJugsOfWater > 0) {
            new BuyItemEvent()
                    .setId(ItemID.JUG_OF_WATER)
                    .setAmount(neededJugsOfWater)
                    .setPrice((int)(Prices.getItemPrice(ItemID.JUG_OF_WATER) * wineSuppliesBuyMultiplier))
                    .execute();
        }
        return -1;*/
    }
}
