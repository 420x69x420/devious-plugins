package net.unethicalite.scripts.tasks.general.leaves;

import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.scripts.framework.Leaf;

public class SayThanksForEgg extends Leaf {
    @Override
    public boolean isValid() {
        return Bank.isOpen() && Equipment.contains(e -> e.getName().contains("handegg"));
    }

    @Override
    public int execute() {
        Bank.depositEquipment();
        return Rand.nextInt(100,500);
    }
}
