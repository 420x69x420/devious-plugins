package net.unethicalite.scripts.framework;

import net.runelite.client.plugins.Plugin;

public interface PluginInterface {
    Plugin plugin();
    boolean shouldBondUp();
    boolean setSettings();
    boolean isBankCached();
    void cachedBank();
    boolean shouldHandleBugs();
    void handledBug();
    boolean loginF2P();
    String botGroup();
}