package net.unethicalite.scripts.api.events;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Production;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

@Slf4j
public class HopWorldEvent extends AbstractEvent {
    private int newWorld;
    private int oldWorld;
    public HopWorldEvent(int world) {
        this.newWorld = world;
        this.oldWorld = Worlds.getCurrentId();
        this.setEventCompletedCondition(() -> Game.getState() == GameState.LOGGED_IN && Worlds.getCurrentId() == this.newWorld);
    }
    public HopWorldEvent setRandomF2P() {
        this.newWorld = Worlds.getRandom(w -> w.isNormal() && !w.isMembers()).getId();
        return this;
    }
    public HopWorldEvent setRandomP2P() {
        this.newWorld = Worlds.getRandom(w -> w.isNormal() && w.isMembers()).getId();
        return this;
    }
    public HopWorldEvent() {
        this.oldWorld = Worlds.getCurrentId();
        World oldWorld = Worlds.getCurrentWorld();
        World randWorld = Worlds.getRandom(w -> oldWorld.getId() != w.getId() &&
                oldWorld.isMembers() == w.isMembers() &&
                oldWorld.isNormal() == w.isNormal() &&
                oldWorld.isAllPkWorld() == w.isAllPkWorld() &&
                oldWorld.isLeague() == w.isLeague() &&
                oldWorld.isSkillTotal() == w.isSkillTotal() &&
                oldWorld.getActivity().toLowerCase().contains("fresh start") == w.getActivity().toLowerCase().contains("fresh start") &&
                oldWorld.getActivity().toLowerCase().contains("beta") == w.getActivity().toLowerCase().contains("beta") &&
                oldWorld.isQuestSpeedRunning() == w.isQuestSpeedRunning() &&
                oldWorld.isTournament() == w.isTournament());
        if (randWorld == null) {
            log.info("[HOP WORLD EVENT] not able to find another world to hop to :o currently on world: "+this.oldWorld);
        } else {
            this.newWorld = randWorld.getId();
            log.info("[HOP WORLD EVENT] set random similar world: "+this.newWorld);
        }
        this.setEventCompletedCondition(() -> Game.getState() == GameState.LOGGED_IN && Worlds.getCurrentId() == this.newWorld);
    }
    private boolean closeCommonInterfaces() {
        if (Bank.isOpen()) {
            Bank.close();
            return false;
        }
        if (GrandExchange.isOpen()) {
            GrandExchange.close();
            return false;
        }
        if (Dialog.isViewingOptions()) {
            Movement.walk(Players.getLocal());
            return false;
        }
        if (Dialog.isEnterInputOpen()) {
            Keyboard.type("1",true);
            return false;
        }
        if (Trade.isOpen()) {
            Trade.accept();
            return false;
        }
        if (Production.isOpen()) {
            Movement.walk(Players.getLocal().getWorldLocation());
            return false;
        }
        return true;
    }

    private boolean openWorldHopper() {
        if (!closeCommonInterfaces()) {
            return false;
        }
        if (Widgets.get(WidgetInfo.WORLD_SWITCHER_LIST) != null) {
            return true;
        }
        Worlds.openHopper();
        return Widgets.get(WidgetInfo.WORLD_SWITCHER_LIST) != null;
    }

    @Override
    public void onLoop() {
        GameState state = Game.getState();
        if (state == GameState.LOGIN_SCREEN ||
                state == GameState.LOGIN_SCREEN_AUTHENTICATOR ||
                state == GameState.STARTING ||
                state == GameState.UNKNOWN) {
            log.info("[HOP WORLD EVENT] Failed to directly worldhop and got disconnected! Gamestate: "+state);
            this.setEventFailed(true);
            return;
        }

        if (state == GameState.HOPPING ||
                state == GameState.LOADING ||
                state == GameState.CONNECTION_LOST ||
                state == GameState.LOGGING_IN) {
            //log.info("[HOP WORLD EVENT] Waiting on game state to process! Gamestate: "+state);
            return;
        }

        if (!openWorldHopper()) {
            return;
        }

        int oldWorldId = Worlds.getCurrentId();
        log.info("[HOP WORLD EVENT] Hopping from: "+Worlds.getCurrentId()+" to: "+this.newWorld);
        Static.getClient().hopToWorld(Worlds.getFirst(this.newWorld));

        if (Time.sleepUntil(() -> Worlds.getCurrentId() != oldWorldId, () -> Game.getState() == GameState.HOPPING, 100, 4000)) {
            log.info("[HOP WORLD EVENT] Hop successful!");
        } else {
            log.info("[HOP WORLD EVENT] Hop failed!");
        }
    }
}
