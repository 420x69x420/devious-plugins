/*
 * Copyright (c) 2022, Melxin <https://github.com/melxin/>,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.unethicalite.powerfisher;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;
import javax.inject.Inject;

@Slf4j
@Extension
@PluginDescriptor(
        name = "mPower Fisher",
        description = "Power fish",
        enabledByDefault = false,
        tags =
                {
                        "Fishing",
                        "auto",
                        "fish",
                        "shrimp",
                        "anchovies",
                        "trout",
                        "salmon",
                        "barbarian"
                }
)
public class PowerFisherPlugin extends Script {
    @Inject
    private Client client;

    @Inject
    private PowerFisherConfig config;

    @Provides
    private PowerFisherConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PowerFisherConfig.class);
    }


    @Override
    protected int loop() {
        // logged in
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return -1;
        }

        // Stop when level is reached..
        if (client.getBoostedSkillLevel(Skill.FISHING) >= config.destinationLevel())
        {
            log.info("Congratulations, reached goal fishing level: "+config.destinationLevel()+", stopping fishing!");
            this.stop();
            return -1;
        }

        if (HopWorld6HLog.shouldHop()) {
            return HopWorld6HLog.hop();
        }

        if (Dialog.canContinue()) {
            Dialog.continueSpace();
            return -1;
        }

        FishingType fishingType = config.fishingType();

        // No required items found
        if (!Inventory.contains(fishingType.getRequiredItems()))
        {
            log.error("Make sure you have required items in inventory: {}", fishingType.getRequiredItems().toString());
            this.stop();
            return -1;
        }

        // Idle
        if (client.getLocalPlayer().getInteracting() == null)
        {
            // Drop fish
            if (Inventory.contains(fishingType.getFishToDrop()))
            {
                int dropped = 0;
                for (Item toDrop : Inventory.getAll(fishingType.getFishToDrop())) {
                    if (dropped >= 10) {
                        dropped = 0;
                        Time.sleepTick();
                    }
                    toDrop.drop();
                    dropped++;
                }
                return -1;
            }
            log.info("fishingType = "+fishingType.getFishToDrop() + " " + fishingType.getAction());
            // Fish
            NPC fishingSpot = client.getNpcs()
                    .stream()
                    .filter(n -> n.getName().contains("spot") && n.hasAction(fishingType.getAction()))
                    .findFirst()
                    .get();

            if (fishingSpot == null)
            {
                return -1;
            }
            fishingSpot.interact(fishingType.getAction());
        }
        return -2;
    }

    @Override
    public void onStart(String... strings) {
        HopWorld6HLog.resetHopTick();
    }


}
