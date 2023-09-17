package net.unethicalite.scripts.tasks.general.leaves.tutorial.customization;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.api.extended.ExKeyboard;

import java.awt.event.KeyEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
@Slf4j
public class TypeName {

	/**
	 * Optional action = [Look up name]
	 * @return
	 */
	public static Widget getLookupNameButton() {
		return Widgets.get(558, 18);
	}
	public static Widget getLookupStatusText() {
		return Widgets.get(558, 13);
	}

	/**
	 * Action = [Enter name]
	 * @return
	 */
	public static Widget getEnterNameButton() {
		return Widgets.get(558, 7);
	}
	public static Widget getNameFieldText() {
		return Widgets.get(558, 12);
	}

	/**
	 * Action = [Set name]
	 * @return
	 */
	public static Widget getSetNameButton() {
		return Widgets.get(558, 19);
	}

    public static void onLoop() {
    	Widget setNameButton = getSetNameButton();
		Widget lookupNameButton = getLookupNameButton();
		Widget nameFieldText = getNameFieldText();
		Widget lookupStatusText = getLookupStatusText();
		Widget enterNameButton = getEnterNameButton();
    	if(lookupStatusText != null && lookupStatusText.isVisible() &&
				(lookupStatusText.getText().contains("Please look up a name to see whether it is available.") ||
						lookupStatusText.getText().contains("Please enter a name to look up")))
    	{
    		if(lookupNameButton != null && lookupNameButton.hasAction("Look up name"))
			{
				while ((nameFieldText = getNameFieldText()) != null && nameFieldText.isVisible() && nameFieldText.getText().contains("*") && nameFieldText.getText().length() > 1) {
					ExKeyboard.pressSpecialKey(KeyEvent.VK_BACK_SPACE);
				}
				typeAdjName();
			}
			else
    		{
				enterNameButton.interact("Enter name");
    			Time.sleepUntil(() -> {
					Widget nameFieldTextAgain = getNameFieldText();
					return nameFieldTextAgain != null && nameFieldTextAgain.isVisible() && nameFieldTextAgain.getText().contains("*");
				}, 500, 5000);
    		}
    	}
    	else if(lookupStatusText != null && lookupStatusText.isVisible() && lookupStatusText.getText().contains("Great! The display name"))
    	{
			setNameButton.interact("Set name");
			log.info("Confirming name");
			Time.sleepUntil(() -> {
				Widget w = getLookupStatusText();
				return w == null || !w.isVisible();
			}, 300, 5000);
    	}
    	else if(lookupStatusText != null &&
    			lookupStatusText.isVisible() &&
    			(lookupStatusText.getText().contains("Sorry, the display name") ||
    					lookupStatusText.getText().contains("could not be claimed") ||
    					lookupStatusText.getText().contains("This system is currently")))
    	{
    		if(nameFieldText != null && 
        			nameFieldText.isVisible())
        	{
				while ((nameFieldText = getNameFieldText()) != null && nameFieldText.isVisible() && nameFieldText.getText().contains("*") && nameFieldText.getText().length() > 1) {
					ExKeyboard.pressSpecialKey(KeyEvent.VK_BACK_SPACE);
				}
    			if(nameFieldText.getText().contains("*"))
    			{
					typeAdjName();
					return;
				}
    			else if(enterNameButton != null && enterNameButton.isVisible() && enterNameButton.getActions()[0].contains("Enter name"))
    	    	{
					enterNameButton.interact("Enter name");
					Time.sleepUntil(() -> {
						Widget w = getNameFieldText();
						return w != null && w.isVisible() && w.getText().contains("*");
					}, 300, 5000);
    	    	}
    		}
    	}
    }
	private static final String BASE_URL = "https://random-name-generator.dreambotter420.repl.co/";
	public static String parseValue(String jsonString) {
		String key = "\"wordfound\":";
		int startIndex = jsonString.indexOf(key);

		if (startIndex == -1) {
			return "Key not found";
		}

		startIndex += key.length();
		int endIndex = jsonString.indexOf("}", startIndex);
		String value = jsonString.substring(startIndex, endIndex).replace("\"", "").trim();

		return value;
	}
	public static String getWordFromRepl() throws IOException {
		try {
			URL url = new URL(BASE_URL + "get_name/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				throw new IOException("Unexpected code " + responseCode);
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print raw response
			log.info("RAW RESPONSE: " + response);
			String parsedValue = parseValue(response.toString());
			log.info("PARSED VALUE: "+parsedValue);

			return parsedValue;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String getRandomWord() {
		List<String> strs = getWordsFromFile();
		int randIndex = Rand.nextInt(0,strs.size()-1);
		return strs.get(randIndex);
	}
	private static boolean shouldSkipWord(int len, int randChance) {
		//hard limit of character range
		if (len < 1 || len > 12) {
			return true;
		}

		//random chance of shorter name, high preference for less than 10-12, less-high preference for less than 5-9
		if (len == 12 && randChance > 1) {
			return true;
		} else if (len == 11 && randChance > 2) {
			return true;
		} else if (len == 10 && randChance > 3) {
			return true;
		} else if (len == 9 && randChance > 15) {
			return true;
		} else if (len == 8 && randChance > 20) {
			return true;
		} else if (len == 7 && randChance > 25) {
			return true;
		} else if (len == 6 && randChance > 45) {
			return true;
		} else if (len == 5 && randChance > 65) {
			return true;
		}
		return false;
	}
    public static List<String> getWordsFromFile()
	{
		File adjFile = new File("C:\\Users\\USER\\Desktop\\words.txt");
		List<String> allAdjText = new ArrayList<String>();
		try {
			Scanner scanr = new Scanner(adjFile);
    		while(scanr.hasNextLine())
    		{
    			allAdjText.add(scanr.nextLine());
    		}
    		scanr.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		List<String> adjs = new ArrayList<>();

		int randChance = Rand.nextInt(0,100);
		for(String adjLine : allAdjText)
		{
			if(adjLine.isBlank()) continue;
			if(adjLine.contains(","))
			{
				String[] adjsInLine = adjLine.split(",");
				for(String adj : adjsInLine)
				{
					adj = adj.strip();
					int len = adj.length();
					if (shouldSkipWord(len, randChance)) {
						continue;
					}
					adjs.add(adj);
				}
			}
			else {
				adjLine = adjLine.strip();
				int len = adjLine.length();
				if (shouldSkipWord(len, randChance)) {
					continue;
				}
				adjs.add(adjLine);
			}
		}
		return adjs;
	}
	public static String generateName(String randWord)
	{
		if(randWord == null || randWord.isEmpty()) return null;
		boolean foundOne = false;
		String name = "";
		while(!foundOne)
		{
			String epicNumber = (Rand.nextInt(1,100) > 85 ? "69" : "420");
			boolean face = false;
			if(Rand.nextInt(75,125) >= 115) face = true;
			boolean addSpace = false;
			if(Rand.nextInt(75,125) >= 115) addSpace = true;
			boolean addUnderscore = false;
			if(!addSpace && !face && Rand.nextInt(75,125) >= 115) addUnderscore = true;
			boolean cap = false;
			if(!face && Rand.nextInt(75,125) >= 105) cap = true;
			boolean x = false;
			if(!addSpace && !addUnderscore && Rand.nextInt(75,125) >= 115) x = true;
			boolean xx = false;
			if(!addSpace && !addUnderscore && !x && Rand.nextInt(75,125) >= 115) xx = true;
			boolean four201st = false;
			if(!face && Rand.nextInt(75,125) >= 115) four201st = true;
			name = "";
			String firstPart = "";
			String secondPart = "";
			if(face)
			{
				randWord = getRandomFace();
			}
			if(cap) randWord = randWord.substring(0, 1).toUpperCase() + randWord.substring(1);
			
			if(four201st) 
			{
				firstPart = epicNumber;
				secondPart = randWord;
			}
			else 
			{
				firstPart = randWord;
				secondPart = epicNumber;
			}
			name = name.concat(firstPart);
			if(addSpace) name = name.concat(" ");
			if(addUnderscore) 
			{
				if(face)
				{
					if(Rand.nextInt(75,125) >= 115) name = name.concat(" - ");
					else name = name.concat(" _ ");
				}
				else 
				{
					if(Rand.nextInt(75,125) >= 115) name = name.concat("-");
					else name = name.concat("_");
				}
			}
			
			if(xx)
			{
				if(face) name = name.concat(" xx ");
				else name = name.concat("xx");
			}
			if(x)
			{
				if(face) name = name.concat(" x ");
				else name = name.concat("x");
			}
			name = name.concat(secondPart);
			log.info("Name generated|" + name);
			if(name.length() > 12) log.info("Name generated too long (chars: " + name.length()+")! Trying again..");
			else if(name.length() <= 9)
			{
				if(name.length() > 8)
				{
					String anotherFace = getRandomFace();
					log.info("Adding face|"+anotherFace);
					name = name.concat(anotherFace);
					log.info("Name generated|" + name);
					foundOne = true;
				}
				if(name.length() > 7)
				{
					String anotherFace = getRandomFace();
					log.info("Adding face with space|"+anotherFace);
					name = name.concat(" " + anotherFace);
					log.info("Name generated|" + name);
					foundOne = true;
				}
				if(name.length() > 5)
				{
					String anotherFace = getRandomFace();
					log.info("Adding face with space-space|"+anotherFace);
					if(Rand.nextInt(75,125) >= 115) name = name.concat(" - ");
					else name = name.concat(" _ ");
					name = name.concat(anotherFace);
					log.info("Name generated|" + name);
					foundOne = true;
				}
			}
			else foundOne = true;
		}
		return name;
	}
	
	public static String getRandomFace()
	{
		boolean underscore = false;
		String first = "";
		String second = "";
		String mid = "";
		if(Rand.nextInt(75,125) >= 105) underscore = true;
		if(underscore) mid = "_";
		else mid = "-";
		int decider = Rand.nextInt(0, 12);
		switch (decider)
		{
		case(0):
		{
			if(Rand.nextInt(75,125) >= 115) first = "O";
			else if(Rand.nextInt(75,125) >= 85) first = "0";
			else first = "o";
			
			if(first.equals("O") && Rand.nextInt(75,125) >= 115) second = "0";
			else second = "O";
			
			if(first.equals("o")) second = "o";
			break;
		}
		case(1):
		{
			first = second = "v";
			break;
		}
		case(2):
		{
			first = second = "V";
			break;
		}
		case(3):
		{
			first = second = "x";
			break;
		}
		case(4):
		{
			first = second = "X";
			break;
		}
		case(5):
		{
			if(mid.equals("_"))
			{
				first = second = "L";
			}
			else
			{
				first = second = "u";
			}
			break;
		}
		case(6):
		{
			first = second = "u";
			break;
		}
		case(7):
		{
			first = second = "U";
			break;
		}
		case(8):
		{
			if(mid.equals("_"))
			{
				first = second = "p";
			}
			else
			{
				first = second = "Q";
			}
			break;
		}
		case(9):
		{
			first = second = "Q";
			break;
		}
		case(10):
		{
			if(mid.equals("_"))
			{
				first = second = "Y";
			}
			else if (mid.equals("-"))
			{
				first = second = "n";
			}
			else
			{
				first = second = "x";
			}
			break;
		}
		default:
		{
			first = second = "n";
			break;
		}
		}
		return first + mid + second;
	}
	public static void typeAdjName() {
		String name = null;
		name = generateName(getRandomWord());
		/*try {
			//name = generateName(getWordFromRepl());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/
		if(name != null) {
			Keyboard.type(name,true);
			String currentText = getLookupStatusText().getText();
			Time.sleepUntil(() -> {
				Widget newText = getLookupStatusText();
				return newText == null || !newText.isVisible() || !newText.getText().equals(currentText);
			}, 300, 5000);
		}
		else log.info("Something wrong! No generated name (null)");
	}
    public static void type420Name()
    {
    	try
		{
    		log.info("beginning Trying file operations");
    		String userHomeDBTempRemoved = System.getProperty("user.home").split("DBFolderCache")[0];
    		File usernamesFile = new File(userHomeDBTempRemoved+"\\DreamBot\\Scripts\\usernames.txt");
    		
    		List<String> usernames = new ArrayList<String>();
    		if(usernames.size() > 0)
    		{
    			log.info(usernames.size() + " is the size of usernames nonnull");
    		}
    		else log.info("usernames null from path " + System.getProperty("user.home"));
    		
    		Scanner scanr = new Scanner(usernamesFile);
    		while(scanr.hasNextLine())
    		{
    			usernames.add(scanr.nextLine());
    		}
    		scanr.close();
    		if(usernames.get(0) != null)
    		{
    			String name = usernames.get(0);
    			log.info("Beginning username removal/re-write after selecting Tutorial Island RSN: " + name);
    			usernames.remove(0);
        		File outFile = null;
        		FileWriter ffs = null;
        		BufferedWriter bw = null;
        		outFile = new File(userHomeDBTempRemoved+"\\DreamBot\\Scripts\\usernames.txt");
        		ffs = new FileWriter(outFile);
        		bw = new BufferedWriter(ffs);
        		for(String username : usernames)
        		{
        			bw.write(username);
        			bw.newLine();
        			bw.flush();
        		}
        		ffs.close();
        		log.info("end username removal");
    			Keyboard.type(name, true);
				String currentText = getLookupStatusText().getText();
				Time.sleepUntil(() -> {
					Widget newText = getLookupStatusText();
					return newText == null || !newText.isVisible() || !newText.getText().equals(currentText);
				}, 300, 5000);
    		}
    		else
    		{
    			log.info("No more random names :-( Script stopping...");
				Plugins.getPluginManager().stopPlugin(InterfaceInstance.pluginInterface.plugin());
    			return;
    		}
		}
		catch(Exception whatever)
		{
			whatever.printStackTrace();
		}
	}
}



