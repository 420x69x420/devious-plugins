package net.unethicalite.scripts.api.muling.messages.server;


import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;
import net.unethicalite.scripts.api.muling.messages.Mule;

import java.util.List;

public class ListMulesResponseMessage extends AbstractMessage
{
	public final boolean success;
	public final String errorMessage;
	public final List<Mule> mules;

	public ListMulesResponseMessage(boolean success, String errorMessage, List<Mule> mules)
	{
		super(MessageType.LIST_MULES_RESPONSE);
		this.success = success;
		this.errorMessage = errorMessage;
		this.mules = mules;
	}
}
