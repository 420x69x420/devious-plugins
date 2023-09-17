package net.unethicalite.scripts.tasks.general.leaves;

import net.unethicalite.api.items.Bank;
import net.unethicalite.scripts.api.events.OpenBankEvent;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.Leaf;

public class CacheBank extends Leaf {
    @Override
    public boolean isValid() {
        return !InterfaceInstance.pluginInterface.isBankCached();
    }

    @Override
    public int execute() {
        if (Bank.isOpen()) {
            InterfaceInstance.pluginInterface.cachedBank();
            return 50;
        }
        new OpenBankEvent().execute();
        return 100;
    }
}
