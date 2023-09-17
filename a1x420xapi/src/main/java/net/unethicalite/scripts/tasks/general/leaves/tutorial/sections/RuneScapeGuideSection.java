package net.unethicalite.scripts.tasks.general.leaves.tutorial.sections;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.scripts.tasks.general.leaves.Tut;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.TutorialTab;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.customization.CharacterPainter;
import net.unethicalite.scripts.tasks.general.leaves.tutorial.customization.TypeName;

@Slf4j
public final class RuneScapeGuideSection {
    private static WorldPoint instructorTile = new WorldPoint(3093, 3107, 0);

    private static String instructor = "Gielinor Guide";

    public static void onLoop() {
        switch (Tut.mainVarp()) {
            case 0:
            case 1:
            case 2:
                Widget typeName = TypeName.getLookupStatusText();
                Widget characterPainter = CharacterPainter.getPainterMenu();
                if (typeName != null && typeName.isVisible()) {
                    TypeName.onLoop();
                } else if (characterPainter != null && characterPainter.isVisible()) {
                    CharacterPainter.onLoop();
                } else {
                    Tut.talkToInstructor(instructorTile, instructor);
                }
                break;
            case 3:
                TutorialTab.open(Tab.OPTIONS);
                break;
            case 10:
                TileObject door = TileObjects.getNearest(g -> g.getName().equals("Door") && g.hasAction("Open"));
                if (door != null) {
                    door.interact("Open");
                    Time.sleepUntil(() -> Tut.mainVarp() != 10, 420, 5000);
                }
                break;
            default:
                Tut.talkToInstructor(instructorTile, instructor);
                break;
        }
    }

}