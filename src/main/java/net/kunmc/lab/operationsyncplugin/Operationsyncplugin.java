package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public final class Operationsyncplugin extends JavaPlugin {

    private ProtocolManager protocolManager;

    private boolean active = false;
    private String king = "";

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketListener(this));

        new CommandListener(this).register();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Player getKing() {
        return Bukkit.getPlayerExact(king);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setKingID(String king) {
        this.king = king;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
