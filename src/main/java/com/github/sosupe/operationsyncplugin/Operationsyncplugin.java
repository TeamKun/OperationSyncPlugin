package com.github.sosupe.operationsyncplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.ProtocolLibrary;

public final class Operationsyncplugin extends JavaPlugin {
    public static Operationsyncplugin INSTANCE;
    public int mode;
    public Map<String, Vector> offsets = new HashMap<>();

    public String king=null;

    public ProtocolManager manager;

    @Override
    public void onEnable() {
        INSTANCE=this;
        // イベント登録
        getCommand("sync").setExecutor(new CommandListener());
        getServer().getPluginManager().registerEvents(new EventListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
