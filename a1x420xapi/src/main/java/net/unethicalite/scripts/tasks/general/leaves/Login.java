package net.unethicalite.scripts.tasks.general.leaves;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.unethicalite.api.account.GameAccount;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import net.unethicalite.client.Static;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.api.launcher420.Log;
import net.unethicalite.scripts.framework.Leaf;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Login extends Leaf {
    @Override
    public boolean isValid() {
        return Game.getState() != GameState.LOGGED_IN;
    }

    private static void loginRandomWorld(boolean F2P, String user, String pass)
    {
        Client client = Static.getClient();
        Worlds.loadWorlds();
        if (client.getWorldList() != null) {
            World randWorld = Worlds.getRandom(w -> w.isNormal() && !w.isMembers() == F2P);
            final int worldID = randWorld.getId();
            client.changeWorld(randWorld);
            if (Time.sleepUntil(() -> client.getWorld() == worldID, 10000)) {
                client.setUsername(user);
                client.setPassword(pass);
                Keyboard.sendEnter();
                Keyboard.sendEnter();
            }
        } else {
            log.info("Worlds list not loaded yet");
        }
    }
    @Override
    public int execute() {
        GameAccount gameAccount = Game.getGameAccount();
        if (gameAccount == null) {
            log.info("Cannot login with null GameAccount! Must launch with '--account=USER:PASS' client argument to login with 420xapi. Stopping script: "+ InterfaceInstance.pluginInterface.plugin().getName());
            Plugins.stopPlugin(InterfaceInstance.pluginInterface.plugin());
            return -1;
        }
        if (Game.getState() == GameState.LOADING ||
                Game.getState() == GameState.LOGGING_IN ||
                Game.getState() == GameState.HOPPING ||
                Game.getState() == GameState.CONNECTION_LOST ||
                Game.getState() == GameState.STARTING)   {
            log.info("Waiting while game state is processing / loading");
            return 300;
        }
        GameAccount acc = Game.getGameAccount();
        if (Game.getGameAccount() == null) {
            log.info("We're not loaded with acc :o");
            return 10000;
        }
        String user = acc.getUsername();
        String pass = acc.getPassword();
        if (user.isEmpty() || pass.isEmpty()) {
            log.info("We're loaded with an empty user or pass! user:pass="+user+":"+pass);
            return 10000;
        }
        Client client = Static.getClient();
        GameState state = client.getGameState();
        String loginMessage = client.getLoginMessage().toLowerCase();
        log.debug("LoginState index: "+client.getLoginIndex()+" associated with GameState:"+state+ "and loginMessage: "+loginMessage);
        log.info("Login");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = formatter.format(Instant.now().atZone(ZoneId.systemDefault()));
        log.info(time);
        if (loginMessage.contains("disabled") || loginMessage.contains("serious rule breaking")) {
            Log.sendKeyValuePairToOutputStream("ACCOUNT_STATUS","banned at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            log.info("banned at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            System.exit(0);
        }
        if (loginMessage.contains("your account has been locked")) {
            Log.sendKeyValuePairToOutputStream("ACCOUNT_STATUS","locked at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            log.info("locked at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            System.exit(0);
        }
        if (loginMessage.contains("incorrect username or password")) {
            log.info("invalid credentials at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            Log.sendKeyValuePairToOutputStream("ACCOUNT_STATUS","invalid credentials");
            System.exit(0);
        }
        if (loginMessage.contains("update")) {
            log.info("Game updated!");
            Time.sleep(1000);
            System.exit(0);
        }
        switch (client.getLoginIndex()) {
            case LoginEvent.State.AUTHENTICATOR:
                log.info("Authenticator not scripted yet on Login leaf! Sucks");
                return 1000;

            case LoginEvent.State.ENTER_CREDENTIALS:
                loginRandomWorld(InterfaceInstance.pluginInterface.loginF2P(), user, pass);
                return 600;

            case LoginEvent.State.MAIN_MENU:
            case LoginEvent.State.BEEN_DISCONNECTED:
                client.setLoginIndex(LoginEvent.State.ENTER_CREDENTIALS);
                return 1000;
        }

        return -1;
    }
}
