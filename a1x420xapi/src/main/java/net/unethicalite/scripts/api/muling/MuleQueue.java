package net.unethicalite.scripts.api.muling;

import com.google.inject.Singleton;
import net.unethicalite.scripts.api.muling.messages.client.MuleRequestMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@Singleton
public class MuleQueue {
    public static final List<String> activeTraders = new CopyOnWriteArrayList<>();
    public static final List<MuleRequestMessage> activeRequests = new CopyOnWriteArrayList<>();
}
