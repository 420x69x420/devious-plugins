package net.unethicalite.scripts.runecrafter.leaves;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.scripts.api.events.HopWorldEvent;
import net.unethicalite.scripts.framework.Leaf;

import java.time.Instant;

@Slf4j
public class HopWorlds extends Leaf {
    private Instant hopTime = null;
    @Override
    public boolean isValid() {
        return hopTime == null || Instant.now().isAfter(hopTime);
    }

    @Override
    public int execute() {
        if (hopTime == null) {
            resetHopTime();
            return 100;
        }
        new HopWorldEvent().execute();
        resetHopTime();
        return -1;
    }
    private void resetHopTime() {
        long sec = Rand.nextInt((int) (60D * 60D * 2D), (int) (60D * 60D * 5D));
        int minutes = (int) (sec * 0.6D / 60D);
        log.info("Reset hop tick timer for: "+minutes+" minutes from now");
        // Reset timer for 2-5 hours from now in ticks based on current tick
        hopTime = Instant.now().plusSeconds(sec);
    }
}
