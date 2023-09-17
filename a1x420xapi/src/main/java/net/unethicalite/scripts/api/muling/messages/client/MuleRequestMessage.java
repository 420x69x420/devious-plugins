package net.unethicalite.scripts.api.muling.messages.client;



import net.unethicalite.scripts.api.muling.OfferedItem;
import net.unethicalite.scripts.api.muling.RequiredItem;
import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;

import java.util.List;

public class MuleRequestMessage extends AbstractMessage
{
	public final String requestId;
	public final long requestedAt;
	public final String playerName;
	public final boolean hasMembership;
	public final List<RequiredItem> requiredItems;
	public final List<OfferedItem> offeredItems;
	public final String muleName;

	public MuleRequestMessage(String requestId, long requestedAt, String playerName, boolean hasMembership, List<RequiredItem> requiredItems, List<OfferedItem> offeredItems, String muleName)
	{
		super(MessageType.MULE_REQUEST);
		this.requestId = requestId;
		this.requestedAt = requestedAt;
		this.playerName = playerName;
		this.hasMembership = hasMembership;
		this.requiredItems = requiredItems;
		this.offeredItems = offeredItems;
		this.muleName = muleName;
	}
}
