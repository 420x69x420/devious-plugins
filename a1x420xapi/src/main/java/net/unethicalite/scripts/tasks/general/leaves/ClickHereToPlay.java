package net.unethicalite.scripts.tasks.general.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.script.blocking_events.WelcomeScreenEvent;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class ClickHereToPlay extends Leaf {
    @Override
    public boolean isValid() {
        Widget playButton = WelcomeScreenEvent.getPlayButton();
        return playButton != null && playButton.isVisible();
    }

    @Override
    public int execute() {
        Widget playButton = WelcomeScreenEvent.getPlayButton();
        log.info("Clicked 'Click here to play'");
        Static.getClient().invokeWidgetAction(1, playButton.getId(), -1, -1, "");
        return -1;
    }
}
