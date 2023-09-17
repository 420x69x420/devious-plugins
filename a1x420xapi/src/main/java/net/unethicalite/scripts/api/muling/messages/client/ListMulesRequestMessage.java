package net.unethicalite.scripts.api.muling.messages.client;

import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;

public class ListMulesRequestMessage extends AbstractMessage
{
	public ListMulesRequestMessage()
	{
		super(MessageType.LIST_MULES_REQUEST);
	}
}
