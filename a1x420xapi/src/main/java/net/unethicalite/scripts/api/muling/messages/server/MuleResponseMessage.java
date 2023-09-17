package net.unethicalite.scripts.api.muling.messages.server;


import net.unethicalite.scripts.api.muling.messages.AbstractMessage;
import net.unethicalite.scripts.api.muling.messages.MessageType;
import net.unethicalite.scripts.api.muling.messages.MuleTile;

public class MuleResponseMessage extends AbstractMessage
{
	public final boolean success;
	public final String errorMessage;
	public final int world;
	public final MuleTile location;

	public MuleResponseMessage(boolean success, String errorMessage, int world, MuleTile location)
	{
		super(MessageType.MULE_RESPONSE);
		this.success = success;
		this.errorMessage = errorMessage;
		this.world = world;
		this.location = location;
	}
}
