package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


public final class Operationsyncplugin extends JavaPlugin {

    private ProtocolManager protocolManager;

    private SyncMode syncMode = SyncMode.ADD;
    private boolean active = false;
    private String king = "";

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketListener(this));

        new CommandListener(this).register();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (!isActive()) {
                        return;
                    }
                    if (getKing() == null) {
                        return;
                    }
                    if (player.equals(getKing())) {
                        return;
                    }
                    if (!getSyncMode().equals(SyncMode.ALL)) {
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 255, false, false));
                });
            }
        }.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Player getKing() {
        return Bukkit.getPlayerExact(king);
    }

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public boolean isActive() {
        return active;
    }

    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
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
