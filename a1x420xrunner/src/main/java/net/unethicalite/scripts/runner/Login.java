package net.unethicalite.scripts.runner;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.unethicalite.api.account.GameAccount;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Mouse;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Login {
    public static boolean shouldLogin() {
        return Game.getState() != GameState.LOGGED_IN;
    }

    public static int login(Client client) {
        GameAccount acc = Game.getGameAccount();
        if (Game.getGameAccount() == null) {
            log.info("We're not loaded with acc :o stopping script!");
            API.stopScript = true;
            return 100;
        }
        String user = acc.getUsername();
        String pass = acc.getPassword();
        if (user.isEmpty() || pass.isEmpty()) {
            log.info("We're loaded with an empty user or pass! user:pass="+user+":"+pass);
            API.stopScript = true;
            return 100;
        }
        GameState state = client.getGameState();
        String loginMessage = client.getLoginMessage().toLowerCase();
        log.info("LoginState index: "+client.getLoginIndex()+" associated with GameState:"+state+ "and loginMessage: "+loginMessage);
        log.info("Login");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = formatter.format(Instant.now().atZone(ZoneId.systemDefault()));
        System.out.println(time);
        if (loginMessage.contains("disabled") || loginMessage.contains("serious rule breaking")) {
            Log.sendKeyValuePairToOutputStream("ACCOUNT_STATUS","banned at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            log.info("banned at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            System.exit(0);
        } else if (loginMessage.contains("your account has been locked")) {
            Log.sendKeyValuePairToOutputStream("ACCOUNT_STATUS","locked at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            log.info("locked at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            System.exit(0);
        } else if (loginMessage.contains("incorrect username or password")) {
            log.info("invalid credentials at ["+Instant.now().toEpochMilli()+"]["+time+"]");
            Log.sendKeyValuePairToOutputStream("ACCOUNT_STATUS","invalid credentials");
            System.exit(0);
        } else if (loginMessage.contains("update")) {
            log.info("Game updated!");
            Time.sleep(1000);
            System.exit(0);
        } else {
            if (client.getGameState() == GameState.LOGIN_SCREEN || client.getLoginIndex() == 0)
            {
                client.promptCredentials(false);
                API.shortSleep();
            }
            switch (client.getLoginIndex())
            {
                case 2:
                    if (state == GameState.CONNECTION_LOST ||
                            state == GameState.HOPPING ||
                            state == GameState.LOGGING_IN ||
                            state == GameState.LOADING ||
                            state == GameState.STARTING) {
                        API.shortSleep();
                        break;
                    }
                    loginRandomWorld(client, true, user, pass);
                    API.shortSleep();
                    break;
                case 24:
                {
                    client.promptCredentials(false);
                    client.getCallbacks().post(new LoginStateChanged(2));
                }
                break;
            }
        }

        return API.fastReturn();
    }
    private static void loginRandomWorld(Client client, boolean F2P, String user, String pass)
    {
        Worlds.loadWorlds();
        if (client.getWorldList() != null) {
            World randWorld = API.getRandomWorld(F2P);
            final int worldID = randWorld.getId();
            client.changeWorld(randWorld);
            if (Time.sleepUntil(() -> client.getWorld() == worldID, 10000)) {
                client.setUsername(user);
                client.setPassword(pass);
                Mouse.click(299, 322, true);
            }
        } else {
            log.info("Worlds list not loaded yet");
        }
    }
}
