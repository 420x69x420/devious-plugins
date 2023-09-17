package net.unethicalite.scripts.api.extended;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.input.Keyboard;
@Slf4j
public class ExKeyboard {
    public static void pressSpecialKey(int keyEvent) {
        log.debug("Pressed keyEvent: " + keyEvent);
        Keyboard.pressed(keyEvent);
        log.debug("Release keyEvent: " + keyEvent);
        Keyboard.released(keyEvent);
    }
}
