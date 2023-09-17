package net.unethicalite.scripts.tasks.general.leaves;

import net.unethicalite.scripts.api.extended.ExQuest;
import net.unethicalite.scripts.framework.Leaf;

public class CloseQuestCompletion extends Leaf {

    @Override
    public boolean isValid() {
        return ExQuest.isQuestCompletionOpen();
    }

    @Override
    public int execute() {
        ExQuest.closeQuestCompletion();
        return -1;
    }
}
