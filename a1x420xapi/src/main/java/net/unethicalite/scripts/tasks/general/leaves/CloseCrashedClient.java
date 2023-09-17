package net.unethicalite.scripts.tasks.general.leaves;

import net.unethicalite.client.Static;
import net.unethicalite.scripts.framework.Leaf;

public class CloseCrashedClient extends Leaf {
    @Override
    public boolean isValid() {
        return Static.getClient() == null;
    }

    @Override
    public int execute() {
        System.exit(0);
        return -1;
    }
}
