package com.github.sosupe.operationsyncplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class EventListener implements Listener {
    public static final double EPSILON=0.1D;
    public static final long TIME_EPSILON_MS=50;
    public Map<String, Location> testlocs=new HashMap<>();
    public Map<String, Float> playeryaw=new HashMap<>();
    public Map<String, Float> playerPitch=new HashMap<>();
    public List<UUID> prevPlayersOnGround=new ArrayList<UUID>();
    private long lastTime2;
    private long lastTime3;

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event){
        Player player=event.getPlayer();
        if(event.isSneaking()){
            player.sendMessage("スニーク");
        }

    }


    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(Operationsyncplugin.INSTANCE.mode==1){
            return;
        }
        if(Operationsyncplugin.INSTANCE.king==null){
            return;
        }
        Player player=event.getPlayer();
        String name=player.getName();
        if(Operationsyncplugin.INSTANCE.mode==2){
            //kingが動いたとき
            if(Objects.equals(name,Operationsyncplugin.INSTANCE.king)){
                //プレイヤー移動
                for(Player p:Bukkit.getOnlinePlayers()){
                    if(Objects.equals(p,player)){
                        continue;
                    }
                    if(playerjump(player)==true){
                        p.setVelocity(player.getVelocity().setY(3.0));
                    }
                    long time=System.currentTimeMillis();
                    if(time-lastTime2>TIME_EPSILON_MS){
                        lastTime2=time;
                        Location to=move2(p,player,p.getLocation(),p.getLocation(),EPSILON);
                        if(to!=null){
                            to=direction2(p,player,to);
                            if(to!=null){
                                p.teleport(to);
                            }
                        }
                    }
                }
            }
            //king以外が動いたとき
            else{
                if(playerjump(player)==true){
                    player.setVelocity(player.getVelocity().setY(-5));
                }
                Player king=Bukkit.getPlayer(Operationsyncplugin.INSTANCE.king);
                if(king!=null){
                    Location to=move2(player,king,event.getFrom(),event.getTo(),
                            event.getFrom().getDirection().equals(event.getTo().getDirection())||player.getVelocity().length() > 0 ? 1 : EPSILON);
                    if(to!=null){
                        to.setYaw(player.getLocation().getYaw());
                        to.setPitch(player.getLocation().getPitch());
                        event.setTo(to);
                    }
                }
            }
        }
        if(Operationsyncplugin.INSTANCE.mode==3){
            if(Objects.equals(name,Operationsyncplugin.INSTANCE.king)){

                for(Player p:Bukkit.getOnlinePlayers()){
                    if(Objects.equals(p,player)){
                        continue;
                    }
                    if(playerjump(player)==true){
                        p.setVelocity(player.getVelocity().setY(3.0));
                    }
                    long time=System.currentTimeMillis();
                    if(time-lastTime3>TIME_EPSILON_MS){
                        lastTime3=time;
                        Location to=move3(player,p,EPSILON);
                        if(to!=null){
                            to=direction3(p,player,to);
                            if(to!=null){
                                p.teleport(to);
                            }
                        }
                    }
                }
            }
            else{
                //たぶんここのコードは必要ない....？
            }
        }
    }

    private Location direction2(Player player,Player king,Location to){
        if(playeryaw.containsKey(king.getName())&&playerPitch.containsKey(king.getName())){
            Float yawsa=Math.abs(king.getLocation().getYaw()-playeryaw.get(king.getName()));
            Float pitchsa=Math.abs(king.getLocation().getPitch()-playerPitch.get(king.getName()));
            if(yawsa>0.5){
                to.setYaw(king.getLocation().getYaw());
            }
            if(pitchsa>0.5){
                to.setPitch(king.getLocation().getPitch());
            }
        }
        playeryaw.put(king.getName(),king.getLocation().getYaw());
        playerPitch.put(king.getName(),king.getLocation().getPitch());
        return to;
    }

    private Location direction3(Player player,Player king,Location to){
        if(playeryaw.containsKey(king.getName())&&playerPitch.containsKey(king.getName())){
            Float kingyawsa=king.getLocation().getYaw()-playeryaw.get(king.getName());
            Float kingpitchsa=king.getLocation().getPitch()-playerPitch.get(king.getName());
            Float playawsa=player.getLocation().getYaw()-playeryaw.get(player.getName());
            Float plapitchsa=player.getLocation().getPitch()-playerPitch.get(player.getName());
            Float yawmove=kingyawsa+playawsa;
            Float pitchmove=kingpitchsa+plapitchsa;
            if(Math.abs(yawmove)>0.5){
                if(player.getLocation().getYaw()+yawmove>360){
                    to.setYaw(player.getLocation().getYaw()+yawmove-360);
                }
                else if(player.getLocation().getYaw()+yawmove<0){
                    to.setYaw(360+player.getLocation().getYaw()+yawmove);
                }
                else{
                    to.setYaw(player.getLocation().getYaw()+yawmove);
                }
            }
            if(Math.abs(pitchmove)>0.5){
                if(player.getLocation().getPitch()+pitchmove>90){
                    to.setPitch(90);
                }
                else if(player.getLocation().getPitch()+pitchmove<-90){
                    to.setPitch(-90);
                }
                else{
                    to.setPitch(player.getLocation().getPitch()+pitchmove);
                }
            }
        }
        playeryaw.put(king.getName(),king.getLocation().getYaw());
        playerPitch.put(king.getName(),king.getLocation().getPitch());
        return to;
    }

    private boolean playerjump(Player player){
        boolean jump=false;
        if(player.getVelocity().getY() > 0){
            double jumpVelocity=(double) 0.42F;
            if(player.hasPotionEffect(PotionEffectType.JUMP)){
                jumpVelocity += (double) ((float) (player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.1F);
            }
            if(player.getLocation().getBlock().getType()!= Material.LADDER && prevPlayersOnGround.contains(player.getUniqueId())){
                if (!player.isOnGround() && Double.compare(player.getVelocity().getY(), jumpVelocity) == 0) {
                    jump=true;
                    player.setVelocity(player.getVelocity().setY(3.0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,Integer.MAX_VALUE,200));
                }
            }
        }
        if (player.isOnGround()) {
            if(!(prevPlayersOnGround.contains(player.getUniqueId()))){
                prevPlayersOnGround.add(player.getUniqueId());
            }
        } else {
            if(prevPlayersOnGround.contains(player.getUniqueId())){
                prevPlayersOnGround.remove(player.getUniqueId());
            }
        }
        return jump;
    }

    private Location move2(Player player,Player king,Location fromloc,Location loc,double epsilon){
        Vector offset=Operationsyncplugin.INSTANCE.offsets.computeIfAbsent(player.getName(),e -> king.getLocation().subtract(loc).toVector());
        Location kingloc=king.getLocation().subtract(offset);
        if(Math.abs(loc.getX()-kingloc.getX()) > epsilon || Math.abs(loc.getZ()-kingloc.getZ()) > epsilon){
            loc.setX(kingloc.getX());
            loc.setZ(kingloc.getX());
            return loc;
        }
        return null;
    }

    private Location move3(Player king,Player player,double epsilon){
        if(testlocs.containsKey(king.getName())&&testlocs.containsKey(player.getName())){
            Location kingsa=king.getLocation().subtract(testlocs.get(king.getName()));
            Location playersa=player.getLocation().subtract(testlocs.get(player.getName()));
            Location wa=kingsa.add(playersa);
            if(Math.abs(wa.getX())>epsilon || Math.abs(wa.getZ())>epsilon){
                Location to=player.getLocation().add(wa);
                to.setY(player.getLocation().getY());
                testlocs.put(king.getName(),king.getLocation());
                testlocs.put(player.getName(),player.getLocation());
                return to;
            }
        }
        testlocs.put(king.getName(),king.getLocation());
        testlocs.put(player.getName(),player.getLocation());
        return null;
    }
}
