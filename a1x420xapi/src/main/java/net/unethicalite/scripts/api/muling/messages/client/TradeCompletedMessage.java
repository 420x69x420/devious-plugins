package net.unethicalite.scripts.api.muling.messages.client;


import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;

public class TradeCompletedMessage extends AbstractMessage
{
	public final boolean success;
	public final String reason;
	public final String requestId;

	public TradeCompletedMessage(boolean success, String reason, String requestId)
	{
		super(MessageType.TRADE_COMPLETED);
		this.success = success;
		this.reason = reason;
		this.requestId = requestId;
	}
}
