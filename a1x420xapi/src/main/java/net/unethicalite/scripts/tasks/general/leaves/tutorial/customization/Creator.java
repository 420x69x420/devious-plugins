package net.unethicalite.scripts.tasks.general.leaves.tutorial.customization;

import net.unethicalite.api.commons.Rand;

import java.util.ArrayList;

public class Creator {
	public static ArrayList<attributes> attributeList = new ArrayList<attributes>();
	public static boolean randomized = false;
	public static int headRights;
	public static int headColourRights;
	public static int jawRights;
	public static int armsRights;
	public static int legsRights;
	public static int handsRights;
	public static int torsoColourRights;
	public static int armsColourRights;
	public static int legsColourRights;
	public static int skinColourLefts;
	public static int feetColourRights;
	

	public static int headColourMaxRights;
	public static int torsoColourMaxRights;
	public static int legsColourMaxRights;
	public static int feetColourMaxRights;
	public static void randomizeCreationOrder() {
		if(randomized) return;
		attributeList.clear();
		int rand = Rand.nextInt(1,100);
		if(rand < 25) {
			attributeList.add(attributes.HEAD);
			attributeList.add(attributes.JAW);
			attributeList.add(attributes.ARMS);
			attributeList.add(attributes.HANDS);
			attributeList.add(attributes.LEGS);
			attributeList.add(attributes.SKINCOLOUR);
			attributeList.add(attributes.FEETCOLOUR);
			attributeList.add(attributes.LEGSCOLOUR);
			attributeList.add(attributes.TORSOCOLOUR);
			attributeList.add(attributes.HAIRCOLOUR);
		} else if (rand < 50) {
			attributeList.add(attributes.HAIRCOLOUR);
			attributeList.add(attributes.TORSOCOLOUR);
			attributeList.add(attributes.LEGSCOLOUR);
			attributeList.add(attributes.FEETCOLOUR);
			attributeList.add(attributes.SKINCOLOUR);
			attributeList.add(attributes.LEGS);
			attributeList.add(attributes.HANDS);
			attributeList.add(attributes.ARMS);
			attributeList.add(attributes.JAW);
			attributeList.add(attributes.HEAD);
		} else if (rand < 75) {
			attributeList.add(attributes.SKINCOLOUR);
			attributeList.add(attributes.FEETCOLOUR);
			attributeList.add(attributes.LEGSCOLOUR);
			attributeList.add(attributes.TORSOCOLOUR);
			attributeList.add(attributes.HAIRCOLOUR);
			attributeList.add(attributes.HEAD);
			attributeList.add(attributes.JAW);
			attributeList.add(attributes.ARMS);
			attributeList.add(attributes.HANDS);
			attributeList.add(attributes.LEGS);
		} else {
			attributeList.add(attributes.LEGS);
			attributeList.add(attributes.HANDS);
			attributeList.add(attributes.ARMS);
			attributeList.add(attributes.JAW);
			attributeList.add(attributes.HEAD);
			attributeList.add(attributes.HAIRCOLOUR);
			attributeList.add(attributes.TORSOCOLOUR);
			attributeList.add(attributes.LEGSCOLOUR);
			attributeList.add(attributes.FEETCOLOUR);
			attributeList.add(attributes.SKINCOLOUR);
		}
		rand = Rand.nextInt(1,700);
		if(rand < 100)
		{
			//green color
			headColourMaxRights = 9;
			torsoColourMaxRights = 15;
			legsColourMaxRights = 9;
			feetColourMaxRights = 1;
		} else if(rand < 200)
		{
			//cyan color
			headColourMaxRights = 8;
			torsoColourMaxRights = 20;
			legsColourMaxRights = 20;
			feetColourMaxRights = 5;
		} else if(rand < 300)
		{
			//deep blue color
			headColourMaxRights = 17;
			torsoColourMaxRights = 21;
			legsColourMaxRights = 21;
			feetColourMaxRights = 5;
		} else if(rand < 400)
		{
			//deep red color
			headColourMaxRights = 20;
			torsoColourMaxRights = 24;
			legsColourMaxRights = 24;
			feetColourMaxRights = 4;
		} else if(rand < 500)
		{
			//yellow color
			headColourMaxRights = 5;
			torsoColourMaxRights = 9;
			legsColourMaxRights = 10;
			feetColourMaxRights = 4;
		} else if(rand < 600)
		{
			//black color
			headColourMaxRights = 12;
			torsoColourMaxRights = 16;
			legsColourMaxRights = 16;
			feetColourMaxRights = 3;
		} else
		{
			//purple color
			headColourMaxRights = 11;
			torsoColourMaxRights = 27;
			legsColourMaxRights = 27;
			feetColourMaxRights = 3;
		}
		randomized = true;
	}
	public static enum attributes {
		HEAD,JAW,ARMS,HANDS,LEGS,HAIRCOLOUR,TORSOCOLOUR,LEGSCOLOUR,FEETCOLOUR,SKINCOLOUR
	}
	
}
