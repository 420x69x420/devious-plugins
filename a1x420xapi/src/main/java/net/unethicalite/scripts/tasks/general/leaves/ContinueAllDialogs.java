package net.unethicalite.scripts.tasks.general.leaves;

import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.scripts.framework.Leaf;

public class ContinueAllDialogs extends Leaf {
    @Override
    public boolean isValid() {
        return Dialog.canContinue() || (Dialog.isViewingOptions() && Dialog.hasOption("Please wait..."));
    }

    @Override
    public int execute() {
        if (Dialog.canContinue()) {
            Dialog.continueSpace();
        }
        return -1;
    }
}
