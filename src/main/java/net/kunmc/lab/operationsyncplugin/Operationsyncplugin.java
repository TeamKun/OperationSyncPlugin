package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class Operationsyncplugin extends JavaPlugin {

    private ProtocolManager protocolManager;

    private boolean active = false;
    private boolean syncView = false;
    private String king = "";
    private List<String> kings = new ArrayList<>();

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

    public List<Player> getKings() {
        return kings.stream().map(id -> Bukkit.getPlayerExact(id)).filter(player -> player != null).collect(Collectors.toList());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setKingsID(List<String> kingsID) {
        this.kings = kingsID;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public boolean getSyncView() {
        return syncView;
    }

    public void setSyncView(boolean syncView) {
        this.syncView = syncView;
    }
}
