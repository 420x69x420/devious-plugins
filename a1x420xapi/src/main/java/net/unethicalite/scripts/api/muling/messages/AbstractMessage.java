package net.unethicalite.scripts.api.muling.messages;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;

@AllArgsConstructor
abstract public class AbstractMessage
{
    public final MessageType type;

    public JsonElement toJson()
    {
        return (new Gson()).toJsonTree(this);
    }
}

