package com.github.sosupe.operationsyncplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;


public final class Operationsyncplugin extends JavaPlugin {
    public static Operationsyncplugin INSTANCE;
    public int mode;
    public Map<String, Vector> offsets = new HashMap<>();

    public String king=null;


    @Override
    public void onEnable() {
        INSTANCE=this;
        // イベント登録
        getCommand("sync").setExecutor(new CommandListener());
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Operationsyncplugin.INSTANCE.mode=1;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
