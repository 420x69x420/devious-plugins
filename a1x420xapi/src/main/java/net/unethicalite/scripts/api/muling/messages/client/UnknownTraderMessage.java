package net.unethicalite.scripts.api.muling.messages.client;


import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;

public class UnknownTraderMessage extends AbstractMessage
{
	public final String playerName;

	public UnknownTraderMessage(String playerName)
	{
		super(MessageType.UNKNOWN_TRADER);
		this.playerName = playerName;
	}
}
