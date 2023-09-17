package net.unethicalite.scripts.sharks.tasks.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Production;
import net.unethicalite.scripts.api.events.BankInventoryEvent;
import net.unethicalite.scripts.api.events.OpenBankEvent;
import net.unethicalite.scripts.api.utils.OwnedItems;
import net.unethicalite.scripts.framework.Leaf;
@Slf4j
public class WinesTo99 extends Leaf {

    @Override
    public boolean isValid() {
        return Skills.getLevel(Skill.COOKING) >= 35 && Skills.getLevel(Skill.COOKING) < 99;
    }

    @Override
    public int execute() {
        if (Bank.isOpen()) {
            if (Bank.Inventory.getCount(ItemID.GRAPES) > 0 && Bank.Inventory.getCount(ItemID.JUG_OF_WATER) > 0) {
                Bank.close();
                return -1;
            }
            withdrawIngredients();
            //dont sleep so long after this method call due it already waiting for its completion
            return Rand.nextInt(1,100);
        }

        //ferment wines if we can (method returns true if can ferment)
        if (Inventory.contains(ItemID.GRAPES) && Inventory.contains(ItemID.JUG_OF_WATER)) {
            fermentWine();
            return -1;
        }

        // walk to bank if not near bank and withdraw ingredients
        new OpenBankEvent()
                .execute();
        return -1;
    }

    /**
     * withdraws the ingredients. does nothing if no more items in bank. must be called when bank is already open
     * @return
     */
    private void withdrawIngredients() {
        if (Inventory.contains(OwnedItems.getNotedID(ItemID.GRAPES), OwnedItems.getNotedID(ItemID.JUG_OF_WATER))) {
            log.info("Deposit some noted grapes/jugs of water");
            new BankInventoryEvent().execute();
            return;
        }
        log.info("Start bank deposit + withdraw sequence");
        // simplified bank sequence for simplicity - deposit all items, withdraw both items, wait until have some of both, if so, close bank
        Bank.depositInventory();
        Bank.withdraw(ItemID.GRAPES, 14, Bank.WithdrawMode.ITEM);
        Bank.withdraw(ItemID.JUG_OF_WATER, 14, Bank.WithdrawMode.ITEM);
        Time.sleepUntil(() -> Bank.Inventory.getCount(ItemID.GRAPES) > 0 && Bank.Inventory.getCount(ItemID.JUG_OF_WATER) > 0, 100, 3000);
        log.info("end withdraw sleepUntil");
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
        if (Production.isOpen()) {
            //grab current count of wines to make
            log.info("Press space to make wines");
            Keyboard.type(" ");
            Time.sleepUntil(() -> !Inventory.contains(ItemID.GRAPES, ItemID.JUG_OF_WATER), () -> Players.getLocal().isAnimating(), 100, 4000);
            return;
        }
        //use items together and wait for skill menu to open
        log.info("Use grapes on jugs of water");
        grapes.useOn(jugs);
        Time.sleepUntil(() -> Production.isOpen(), 100, 4000);
    }

}
