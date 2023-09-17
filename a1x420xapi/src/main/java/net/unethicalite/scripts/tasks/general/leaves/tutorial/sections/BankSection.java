package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;


import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.api.events.MovementEvent;
import net.unethicalite.scripts.tasks.general.leaves.Tut;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;

public final class BankSection {

    private static final WorldPoint instructorTile = new WorldPoint(3127, 3124, 0);
    private static final String instructor = "Account Guide";
    private static final WorldPoint bankTile = new WorldPoint(3122, 3124, 0);
    private static final WorldPoint pollBooth = new WorldPoint(3119, 3121, 0);
    private static Widget getPollBoothCloseButton() {
        return Widgets.get(345,2,11);
    }
    private static boolean isPollBoothOpen() {
        Widget w = getPollBoothCloseButton();
        return w != null && w.isVisible();
    }
    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 510:
                TileObject bank = TileObjects.getNearest(g -> g.getName().equals("Bank booth") && g.hasAction("Use"));
                if (bank == null || bank.distanceTo(Players.getLocal()) > 15 || !Reachable.isInteractable(bank)) {
                    new MovementEvent()
                            .setDestination(bankTile)
                            .setRadius(15)
                            .execute();
                } else {
                    bank.interact("Use");
                    Time.sleepUntil(Bank::isOpen, () -> Players.getLocal().isMoving(), 300, 6000);
                }
                break;
            case 520:
                if (Bank.isOpen()) {
                    Bank.close();
                } else {
                    TileObject polllBooth = TileObjects.getFirstSurrounding(pollBooth, 5, "Poll booth");
                    if (polllBooth != null) {
                        polllBooth.interact("Use");
                        Time.sleepUntil(Dialog::canContinue, () -> Players.getLocal().isMoving(), 300, 6000);
                    }
                }
                break;
            case 525:
            case 530:
                if (isPollBoothOpen()) {
                    getPollBoothCloseButton().interact("Close");
                    Time.sleepTick();
                    break;
                }
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 531:
                TutorialTab.open(Tab.ACCOUNT);
                break;
            case 532:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
            case 540:
                //proceed to chuch, its sunday bitch. tabernacle
                Tut.talkToInstructor(PriestSection.instructorTile, PriestSection.instructor);
                break;
        }
    }
}
