package net.unethicalite.scripts.api.extended;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ExBank {
    public static void depositAllExcept(int... ids) {
        List<Integer> itemsToDeposit = new ArrayList<>();
        for (int i : ids) {
            itemsToDeposit.add(i);
        }
        depositAllExcept(itemsToDeposit);
    }

    public static void depositAllExcept(List<Integer> ids) {
        List<Integer> itemsDeposited = new ArrayList<>();
        int itemsThisTick = 1;
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !ids.stream().anyMatch(exceptId -> exceptId == i.getId()))) {
            if (itemsThisTick >= 10) {
                itemsThisTick = 0;
                Time.sleepTick();
            }
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: "+ i.getName());
            Bank.depositAll(i.getId());
            itemsThisTick++;
        }
    }

    public static void depositAllExcept(String... names) {
        List<Integer> itemsDeposited = new ArrayList<>();
        int itemsThisTick = 1;
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !Arrays.stream(names).anyMatch(exceptName -> exceptName.equals(i.getName())))) {
            if (itemsThisTick >= 10) {
                itemsThisTick = 0;
                Time.sleepTick();
            }
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: "+ i.getName());
            Bank.depositAll(i.getId());
            itemsThisTick++;
        }
    }

    public static BankLocation[] f2pBanks = {
            BankLocation.AL_KHARID_BANK,
            BankLocation.DRAYNOR_BANK,
            BankLocation.LUMBRIDGE_BANK,
            BankLocation.EDGEVILLE_BANK,
            BankLocation.FALADOR_EAST_BANK,
            BankLocation.FALADOR_WEST_BANK,
            BankLocation.VARROCK_WEST_BANK,
            BankLocation.VARROCK_EAST_BANK,
            BankLocation.GRAND_EXCHANGE_BANK,
            BankLocation.DUEL_ARENA_BANK,
            BankLocation.FEROX_ENCLAVE_BANK
    };

    public static BankLocation getNearestF2PBankLocation() {
        return Arrays.stream(f2pBanks)
                .sorted(Comparator.comparingInt(b -> b.getArea().distanceTo(Players.getLocal())))
                .collect(Collectors.toList())
                .get(0);
    }
}
