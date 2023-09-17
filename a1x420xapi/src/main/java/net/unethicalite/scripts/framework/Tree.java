package net.unethicalite.scripts.framework;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Tree{
    public final List<Branch> branches;

    public Tree() {
        branches = new LinkedList<>();
    }

    public Tree addBranches(Branch... branches) {
        Collections.addAll(this.branches, branches);
        return this;
    }

    public int execute() {
        for (Branch branch : branches) {
            if (branch.isValid()) {
                return branch.execute();
            }
        }
        return 1000;
    }
}
