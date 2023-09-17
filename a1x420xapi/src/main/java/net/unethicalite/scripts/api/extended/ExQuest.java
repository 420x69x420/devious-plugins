package net.unethicalite.scripts.api.extended;

import net.runelite.api.widgets.Widget;
import net.unethicalite.api.widgets.Widgets;

public class ExQuest {
    public static Widget getQuestCompletionCloseButton() {
        return Widgets.get(153,16);
    }
    public static boolean isQuestCompletionOpen() {
        Widget w = getQuestCompletionCloseButton();
        return w != null && w.isVisible();
    }
    public static void closeQuestCompletion() {
        Widget w = getQuestCompletionCloseButton();
        if (w != null && w.isVisible()) {
            w.interact("Close");
        }
    }
}