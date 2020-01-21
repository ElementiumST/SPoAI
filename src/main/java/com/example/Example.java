package com.example;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Example extends JavaPlugin implements Listener {

    private JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin=this;
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                    for(World world :getServer().getWorlds())
                        for(Entity e : world.getEntities()) {
                            if(e.hasMetadata(SummonUnitStatus.SLAVE)) {

                            }
                        }

            }
        }, 10L, 10L);

    }

    /**
     * Обрабатывает попытку слуги выстрела по мастеру
     * @param e Событие взаимодействия слуги с луком
     */
    @EventHandler
    public void onSlaveShotTry(EntityShootBowEvent e) {
        if(e.getEntity().hasMetadata(SummonUnitStatus.SLAVE)) {
            if(e.getEntity().getType().equals(EntityType.SKELETON)){
                Skeleton slave = (Skeleton) e.getEntity();
                SkeletonSlaveMetaDataValue slaveMetadata =
                        (SkeletonSlaveMetaDataValue) slave.getMetadata(SummonUnitStatus.SLAVE).get(0);
                if (Objects.equals(slave.getTarget(), slaveMetadata.getMaster())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Обрабатывает событие атаки мастера по сущности
     * @param e Событие нанесения урона
     */
    @EventHandler
    public void onMasterAttack(EntityDamageByEntityEvent e) {
        if(e.getDamager().hasMetadata(SummonUnitStatus.MASTER)) {
            MasterMetaDataValue value =
                    (MasterMetaDataValue) e.getDamager().getMetadata(SummonUnitStatus.MASTER).get(0);
            value.grandOrderForSlaves(Order.ATTACK, e.getEntity());
        }
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent e) {
        if(e.getPlayer().hasMetadata(SummonUnitStatus.MASTER)) {

        }
    }
    /**
     *
     * @param e
     */
    @EventHandler
    public void SlaveFriendlyFire(EntityDamageByEntityEvent e) {
        if(e.getEntity().hasMetadata(SummonUnitStatus.SLAVE) && e.getDamager().hasMetadata(SummonUnitStatus.SLAVE)){
            SkeletonSlaveMetaDataValue value1 =
                    (SkeletonSlaveMetaDataValue) e.getDamager().getMetadata(SummonUnitStatus.SLAVE).get(0);
            SkeletonSlaveMetaDataValue value2 =
                    (SkeletonSlaveMetaDataValue) e.getEntity().getMetadata(SummonUnitStatus.SLAVE).get(0);
            if(value1.getMaster().getDisplayName().equals(value2.getMaster().getDisplayName())) {

                e.setCancelled(true);
            }
        }
    }

    /**
     * Обрабатывает гибель слуги, вызывает соответствующую функцию в кслассе обьекта слуги
     * @param e Событие гибели слуги
     */
    @EventHandler
    public void onSlaveDeath(EntityDeathEvent e) {
        if(e.getEntity().hasMetadata(SummonUnitStatus.SLAVE)) {
            if(e.getEntityType().equals(EntityType.SKELETON))  {
                SkeletonSlaveMetaDataValue slave =
                        (SkeletonSlaveMetaDataValue) e.getEntity().getMetadata(SummonUnitStatus.SLAVE).get(0);
                slave.remove(new SummonUnitStatus.OnSummonActionCompleteListener() {
                    @Override
                    public void onSuccess(String message) {
                        slave.getMaster().sendMessage(message);
                    }

                    @Override
                    public void onFail(String message) {
                        slave.getMaster().sendMessage(message);
                    }
                });
            }
        }
    }

    @EventHandler
    public void creeperExplosion(ExplosionPrimeEvent e){

    }
    /**
     * Событие, обрабатывающее
     * @param e
     */
    @EventHandler
    public void slaveGetTarget(EntityTargetLivingEntityEvent e) {
        if(e.getEntity().hasMetadata(SummonUnitStatus.SLAVE)) {
            if(e.getTarget() != null)
                if(e.getTarget().hasMetadata(SummonUnitStatus.SLAVE))
                    e.setCancelled(true);

        }
    }


    @EventHandler
    public void onPlayerSay(PlayerChatEvent e) {
        if(e.getMessage().equals("ss")) {
            SkeletonSlaveMetaDataValue unit = new SkeletonSlaveMetaDataValue(e.getPlayer());
            unit.call(new SummonUnitStatus.OnSummonActionCompleteListener() {
                @Override
                public void onSuccess(String message) {
                    e.getPlayer().sendMessage(message);
                }

                @Override
                public void onFail(String message) {
                    e.getPlayer().sendMessage(message);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
