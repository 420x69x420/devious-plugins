package net.unethicalite.scripts.tasks.settings;

import net.unethicalite.scripts.framework.InterfaceInstance;
import net.unethicalite.scripts.framework.Branch;

public class SettingsBranch extends Branch {
    @Override
    public boolean generalValidation() {
        return InterfaceInstance.pluginInterface.setSettings();
    }
}
