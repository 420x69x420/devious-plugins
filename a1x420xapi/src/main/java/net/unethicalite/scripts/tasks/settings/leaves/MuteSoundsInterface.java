package net.unethicalite.scripts.tasks.settings.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.VarPlayer;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.Leaf;

@Slf4j
public class MuteSoundsInterface extends Leaf {
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int execute() {
        if (!audioSettingsIsOpen()) {
            Widget audioSettingsWidget = Widgets.get(134, 23, 1);
            if (audioSettingsWidget == null) {
                log.info("Can't find the widget to open the audio settings");
                return -1;
            }

            log.info("Opening audio settings");
            audioSettingsWidget.interact("Select Audio");
            Time.sleepTicksUntil(() -> !audioSettingsIsOpen(), 3);
            return -1;
        }

        if (shouldMuteMusicVolume()) {
            log.info("Should mute music volume");
            Widget musicVolumeWidget = Widgets.get(134, 21, 21);
            if (musicVolumeWidget != null && musicVolumeWidget.isVisible()) {
                log.info("Muting music volume");
                musicVolumeWidget.interact("Select");
                Time.sleepTicksUntil(() -> !shouldMuteMusicVolume(), 3);
            }
        }

        if (shouldMuteSoundEffectVolume()) {
            Widget soundEffectVolumeWidget = Widgets.get(134, 21, 42);
            if (soundEffectVolumeWidget != null && soundEffectVolumeWidget.isVisible()) {
                log.info("Muting sound effect volume");
                soundEffectVolumeWidget.interact("Select");
                Time.sleepTicksUntil(() -> !shouldMuteSoundEffectVolume(), 3);
            }
        }

        if (shouldMuteAreaEffectVolume()) {
            log.info("Should mute area effect volume");
            Widget areaEffectVolumeWidget = Widgets.get(134, 21, 63);
            if (areaEffectVolumeWidget != null && areaEffectVolumeWidget.isVisible()) {
                log.info("Muting area effect volume");
                areaEffectVolumeWidget.interact("Select");
                Time.sleepTicksUntil(() -> !shouldMuteAreaEffectVolume(), 3);
            }
        }

        return -1;
    }

    private boolean audioSettingsIsOpen() {
        Widget audioSettingsWidget = Widgets.get(134, 23, 1);
        return audioSettingsWidget != null && !audioSettingsWidget.isVisible();
    }

    public static boolean shouldMuteSounds() {
        return shouldMuteMusicVolume() || shouldMuteSoundEffectVolume() || shouldMuteAreaEffectVolume();
    }

    private static boolean shouldMuteAreaEffectVolume() {
        return Vars.getVarp(VarPlayer.AREA_EFFECT_VOLUME.getId()) != 0;
    }

    private static boolean shouldMuteSoundEffectVolume() {
        return Vars.getVarp(VarPlayer.SOUND_EFFECT_VOLUME.getId()) != 0;
    }

    private static boolean shouldMuteMusicVolume() {
        return Vars.getVarp(VarPlayer.MUSIC_VOLUME.getId()) != 0;
    }
}
