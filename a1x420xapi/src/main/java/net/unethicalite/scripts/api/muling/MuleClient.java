package net.unethicalite.scripts.api.muling;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.items.Trade;
import net.unethicalite.api.util.Text;
import net.unethicalite.scripts.api.muling.messages.MessageType;
import net.unethicalite.scripts.api.muling.messages.OwnedItem;
import net.unethicalite.scripts.api.muling.messages.client.MuleRequestMessage;
import net.unethicalite.scripts.api.muling.messages.client.OwnedItemsUpdateMessage;
import net.unethicalite.scripts.api.muling.messages.client.TradeCompletedMessage;
import net.unethicalite.scripts.api.muling.messages.client.UnknownTraderMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MuleClient extends WebSocketClient {

    public MuleClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

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
                case MULE_REQUEST: {
                    MuleRequestMessage muleRequest = new Gson().fromJson(jsonElement, MuleRequestMessage.class);
                    log.info("Found "+messageType+" msg from server1");
                    MuleQueue.activeRequests.add(muleRequest);
                    break;
                }
                case UNKNOWN_TRADER: {
                    UnknownTraderMessage traderMessage = new Gson().fromJson(jsonElement, UnknownTraderMessage.class);
                    log.info("Adding trader to list: "+traderMessage.playerName+", size after: "+ (MuleQueue.activeTraders.size() + 1));
                    MuleQueue.activeTraders.add(traderMessage.playerName);
                    break;
                }
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

    public void sendTradeCompletedMessage(MuleRequestMessage originatingRequest, boolean successful, String reason) {
        TradeCompletedMessage tradeCompletedMessage = new TradeCompletedMessage(successful, reason, originatingRequest.requestId);
        String jsonMessage = new Gson().toJson(tradeCompletedMessage);
        this.send(jsonMessage);
    }
    public void verifyUnknownTrader(String name) {
        log.info("Pinging server with unknown trader name: "+name);
        UnknownTraderMessage unknownTraderMessage = new UnknownTraderMessage(name);
        String jsonMessage = new Gson().toJson(unknownTraderMessage);
        this.send(jsonMessage);
    }
    public void updateOwnedItems(List<OwnedItem> allOwnedItems) {
        OwnedItemsUpdateMessage ownedItemsUpdateMessage = new OwnedItemsUpdateMessage(allOwnedItems);
        String jsonMessage = new Gson().toJson(ownedItemsUpdateMessage);

        this.send(jsonMessage);
    }

    public MuleRequestMessage getRequestForBotName(String botName) {
        return MuleQueue.activeRequests.stream()
                .filter(activeRequest -> activeRequest.playerName.equals(botName))
                .findAny()
                .orElseGet(null);
    }

    @Subscribe
    public void onChatMessage(final ChatMessage msg) {
        String name = Text.sanitize(msg.getName());
        if (msg.getType().equals(ChatMessageType.TRADEREQ)) {
            this.verifyUnknownTrader(name);
        }

        if (msg.getType().equals(ChatMessageType.TRADE)) {
            if (msg.getMessage().equals("Accepted trade.")) {
                if (!MuleQueue.activeRequests.isEmpty()) {
                    this.sendTradeCompletedMessage(MuleQueue.activeRequests.remove(0), true, "Bot: "+ MuleQueue.activeTraders.remove(0)+"Accepted trade");
                }
            }
            if (msg.getMessage().equals("Other player declined trade.")) {
                if (!MuleQueue.activeRequests.isEmpty()) {
                    this.sendTradeCompletedMessage(MuleQueue.activeRequests.remove(0), false, "Bot: "+ MuleQueue.activeTraders.remove(0)+" declined trade.");
                }
            }
            if (msg.getMessage().equals("Other player doesn't have enough inventory space for this trade.")) {
                if (!MuleQueue.activeRequests.isEmpty()) {
                    this.sendTradeCompletedMessage(MuleQueue.activeRequests.remove(0), false, "Bot: "+ MuleQueue.activeTraders.remove(0)+" doesn't have enough inventory space for this trade.");

                }
            }
        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != InventoryID.INVENTORY.getId()) {
            return;
        }
        // only update owned items if trade is not open
        if (Trade.isOpen()) {
            return;
        }
        // Create a map to hold item IDs and their aggregated quantities
        Map<Integer, Integer> itemMap = new HashMap<>();

        // Loop through all items in the container
        for (Item i : event.getItemContainer().getItems()) {
            if (i != null && i.getName() != null) {
                // Aggregate item quantities by their IDs
                itemMap.put(i.getId(), itemMap.getOrDefault(i.getId(), 0) + i.getQuantity());
            }
        }

        // Convert the map to a list of OwnedItem objects
        List<OwnedItem> ownedItems = itemMap.entrySet().stream()
                .map(entry -> new OwnedItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        this.updateOwnedItems(ownedItems);

    }
}
