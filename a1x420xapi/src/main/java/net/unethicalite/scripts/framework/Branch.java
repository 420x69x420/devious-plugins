package net.unethicalite.scripts.framework;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class Branch extends Leaf {

    public abstract boolean generalValidation();

    public final List<Leaf> leaves;

    public Branch() {
        this.leaves = new LinkedList<>();
    }

    public Branch addLeaves(Leaf... leaves) {
        Collections.addAll(this.leaves, leaves);
        return this;
    }

    @Override
    public boolean isValid() {
        if (generalValidation()) {
            for (Leaf leaf : leaves) {
                if (leaf.isValid()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int execute() {
        for (Leaf leaf : leaves) {
            if (leaf.isValid()) {
                return leaf.execute();
            }
        }
        return 1000;
    }
}

