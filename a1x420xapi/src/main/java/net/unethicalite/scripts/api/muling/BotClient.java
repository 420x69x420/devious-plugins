package net.unethicalite.scripts.api.muling;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.util.Text;
import net.unethicalite.scripts.api.muling.messages.MessageType;
import net.unethicalite.scripts.api.muling.messages.client.MuleRequestMessage;
import net.unethicalite.scripts.api.muling.messages.client.TradeCompletedMessage;
import net.unethicalite.scripts.api.muling.messages.client.TradeRequestMessage;
import net.unethicalite.scripts.api.muling.messages.server.MuleResponseMessage;
import net.unethicalite.scripts.api.muling.messages.server.TradeResponseMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class BotClient extends WebSocketClient {
    public BotClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }
    public MuleResponseMessage muleResponseMessage = null;
    public MuleRequestMessage muleRequestMessage = null;
    public TradeResponseMessage tradeResponseMessage = null;
    public TradeRequestMessage tradeRequestMessage = null;
    public TradeCompletedMessage tradeCompletedMessage = null;
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Connected to central mule comms server");
    }

    @Override
    public void onMessage(String message) {
        try {
            // Parse the incoming JSON message
            JsonElement jsonElement = new JsonParser().parse(message);
            if (jsonElement == null) {
                return;
            }

            // Get the message type
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("type")) {
                return;
            }
            MessageType messageType = MessageType.valueOf(jsonObject.get("type").getAsString());

            // Evaluate message type
            switch (messageType) {
                case MULE_RESPONSE: {
                    this.muleResponseMessage = new Gson().fromJson(jsonElement, MuleResponseMessage.class);
                }
                break;
                case TRADE_RESPONSE: {
                    this.tradeResponseMessage = new Gson().fromJson(jsonElement, TradeResponseMessage.class);
                }
                break;
                case TRADE_COMPLETED: {
                    this.tradeCompletedMessage = new Gson().fromJson(jsonElement, TradeCompletedMessage.class);
                }
                break;

            }
        } catch (Exception e) {
            log.info("Exception in onMessage: "+e);
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        // Handle closure here
        log.info("Closing websocket client: "+i+" "+s);
    }

    @Override
    public void onError(Exception e) {
        // Handle errors here
    }
    public void sendMuleRequest(List<RequiredItem> requiredItems, List<OfferedItem> offeredItems) {
        this.muleRequestMessage = new MuleRequestMessage(UUID.randomUUID().toString(), Instant.now().toEpochMilli(), Players.getLocal().getName(), Game.getMembershipDays() > 0, requiredItems, offeredItems, null);
        String jsonMessage = new Gson().toJson(this.muleRequestMessage);
        this.send(jsonMessage);  // 'this' refers to WebSocketClient instance
    }
    public void sendTradeCompletedMessage(boolean successful, String reason) {
        this.tradeCompletedMessage = new TradeCompletedMessage(successful, reason, this.muleRequestMessage.requestId);
        String jsonMessage = new Gson().toJson(tradeCompletedMessage);
        this.send(jsonMessage);
    }
    public void sendTradeRequestMessage() {
        this.tradeRequestMessage = new TradeRequestMessage(this.muleRequestMessage.requestId);
        String jsonMessage = new Gson().toJson(this.tradeRequestMessage);
        this.send(jsonMessage);
    }
    public void requestTraderName() {
        TradeRequestMessage tradeRequestMessage = new TradeRequestMessage(this.muleRequestMessage.requestId);
        String jsonMessage = new Gson().toJson(tradeRequestMessage);
        this.send(jsonMessage);
    }

    @Subscribe
    public void onChatMessage(final ChatMessage msg) {
        String name = Text.sanitize(msg.getName());
        if (msg.getType().equals(ChatMessageType.TRADE)) {
            if (msg.getMessage().equals("Accepted trade.")) {
                this.sendTradeCompletedMessage( true, "Accepted trade");
            }
            if (msg.getMessage().equals("Other player declined trade.")) {
                this.sendTradeCompletedMessage(false, "Bot declined trade.");
            }
            if (msg.getMessage().equals("Other player doesn't have enough inventory space for this trade.")) {
                this.sendTradeCompletedMessage(false, "Bot doesn't have enough inventory space for this trade.");
            }
        }
    }
}
