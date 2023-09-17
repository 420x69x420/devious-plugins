package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.*;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class DisableSound extends Leaf {
    @Override
    public boolean isValid() {
        return shouldMuteSound();
    }

    @Override
    public int execute() {
        if (GrandExchange.isOpen()) {
            GrandExchange.close();
            return -1;
        }
        if (Bank.isOpen()) {
            Bank.close();
            return -1;
        }
        if (Trade.isOpen()) {
            Trade.decline();
            return -1;
        }
        if (Dialog.canContinue()) {
            Dialog.continueSpace();
            return -1;
        }
        if (Dialog.isOpen() || Production.isOpen()) {
            Movement.walk(Players.getLocal());
            return -1;
        }
        if (!Tabs.isOpen(Tab.OPTIONS)) {
            log.info("Opening options tab");
            Tabs.open(Tab.OPTIONS);
            Time.sleepTicksUntil(() -> Tabs.isOpen(Tab.OPTIONS), 3);
            return -1;
        }

        Widget audio = Widgets.get(116, 68);
        if (audio != null && audio.isVisible() && audio.hasAction("Audio")) {
            log.info("Opening audio tab");
            audio.interact("Audio");
            Time.sleepTicksUntil(() -> !Widgets.get(116, 68).hasAction("Audio"), 3);
            return -1;
        }

        if (shouldMuteMusicVolume()) {
            Widget muteMusic = Widgets.get(116, 93);
            if (muteMusic == null || !muteMusic.isVisible()) {
                log.info("Can't find mute music");
                return -1;
            }

            log.info("Muting music");
            muteMusic.interact("Mute");
            Time.sleepTicksUntil(() -> !shouldMuteMusicVolume(), 3);
        }

        if (shouldMuteSoundEffectVolume()) {
            Widget muteSoundEffect = Widgets.get(116, 107);
            if (muteSoundEffect == null || !muteSoundEffect.isVisible()) {
                log.info("Can't find mute sound effect");
                return -1;
            }

            log.info("Muting sound effect");
            muteSoundEffect.interact("Mute");
            Time.sleepTicksUntil(() -> !shouldMuteSoundEffectVolume(), 3);
        }

        if (shouldMuteAreaEffectVolume()) {
            Widget muteAreaEffect = Widgets.get(116, 122);
            if (muteAreaEffect == null || !muteAreaEffect.isVisible()) {
                log.info("Can't find mute area effect");
                return -1;
            }

            log.info("Muting area effect");
            muteAreaEffect.interact("Mute");
            Time.sleepTicksUntil(() -> !shouldMuteAreaEffectVolume(), 3);
        }

        return -1;
    }

    public static boolean shouldMuteSound() {
        return shouldMuteMusicVolume() || shouldMuteSoundEffectVolume() || shouldMuteAreaEffectVolume();
    }

    private static boolean shouldMuteMusicVolume() {
        Widget muteMusic = Widgets.get(116, 93);
        return muteMusic != null && muteMusic.hasAction("Mute");
    }

    private static boolean shouldMuteSoundEffectVolume() {
        Widget muteSoundEffect = Widgets.get(116, 107);
        return muteSoundEffect != null && muteSoundEffect.hasAction("Mute");
    }

    private static boolean shouldMuteAreaEffectVolume() {
        Widget muteAreaSounds = Widgets.get(116, 122);
        return muteAreaSounds != null && muteAreaSounds.hasAction("Mute");
    }
}
