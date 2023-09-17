package net.unethicalite.scripts.tasks.general.leaves;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.entities.Players;
import net.unethicalite.scripts.framework.Leaf;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

@Slf4j
public class UniqueActions extends Leaf {
	private static LinkedHashMap<Actionz,Character> uniqueActions = new LinkedHashMap<>();
	@Override
	public boolean isValid() {
		return uniqueActions.isEmpty();
	}

	@Override
	public int execute() {
		initialize();
		return 100;
	}

	public enum Actionz {
		USE_GE_BANKER,
		SCRIPT_CUSTOM_ACTION_1,
		SCRIPT_CUSTOM_ACTION_2,
		SCRIPT_CUSTOM_ACTION_3,
		SCRIPT_CUSTOM_ACTION_4,
		SCRIPT_CUSTOM_ACTION_5,
		SCRIPT_CUSTOM_ACTION_6,
		SCRIPT_CUSTOM_ACTION_7,
		SCRIPT_CUSTOM_ACTION_8,
		SCRIPT_CUSTOM_ACTION_9,
		SCRIPT_CUSTOM_ACTION_10
	}
	public static boolean isActionEnabled(Actionz action)
	{
		if(action == null) return false;
		return isHexCharTrue(uniqueActions.get(action));
	}
	public static boolean isActionAbovePercent(Actionz action, int percent)
	{
		if(action == null) return false;
		return isHexCharAbovePercentage(uniqueActions.get(action), percent);
	}
	public static void initialize()
	{
		String username = Players.getLocal().getName();
		String hash = getHash(username);
		for(int i = 0; i <= 32; i++)
		{
			switch(i)
			{
			case(2): uniqueActions.put(Actionz.USE_GE_BANKER,hash.charAt(i));break;
			case(3): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_2,hash.charAt(i));break;
			case(4): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_3,hash.charAt(i));break;
			case(5): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_4,hash.charAt(i));break;
			case(6): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_5,hash.charAt(i));break;
			case(7): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_6,hash.charAt(i));break;
			case(8): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_7,hash.charAt(i));break;
			case(9): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_8,hash.charAt(i));break;
			case(10): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_9,hash.charAt(i));break;
			case(11): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_10,hash.charAt(i));break;
			case(12): uniqueActions.put(Actionz.SCRIPT_CUSTOM_ACTION_1,hash.charAt(i));break;
			default:break;
			}
		}
		log.info("Set antiban profile based on MD5 Hash of Player username:"+hash);
		printAllActionParameters();
	}
	
	
	private static void printAllActionParameters()
	{
		for(Entry<Actionz,Character> mapEntry : uniqueActions.entrySet())
		{
			log.info("Parameter: " + mapEntry.getKey().toString()+" is " + (isActionEnabled(mapEntry.getKey()) ? "ENABLED" : "DISABLED"));
		}
	}
	
	private static String getHash(String username)
	{
		try
		{
			// invoking the static getInstance() method of the MessageDigest class  
			// Notice it has MD5 in its parameter.  
			MessageDigest msgDst = MessageDigest.getInstance("MD5");  
			  
			// the digest() method is invoked to compute the message digest  
			// from an input digest() and it returns an array of byte  
			byte[] msgArr = msgDst.digest(username.getBytes());  
			  
			// getting signum representation from byte array msgArr  
			BigInteger bi = new BigInteger(1, msgArr);  
			  
			// Converting into hex value  
			String hshtxt = bi.toString(16);  
			  
			while (hshtxt.length() < 32)   
			{  
				hshtxt = "0" + hshtxt;  
			}  
			
			return hshtxt; 
		}
		catch (NoSuchAlgorithmException abc)   
		{  
			throw new RuntimeException(abc);  
		}
	}
	/**
	 * Converts Hash Hex char to true/false
	 * Hex chars = 0-9, a-f.
	 * 16 possibilities, so first 8 possibilities return true, others false.
	 */
	public static boolean isHexCharTrue(char c)
	{
		if(c == "0".charAt(0) || c == "1".charAt(0) || c == "2".charAt(0) ||  c == "3".charAt(0) || 
				c == "4".charAt(0) || c == "5".charAt(0) || c == "6".charAt(0) || c == "7".charAt(0))
		{
			return true;
		}
		return false;
	}

	public static boolean isHexCharAbovePercentage(char c, int percent) {
		int decimalValue = Character.digit(c, 16);
		if (decimalValue == -1) {
			// Invalid hex character
			return false;
		}

		double hexPercentage = ((double) decimalValue / 15) * 100;
		return percent > hexPercentage;
	}
}
