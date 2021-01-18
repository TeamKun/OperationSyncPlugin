package com.github.sosupe.operationsyncplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.ProtocolLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.sosupe.operationsyncplugin.Operationsyncplugin;

public class CommandListener implements CommandExecutor,TabCompleter{
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(args.length<1)
            return false;
        if("king".equals(args[0])){
            Operationsyncplugin.INSTANCE.offsets.clear();
            if(args.length>=2){
                Operationsyncplugin.INSTANCE.king=args[1];
                sender.sendMessage("中心プレイヤーが"+Operationsyncplugin.INSTANCE.king+"にセットされました");
            }
            else{
                Player king=Bukkit.getPlayer(Operationsyncplugin.INSTANCE.king);
                Operationsyncplugin.INSTANCE.king=null;
                sender.sendMessage("中心プレイヤーがリセットされました");
            }
            return true;
        }

        //allはmode=2
        if("all".equals(args[0])){
            if(Operationsyncplugin.INSTANCE.mode!=2){
                if(Operationsyncplugin.INSTANCE.king==null){
                    Operationsyncplugin.INSTANCE.king=sender.getName();
                    sender.sendMessage("中心プレイヤーが"+Operationsyncplugin.INSTANCE.king+"にセットされました");
                }

                Operationsyncplugin.INSTANCE.mode=2;
                Operationsyncplugin.INSTANCE.offsets.clear();
                Bukkit.broadcastMessage("操作シンクロが完全同期されました");
            }
            else{
                Bukkit.broadcastMessage("操作シンクロは完全同期されています");
            }
            return true;
        }

        //offはmode=1
        if("off".equals(args[0])){
            Player p=(Player) sender;
            Location l=p.getLocation();
            l.setYaw(l.getYaw()+10);
            //l.setPitch(90);
            p.teleport(l);

            if(Operationsyncplugin.INSTANCE.mode!=1){
                Operationsyncplugin.INSTANCE.offsets.clear();
                Operationsyncplugin.INSTANCE.mode=1;
                Bukkit.broadcastMessage("操作シンクロが無効化されました");
            }
            else{
                Bukkit.broadcastMessage("操作シンクロは無効化されています");
            }
            return true;
        }

        //addはmode=3
        if("add".equals(args[0])){
            if(Operationsyncplugin.INSTANCE.mode!=3){
                if(Operationsyncplugin.INSTANCE.king==null){
                    Operationsyncplugin.INSTANCE.king=sender.getName();
                    sender.sendMessage("中心プレイヤーが"+Operationsyncplugin.INSTANCE.king+"にセットされました");
                }
                Operationsyncplugin.INSTANCE.mode=3;
                Operationsyncplugin.INSTANCE.offsets.clear();
                Bukkit.broadcastMessage("操作シンクロが加算化されました");
            }
            else{
                Bukkit.broadcastMessage("操作シンクロは加算化されています");
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command command,String label,String[] args){
        final List<String> completions = new ArrayList<>();
        if(args.length==1){
            completions.add("king");
            completions.add("off");
            completions.add("all");
            completions.add("add");
        }
        if(args.length==2&&"king".equals(args[0])){
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
        }
        return completions;
    }

}
