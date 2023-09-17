package net.unethicalite.scripts.tasks.general.leaves.tutorial.customization;


import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.scripts.api.utils.Sleep;

@Slf4j
public class CharacterPainter {
	public static Widget getPainterMenu() {
		return Widgets.get(679, 3, 1);
	}
	public static Widget getPainterConfirmationButton() {
		return Widgets.get(679,68);
	}
    public static void onLoop() {
    	/**character creator menu
    	 * following method creates the exact appearance I want my characters
    	 */
    	Creator.randomizeCreationOrder();

    	if (Creator.headRights < 7 ||
    			Creator.jawRights < 1 ||
    			Creator.armsRights < 3 ||
    			Creator.legsRights < 5 ||
    			Creator.handsRights < 1 ||
    			Creator.headColourRights < Creator.headColourMaxRights ||
    			Creator.torsoColourRights < Creator.torsoColourMaxRights ||
    			Creator.legsColourRights < Creator.legsColourMaxRights ||
    			Creator.feetColourRights < Creator.feetColourMaxRights ||
    			Creator.skinColourLefts < 2) 
    	{
			Sleep.shortSleep();
    		if(Creator.attributeList.get(0).equals(Creator.attributes.HEAD)) {
        		if(Creator.headRights >= 7) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,13).interact("Select");
					Creator.headRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.JAW)) {
        		if(Creator.jawRights >= 1) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,17).interact("Select");
					Creator.jawRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.ARMS)) {
        		if(Creator.armsRights >= 3) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,25).interact("Select");
					Creator.armsRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.LEGS)) {
        		if(Creator.legsRights >= 5) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,33).interact("Select");
					Creator.legsRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.HANDS)) {
        		if(Creator.handsRights >= 1) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,28).interact("Select");
					Creator.handsRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.HAIRCOLOUR)) {
        		if(Creator.headColourRights >= Creator.headColourMaxRights) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,44).interact("Select");
					Creator.headColourRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.TORSOCOLOUR)) {
        		if(Creator.torsoColourRights >= Creator.torsoColourMaxRights) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,48).interact("Select");
					Creator.torsoColourRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.LEGSCOLOUR)) {
        		if(Creator.legsColourRights >= Creator.legsColourMaxRights) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,52).interact("Select");
					Creator.legsColourRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.FEETCOLOUR)) {
        		if(Creator.feetColourRights >= Creator.feetColourMaxRights) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,56).interact("Select");
					Creator.feetColourRights++;
        		}
        	} else if(Creator.attributeList.get(0).equals(Creator.attributes.SKINCOLOUR)) {
        		if(Creator.skinColourLefts >= 2) Creator.attributeList.remove(0);
        		else {
					Widgets.get(679,59).interact("Select");
					Creator.skinColourLefts++;
        		}
        	}
			return;
    	}
		Time.sleepTick();
		Widget confirm = getPainterConfirmationButton();
		if (confirm != null && confirm.isVisible()) {
			confirm.interact("Confirm");
			Time.sleepTick();
			log.info("ALL DONE PAINTING CHARACTER! Confirming");
		}
    }
}
