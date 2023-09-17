package net.unethicalite.scripts.tasks.general.leaves.tutorial;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Widgets;

@Slf4j
public class TutorialTab {

    @Getter
    @AllArgsConstructor
    public enum TutTab {
        ACCOUNT(548, 47, 161, 43, "Account Management"),
        FRIENDS(548, 48, 161, 44, "Friends List"),
        OPTIONS(548, 50, 161, 46, "Settings"),
        COMBAT(548, 62, 161, 58, "Combat Options"),
        SKILLS(548, 63, 161, 59, "Skills"),
        QUESTS(548, 64, 161, 60, "Quest List"),
        INVENTORY(548, 65, 161, 61, "Inventory"),
        EQUIPMENT(548, 66, 161, 62, "Worn Equipment"),
        PRAYER(548, 67, 161, 63, "Prayer"),
        MAGIC(548, 68, 161, 64, "Magic");

        private final int groupId1;
        private final int childId1;
        private final int groupId2;
        private final int childId2;
        private final String action;
    }
    public static void open(Tab tab) {
        TutTab t = TutTab.valueOf(tab.name());
        if (t == null) {
            log.info("Unsupported tab to open on Tutorial Island: " + tab);
            return;
        }

        Widget tabButton = Widgets.get(t.getGroupId1(),t.getChildId1());
        if (tabButton == null || !tabButton.isVisible()) {
            tabButton = Widgets.get(t.getGroupId2(),t.getChildId2());
        }
        if (tabButton == null || !tabButton.isVisible()) {
            log.info("Tab to open on tutorial null or not visible: " +tab);
            return;
        }
        if (!tabButton.hasAction(t.getAction())) {
            log.info("Tab to open on tutorial not have exact action: " +t.getAction());
            return;
        }
        tabButton.interact(t.getAction());
    }
}
