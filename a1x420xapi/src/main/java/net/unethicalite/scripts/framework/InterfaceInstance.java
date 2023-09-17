package net.unethicalite.scripts.framework;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterfaceInstance {
    public static PluginInterface pluginInterface;

    public static void setGlobalPlugin(PluginInterface pluginInterface) {
        InterfaceInstance.pluginInterface = pluginInterface;
        /*Plugins.getPluginManager().getPlugins().forEach((p) -> {
            if (!p.getName().equalsIgnoreCase("420xapi") &&
                    !p.getName().equalsIgnoreCase(pluginInterface.plugin().getName()) &&
                    !p.getName().equalsIgnoreCase("configuration") &&
                    !p.getName().equalsIgnoreCase("devious client") &&
                    !p.getName().equalsIgnoreCase("developer tools") &&
                    !p.getName().equalsIgnoreCase("info panel") &&
                    !p.getName().equalsIgnoreCase("developer tools") &&
                    !p.getName().equalsIgnoreCase("Unethical Dev Tools") &&
                    !p.getName().equalsIgnoreCase("xtea") &&
                    !p.getName().equalsIgnoreCase("low detail") &&
                    !p.getName().equalsIgnoreCase("openosrs")) {                if (Plugins.isEnabled(p)) {
                    log.info("Stopping irrelevant plugin (to our 420x script and required devious plugins): "+p.getName());
                    Plugins.stopPlugin(p);
                }
            }
            if (p.getName().equalsIgnoreCase("low detail") && !Plugins.isEnabled(p)) {
                log.info("Starting low detail plugin just cos");
                Plugins.startPlugin(p);
            }
        });*/
    }
}
