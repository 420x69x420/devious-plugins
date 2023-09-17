package net.unethicalite.scripts.runner;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;

import javax.inject.Inject;

@Slf4j
public class CustomizeSettings {
	private final Client client;
	public static final int ACCEPT_TRADE_DELAY_VARBIT = 13130;
	public static int acceptTradeDelayVarbit = 0;
	public static final int HOP_WORLDS_WARNING_VARBIT = 4100;
	public static int hopWorldsWarningVarbit = 0;
	public static final int SETTINGS_TAB_VISIBLE_VARBIT = 9656;
	public static int settingsTabVisibleVarbit = 0;
	public static final int SETTINGS_INTERFACES_VARBIT_VALUE = 6;
	public static final int SETTINGS_WARNING_VARBIT_VALUE = 7;

	@Inject
	public CustomizeSettings(final Client client)
	{
		this.client = client;
	}
	 public static boolean validate()
	 {
		 Widget w = getSettingsExitButton();
	    return isTradeAcceptOn() || isWorldHopWarningOn() || (w != null && w.isVisible());
	 }
	 public static boolean isTradeAcceptOn() {
		return acceptTradeDelayVarbit == 0;
	 }
	public static boolean isWorldHopWarningOn() {
		return hopWorldsWarningVarbit == 0;
	}
	private static Widget getSettingsExitButton() {
		return Widgets.get(134,4);
	}
	private static Widget getAllSettingsButton() {
		return Widgets.get(116, w -> w.isVisible() &&
				(w.hasAction("All Settings")));
	}
    public static int execute(Client client) {
		Widget closeSettingsButton = getSettingsExitButton();
		if (isTradeAcceptOn()) {
			if (closeSettingsButton != null && closeSettingsButton.isVisible()) {
				if (settingsTabVisibleVarbit != SETTINGS_INTERFACES_VARBIT_VALUE) {
					Widget w = Widgets.get(134,23,6);
					if (w != null && w.isVisible()) {
						w.interact("Select Interfaces");
					}
					//click "Interfaces" sub-tab
					//client.invokeWidgetAction(57, 8781847, 6, -1, "");
					return API.returnTick();
				}
				//click "Toggle" on accept trade delay
				Widget w = Widgets.get(134,19,19);
				if (w != null && w.isVisible()) {
					w.interact("Toggle");
				}
				//client.invokeWidgetAction(57, 8781843, 19, -1, "");
				return API.returnTick();
			}
			if (!Tabs.isOpen(Tab.OPTIONS)) {
				Tabs.open(Tab.OPTIONS);
				return API.returnTick();
			}
			Widget allSettings = getAllSettingsButton();
			if (allSettings != null && allSettings.isVisible()) {
				allSettings.interact("All Settings");
			}
			return API.returnTick();
		}

		if (isWorldHopWarningOn()) {
			if (closeSettingsButton != null && closeSettingsButton.isVisible()) {
				if (settingsTabVisibleVarbit != SETTINGS_WARNING_VARBIT_VALUE) {
					Widget w = Widgets.get(134,23,7);
					if (w != null && w.isVisible()) {
						w.interact("Select Warnings");
					}
					//click "Warnings" sub-tab
					//client.invokeWidgetAction(57, 8781847, 7, -1, "");
					return API.returnTick();
				}
				Widget w = Widgets.get(134,19,32);
				if (w != null && w.isVisible()) {
					w.interact("Toggle");
				}
				//click "Toggle" on world delay maybe
				//client.invokeWidgetAction(57, 8781843, 32, -1, "");
				return API.returnTick();
			}
			if (!Tabs.isOpen(Tab.OPTIONS)) {
				Tabs.open(Tab.OPTIONS);
				return API.returnTick();
			}
			Widget allSettings = getAllSettingsButton();
			if (allSettings != null && allSettings.isVisible()) {
				allSettings.interact("All Settings");
			}
			return API.returnTick();
		}
		if (closeSettingsButton != null && closeSettingsButton.isVisible()) {
			closeSettingsButton.interact("Close");
		}
		return API.returnTick();
	}
}
