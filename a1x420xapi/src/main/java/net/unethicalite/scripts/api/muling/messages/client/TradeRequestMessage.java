package net.unethicalite.scripts.api.muling.messages.client;


import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;

public class TradeRequestMessage extends AbstractMessage
{
	public final String requestId;

	public TradeRequestMessage(String requestId)
	{
		super(MessageType.TRADE_REQUEST);
		this.requestId = requestId;
	}
}
