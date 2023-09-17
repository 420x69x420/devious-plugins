package net.unethicalite.scripts.nightmare.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.unethicalite.scripts.nightmare.NightmarePlugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

import static net.unethicalite.scripts.nightmare.NightmarePlugin.isInFeroxEnclave;
@Slf4j
public class CombatStuff {
    public static boolean shouldEat (int configThreshold) {
        return Skills.getBoostedLevel(Skill.HITPOINTS) <= configThreshold;
    }
    public static boolean eat () {
        if (!Inventory.contains(ItemID.ANGLERFISH)) {
            if (Skills.getBoostedLevel(Skill.HITPOINTS) < 25) {
                log.info("teleport out");
                teleportOut();
                return true;
            }
            log.info("no more anglerfish but more than 25 HP, riding it out");
        } else if (NightmarePlugin.eatTickTimer == 0) {
            Item food = Inventory.getFirst(ItemID.ANGLERFISH);
            log.info("Eat anglerfish");
            food.interact("Eat");
            NightmarePlugin.eatTickTimer = 3;
            return true;
        }
        return false;
    }
    public static int teleportOut() {
        if (Bank.isOpen()) {
            API.shortSleep();
            Bank.close();
            return API.shortReturn();
        }

        if (Equipment.contains(i -> i.getName().contains("Ring of dueling"))) {
            API.fastSleep();
            Item duelEquipped = Equipment.getFirst(i -> i.getName().contains("Ring of dueling"));
            log.info("Teleporting to Ferox!");
            duelEquipped.interact("Ferox Enclave");
            Time.sleepUntil(() -> isInFeroxEnclave(), () -> Players.getLocal().isAnimating(), 100, 2000);
            return API.shortReturn();
        }
        if (!Inventory.contains(i -> i.getName().contains("Ring of dueling"))) {
            log.info("Script error :o no dueling left!! wtf");
            return API.shortReturn();
        }
        Item duelInvy = Inventory.getFirst(o -> o.getName().contains("Ring of dueling"));
        log.info("Wear dueling ring");
        duelInvy.interact("Wear");
        return API.shortReturn();
    }
    public static boolean shouldDrinkCombatPotion() {
        return NightmarePlugin.divinePotionExpiring || Skills.getBoostedLevel(Skill.STRENGTH) <= Skills.getLevel(Skill.STRENGTH);
    }
    public static boolean drinkCombatPotion() {
        int doseID = Doses.getLeastSuperCombat();
        if (doseID == -1) {
            log.info("teleport out, no super combat pots");
            teleportOut();
            return true;
        }
        Item pot = Inventory.getFirst(doseID);
        if (pot == null) {
            log.info("Script error, super combat pot discrepancy");
            CombatStuff.teleportOut();
            return true;
        }
        if (NightmarePlugin.drinkTickTimer == 0) {
            log.info("Drink " + pot.getName());
            pot.interact("Drink");
            NightmarePlugin.drinkTickTimer = 3;
            NightmarePlugin.divinePotionExpiring = false;
        }
        return false;
    }

    public static boolean shouldDrinkPrayer(int prayerConfig) {
        return Skills.getBoostedLevel(Skill.PRAYER) <= prayerConfig;
    }
    public static boolean drinkPrayerPotion() {
        int doseID = Doses.getLeastPrayerPot();
        if (doseID == -1) {
            if (Skills.getBoostedLevel(Skill.PRAYER) <= 2) {
                log.info("teleport out, no prayer pots");
                teleportOut();
                return true;
            }
            log.info("No more prayer pots but more than 2 prayer, riding it out");
        } else if (NightmarePlugin.drinkTickTimer == 0) {
            Item pot = Inventory.getFirst(doseID);
            log.info("Drink " + pot.getName());
            pot.interact("Drink");
            NightmarePlugin.drinkTickTimer = 3;
            API.fastSleep();
        }
        return false;
    }
    public static boolean inventoryEquipmentContains(int id) {
        return Inventory.contains(id) || Equipment.contains(id);
    }
}
