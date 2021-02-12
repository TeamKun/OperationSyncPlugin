package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class Operationsyncplugin extends JavaPlugin {

    private ProtocolManager protocolManager;

    private boolean active = false;
    private boolean syncView = false;
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

    public boolean shouldSync(Player player, Player king) {
        if (isKing(player)) {
            return false;
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            return false;
        }
        Team team = king.getScoreboard().getEntryTeam(king.getName());
        if (team == null) {
            return true;
        }
        Team team1 = player.getScoreboard().getEntryTeam(player.getName());
        if (!team.equals(team1)) {
            return false;
        }
        return true;
    }

    public boolean isKing(Player player) {
        return getKings().contains(player);
    }
}
