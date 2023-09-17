package net.unethicalite.scripts.api.muling.messages.client;


import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;
import net.unethicalite.scripts.api.muling.messages.OwnedItem;

import java.util.List;

public class OwnedItemsUpdateMessage extends AbstractMessage
{
	public final List<OwnedItem> ownedItems;

	public OwnedItemsUpdateMessage(List<OwnedItem> ownedItems)
	{
		super(MessageType.OWNED_ITEMS_UPDATE);
		this.ownedItems = ownedItems;
	}
}
