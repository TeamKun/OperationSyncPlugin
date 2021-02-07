package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;

public class EventListener implements Listener {

    private final Operationsyncplugin operationsyncplugin;

    public EventListener(Operationsyncplugin plugin) {
        this.operationsyncplugin = plugin;
    }

    private boolean shouldHandle(Player player) {
        if (!operationsyncplugin.isActive()) {
            return false;
        }
        if (operationsyncplugin.getKings().isEmpty()){
            return false;
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            return false;
        }
        return true;
    }

    private boolean shouldSync(Player player) {
        if (isKing(player)) {
            return false;
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            return false;
        }
        return true;
    }

    private boolean isKing(Player player) {
        return operationsyncplugin.getKings().contains(player);
    }

    @EventHandler
    public void onKingSwapHandItemchange(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            ItemStack item2 = otherPlayer.getInventory().getItemInOffHand();
            ItemStack item1 = otherPlayer.getInventory().getItemInMainHand();
            otherPlayer.getInventory().setItemInOffHand(item1);
            otherPlayer.getInventory().setItemInMainHand(item2);
        });
    }

    @EventHandler
    public void onKingSlotChange(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            otherPlayer.getInventory().setHeldItemSlot(event.getNewSlot());
        });
    }

    @EventHandler
    public void onKingDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        int amount = event.getItemDrop().getItemStack().getAmount();
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            ItemStack itemStack = otherPlayer.getItemInHand();
            if (itemStack.getType().equals(Material.AIR)) {
                return;
            }
            ItemStack clone = itemStack.clone();
            clone.setAmount(Math.min(itemStack.getAmount(), amount));
            Item item = otherPlayer.getWorld().dropItem(otherPlayer.getLocation(), clone);
            item.setPickupDelay(30);
            double yaw = otherPlayer.getLocation().getYaw();
            double pitch = otherPlayer.getLocation().getPitch();
            yaw = Math.toRadians(yaw);
            pitch = Math.toRadians(pitch);
            Vector vector = new Vector(Math.cos(pitch) * Math.sin(-yaw),Math.sin(- pitch),Math.cos(pitch) * Math.cos(yaw));
            vector.multiply(0.3);
            item.setVelocity(vector);
            itemStack.subtract(amount);
        });
    }

    @EventHandler
    public void onKingMoveParallel(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            Vector locDif = event.getTo().toVector().subtract(event.getFrom().toVector());
            locDif.rotateAroundY(Math.toRadians((event.getFrom().getYaw() - otherPlayer.getLocation().getYaw())))
                    .setY(0)
                    .setX(locDif.getX() * otherPlayer.getVelocity().getX() < 0 || Math.abs(locDif.getX()) > Math.abs(otherPlayer.getVelocity().getX()) ? locDif.getX() : 0)
                    .setZ(locDif.getZ() * otherPlayer.getVelocity().getZ() < 0 || Math.abs(locDif.getZ()) > Math.abs(otherPlayer.getVelocity().getZ()) ? locDif.getZ() : 0)
                    .multiply(0.7);
            otherPlayer.setVelocity(otherPlayer.getVelocity().add(locDif));

            if (!operationsyncplugin.getSyncView()) {
                return;
            }
            float yawDif = event.getTo().getYaw() - event.getFrom().getYaw();
            float pitchDif = event.getTo().getPitch() - event.getFrom().getPitch();
            Location location = otherPlayer.getLocation();
            location.setYaw(location.getYaw() + yawDif);
            location.setPitch(location.getPitch() + pitchDif);
            if (yawDif == 0 || pitchDif == 0) {
                return;
            }
            otherPlayer.teleport(location);
        });
    }

    @EventHandler
    public void onKingJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        if (player.isJumping()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getY() >= to.getY()) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            if (!otherPlayer.isOnGround()) {
                return;
            }
            if (otherPlayer.isJumping()) {
                return;
            }
            if (otherPlayer.getVelocity().getY() > 0) {
                return;
            }
            Vector pVec = otherPlayer.getVelocity();
            otherPlayer.setVelocity(pVec.add(new Vector(0, 0.5, 0)));
        });
    }

    @EventHandler
    public void onKingAttack(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            return;
        }
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        if (!event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            otherPlayer.swingMainHand();
            Entity entity = otherPlayer.getTargetEntity(4, false);
            if (entity == null) {
                return;
            }
            ((CraftPlayer) otherPlayer).getHandle().attack(((CraftEntity) entity).getHandle());
        });
    }

    @EventHandler
    public void onKingPlace(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))) {
            return;
        }
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        if (!event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            if (!otherPlayer.getItemInHand().getType().isBlock()) {
                return;
            }
            Block block = otherPlayer.getTargetBlock(4);
            if (block.getType().equals(Material.AIR)) {
                return;
            }
            BlockFace blockFace = otherPlayer.getTargetBlockFace(4);
            block.getLocation().clone().add(blockFace.getDirection()).getBlock().setType(otherPlayer.getItemInHand().getType());
            otherPlayer.getInventory().getItemInHand().subtract();
            otherPlayer.swingMainHand();
         });
    }

    @EventHandler
    public void onKingSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            if (event.isSneaking()) {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.START_SNEAKING);
                packetContainer.getIntegers().write(0, otherPlayer.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(otherPlayer, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.STOP_SNEAKING);
                packetContainer.getIntegers().write(0, otherPlayer.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(otherPlayer, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @EventHandler
    public void onKingSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (!shouldHandle(player)) {
            return;
        }
        if (!isKing(player)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (!shouldSync(otherPlayer)) {
                return;
            }
            if (event.isSprinting()) {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.START_SPRINTING);
                packetContainer.getIntegers().write(0, otherPlayer.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(otherPlayer, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.STOP_SPRINTING);
                packetContainer.getIntegers().write(0, otherPlayer.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(otherPlayer, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
