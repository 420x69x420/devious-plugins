package net.unethicalite.scripts.api.muling.messages;

import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
@ToString
public class Mule
{
	public String playerName;
	public String[] groups;
	public int worldId;
	public MuleTile tile;
	public boolean member;
	public List<OwnedItem> ownedItems;
	public List<OwnedItem> remainingItems;
	public int queueSize;
	public int requestCount;
}
