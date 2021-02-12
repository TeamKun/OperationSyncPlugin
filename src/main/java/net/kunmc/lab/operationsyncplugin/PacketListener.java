package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class PacketListener extends PacketAdapter {

    private Operationsyncplugin operationsyncplugin;

    public PacketListener(Operationsyncplugin plugin) {
        super(plugin, PacketType.Play.Client.BLOCK_DIG);
        this.operationsyncplugin = plugin;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
            if (!operationsyncplugin.isActive()) {
                return;
            }
            if (operationsyncplugin.getKings().isEmpty()) {
                return;
            }
            Player king = event.getPlayer();
            if (king.getGameMode().equals(GameMode.SPECTATOR)) {
                return;
            }
            if (!operationsyncplugin.getKings().contains(king)) {
                return;
            }
            PacketContainer packet = event.getPacket();
            EnumWrappers.PlayerDigType type = packet.getPlayerDigTypes().read(0);
            switch (type) {
                case START_DESTROY_BLOCK:
                case STOP_DESTROY_BLOCK:
                case ABORT_DESTROY_BLOCK:
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                            return;
                        }
                        if (operationsyncplugin.isKing(player)) {
                            return;
                        }
                        if (!operationsyncplugin.shouldSync(player, king)) {
                            return;
                        }
                        PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.BLOCK_DIG);
                        packetContainer.getPlayerDigTypes().write(0, type);
                        BlockPosition position = new BlockPosition(player.getTargetBlock(4).getLocation().toVector());
                        packetContainer.getBlockPositionModifier().write(0, position);
                        EnumWrappers.Direction direction = EnumWrappers.Direction.valueOf(player.getTargetBlockFace(4).name());
                        packetContainer.getDirections().write(0, direction);
                        try {
                            operationsyncplugin.getProtocolManager().recieveClientPacket(player, packetContainer);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
