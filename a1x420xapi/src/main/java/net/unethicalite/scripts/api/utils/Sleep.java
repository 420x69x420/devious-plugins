package net.unethicalite.scripts.api.utils;

import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;

public class Sleep {
    public static void shortSleep() {
        Time.sleep(100,350);
    }

    public static void fastSleep() {
        Time.sleep(25,75);
    }

    public static void longSleep() {
        Time.sleep(500,1000);
    }

    public static int returnTick() {
        Time.sleepTick();
        return Rand.nextInt(25,75);
    }
    public static int returnFast() {
        Time.sleepTick();
        return Rand.nextInt(25,75);
    }
    public static int returnShort() {
        Time.sleepTick();
        return Rand.nextInt(100,350);
    }
    public static int returnLong() {
        Time.sleepTick();
        return Rand.nextInt(500,1000);
    }
}
