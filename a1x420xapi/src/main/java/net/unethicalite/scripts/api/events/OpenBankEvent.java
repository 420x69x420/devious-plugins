package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.api.utils.Sleep;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OpenBankEvent extends AbstractEvent {
    private final BankLocation[] f2pBanks = {
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
    private final BankLocation[] p2pBanks = {
            BankLocation.ARDOUGNE_NORTH_BANK,
            BankLocation.ARDOUGNE_SOUTH_BANK,
            BankLocation.BARBARIAN_OUTPOST_BANK,
            BankLocation.CASTLE_WARS_BANK,
            BankLocation.CATHERBY_BANK,
            BankLocation.CHARCOAL_BURNERS_BANK,
            BankLocation.GRAND_TREE_SOUTH_BANK,
            BankLocation.GRAND_TREE_WEST_BANK,
            BankLocation.HOSIDIUS_BANK,
            BankLocation.PORT_KHAZARD_BANK,
            BankLocation.PORT_PISCARILIUS_BANK,
            BankLocation.SEERS_VILLAGE_BANK,
            BankLocation.TREE_GNOME_STRONGHOLD_BANK,
            BankLocation.YANILLE_BANK
    };
    private BankLocation getNearestValidBankLocation() {
        List<BankLocation> validBankLocations = new ArrayList<>();
        if (Worlds.getCurrentWorld().isMembers()) {
            validBankLocations.addAll(Arrays.stream(p2pBanks).collect(Collectors.toList()));
        }
        validBankLocations.addAll(Arrays.stream(f2pBanks).collect(Collectors.toList()));
        return validBankLocations
                .stream()
                .min(Comparator.comparingInt(x -> x.getArea().distanceTo(Players.getLocal())))
                .orElse(null);
    }
    private BankLocation bankLocation;

    public OpenBankEvent() {
        this.setEventCompletedCondition(Bank::isOpen);
        this.setEventTimeoutTicks(200);
    }

    public BankLocation getBankLocation() {
        return bankLocation;
    }

    public OpenBankEvent setBankLocation(BankLocation bankLocation) {
        this.bankLocation = bankLocation;
        return this;
    }

    private final WorldPoint roguesDenFire = new WorldPoint(3042,4973, 1);
    @Override
    public void onLoop() {
        if (getBankLocation() == null) {
            //special check for rogue's den bank to skip because we are in its banklocation already
            if (roguesDenFire.distanceTo(Players.getLocal()) > 25) {
                setBankLocation(getNearestValidBankLocation());
                log.info("[OPEN BANK EVENT] - Nearest bank location: " + getBankLocation());
            }

        }

        if (getBankLocation() != null && !getBankLocation().getArea().contains(Players.getLocal())) {
            log.info("[OPEN BANK EVENT] - Walking to " + getBankLocation());
            new MovementEvent()
                    .setDestination(getBankLocation())
                    .execute();
            return;
        }

        if (Movement.isWalking()) {
            return;
        }

        Map<SceneEntity, Integer> bankObjectDistances = new HashMap<>();
        SceneEntity bankBooth = TileObjects.getNearest(x -> x.getName().equals("Bank booth") && x.hasAction("Bank") && Reachable.isInteractable(x));
        SceneEntity banker = NPCs.getNearest(x -> x.getName().equals("Banker") && x.hasAction("Bank"));
        SceneEntity bankChest = TileObjects.getNearest(x -> x.getName().equals("Bank chest") && x.hasAction("Use") && Reachable.isInteractable(x));

        if (bankBooth != null) bankObjectDistances.put(bankBooth, bankBooth.getWorldLocation().distanceToPath(Static.getClient(), Players.getLocal().getWorldLocation()));
        if (banker != null) bankObjectDistances.put(banker, banker.getWorldLocation().distanceToPath(Static.getClient(), Players.getLocal().getWorldLocation()));
        if (bankChest != null) bankObjectDistances.put(bankChest, bankChest.getWorldLocation().distanceToPath(Static.getClient(), Players.getLocal().getWorldLocation()));

        if (!bankObjectDistances.isEmpty()) {
            SceneEntity nearestBankObject = bankObjectDistances.entrySet()
                    .stream()
                    .min(Map.Entry.comparingByValue())
                    .orElseThrow()
                    .getKey();

            String action = nearestBankObject.hasAction("Bank") ? "Bank" : "Use";
            log.info("[OPEN BANK EVENT] - Opening bank by " + nearestBankObject.getName());
            nearestBankObject.interact(action);
            Time.sleepTicksUntil((Bank::isOpen), 10);
        }
    }
}
