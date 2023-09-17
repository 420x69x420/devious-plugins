package net.unethicalite.scripts.tasks.bondup.leaves;

import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.scripts.api.events.BondUpEvent;
import net.unethicalite.scripts.api.events.HopWorldEvent;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.Leaf;

public class BondUp extends Leaf {

    @Override
    public boolean isValid() {
        return InterfaceInstance.pluginInterface.shouldBondUp() && (Game.getMembershipDays() <= 1 || (Game.getMembershipDays() > 0 && !Worlds.getCurrentWorld().isMembers()));
    }

    @Override
    public int execute() {
        //have more than 1 membership days remaining, hop to a p2p world
        if (Game.getMembershipDays() > 0 && !Worlds.getCurrentWorld().isMembers()) {
            new HopWorldEvent(Worlds.getRandom(w -> w.isNormal() && w.isMembers()).getId())
                    .execute();
            return -1;
        }
        //have more 1 or 0 membership days remaining, bond ourself
        new BondUpEvent().execute();
        return -1;
    }
}
