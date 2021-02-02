package net.kunmc.lab.operationsyncplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
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
import java.util.Objects;

public class EventListener implements Listener {

    private final Operationsyncplugin operationsyncplugin;

    public EventListener(Operationsyncplugin plugin) {
        this.operationsyncplugin = plugin;
    }

    @EventHandler
    public void SwapHandItemchange(PlayerSwapHandItemsEvent event){
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null){
            return;
        }

        Player player = event.getPlayer();
        if(player.equals(operationsyncplugin.getKing())){
            for(Player p:Bukkit.getOnlinePlayers()){
                if(Objects.equals(p,player)){
                    continue;
                }
                ItemStack item2=p.getInventory().getItemInOffHand();
                ItemStack item1=p.getInventory().getItemInMainHand();
                p.getInventory().setItemInOffHand(item1);
                p.getInventory().setItemInMainHand(item2);
            }
        }
        else{
            //たぶんいらない
        }
    }

    @EventHandler
    public void onKingSlotChange(PlayerItemHeldEvent event){
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null){
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.getInventory().setHeldItemSlot(event.getNewSlot()));
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null){
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        int amount = event.getItemDrop().getItemStack().getAmount();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.getType().equals(Material.AIR)) {
                return;
            }
            ItemStack clone = itemStack.clone();
            clone.setAmount(Math.min(itemStack.getAmount(), amount));
            Item item = player.getWorld().dropItem(player.getLocation(), clone);
            item.setPickupDelay(30);
            double yaw = player.getLocation().getYaw();
            double pitch = player.getLocation().getPitch();
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
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null) {
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            Vector locDif = event.getTo().toVector().subtract(event.getFrom().toVector());
            locDif.rotateAroundY(Math.toRadians((event.getFrom().getYaw() - player.getLocation().getYaw())))
                    .setY(0)
                    .setX(locDif.getX() * player.getVelocity().getX() < 0 || Math.abs(locDif.getX()) > Math.abs(player.getVelocity().getX()) ? locDif.getX() : 0)
                    .setZ(locDif.getZ() * player.getVelocity().getZ() < 0 || Math.abs(locDif.getZ()) > Math.abs(player.getVelocity().getZ()) ? locDif.getZ() : 0)
                    .multiply(0.7);
            player.setVelocity(player.getVelocity().add(locDif));

            float yawDif = event.getTo().getYaw() - event.getFrom().getYaw();
            float pitchDif = event.getTo().getPitch() - event.getFrom().getPitch();
            Location location = player.getLocation();
            location.setYaw(location.getYaw() + yawDif);
            location.setPitch(location.getPitch() + pitchDif);
            if (yawDif == 0 || pitchDif == 0) {
                return;
            }
            player.teleport(location);
        });
    }

    @EventHandler
    public void onKingJump(PlayerMoveEvent event) {
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null) {
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        if (king.isJumping()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getY() >= to.getY()) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            if (!player.isOnGround()) {
                return;
            }
            if (player.isJumping()) {
                return;
            }
            if (player.getVelocity().getY() > 0) {
                return;
            }
            Vector pVec = player.getVelocity();
            player.setVelocity(pVec.add(new Vector(0, 0.5, 0)));
        });
    }

    @EventHandler
    public void onKingAttack(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            return;
        }
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null) {
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        if (!event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            player.swingMainHand();
            Entity entity = player.getTargetEntity(4, false);
            if (entity == null) {
                return;
            }
            ((CraftPlayer) player).getHandle().attack(((CraftEntity) entity).getHandle());
        });
    }

    @EventHandler
    public void onKingPlace(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))) {
            return;
        }
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null) {
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        if (!event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            if (!player.getItemInHand().getType().isBlock()) {
                return;
            }
            Block block = player.getTargetBlock(4);
            if (block.getType().equals(Material.AIR)) {
                return;
            }
            BlockFace blockFace = player.getTargetBlockFace(4);
            block.getLocation().clone().add(blockFace.getDirection()).getBlock().setType(player.getItemInHand().getType());
            player.getInventory().getItemInHand().subtract();
            player.swingMainHand();
         });
    }

    @EventHandler
    public void onKingSneak(PlayerToggleSneakEvent event) {
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null) {
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            if (event.isSneaking()) {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.START_SNEAKING);
                packetContainer.getIntegers().write(0, player.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(player, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.STOP_SNEAKING);
                packetContainer.getIntegers().write(0, player.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(player, packetContainer);
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
        if (!operationsyncplugin.isActive()) {
            return;
        }
        if (operationsyncplugin.getKing() == null) {
            return;
        }
        Player king = event.getPlayer();
        if (!king.equals(operationsyncplugin.getKing())) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.equals(king)) {
                return;
            }
            if (event.isSprinting()) {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.START_SPRINTING);
                packetContainer.getIntegers().write(0, player.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(player, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                PacketContainer packetContainer = operationsyncplugin.getProtocolManager().createPacket(PacketType.Play.Client.ENTITY_ACTION);
                packetContainer.getPlayerActions().write(0, EnumWrappers.PlayerAction.STOP_SPRINTING);
                packetContainer.getIntegers().write(0, player.getEntityId());
                try {
                    operationsyncplugin.getProtocolManager().recieveClientPacket(player, packetContainer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
