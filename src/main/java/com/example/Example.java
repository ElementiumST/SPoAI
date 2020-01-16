package com.example;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Example extends JavaPlugin implements Listener {

    private JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin=this;
        getServer().getPluginManager().registerEvents(this, this);


    }

    @EventHandler
    public void onSlaveShot(EntityShootBowEvent e) {
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
    @EventHandler
    public void onMasterAttack(EntityDamageByEntityEvent e) {
        if(e.getDamager().hasMetadata(SummonUnitStatus.MASTER)) {
            MasterMetaDataValue value =
                    (MasterMetaDataValue) e.getDamager().getMetadata(SummonUnitStatus.MASTER).get(0);
            value.grandOrderForSlaves(Order.ATTACK, new Object[] {e.getEntity()});
        }
    }

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
    public void slaveLostTarget(EntityTargetEvent e) {
        if(e.getEntity().hasMetadata(SummonUnitStatus.SLAVE)) {
            if(e.getEntityType().equals(EntityType.SKELETON)){
                SkeletonSlaveMetaDataValue slave =
                        (SkeletonSlaveMetaDataValue) e.getEntity().getMetadata(SummonUnitStatus.SLAVE).get(0);
                ((Skeleton) e.getEntity()).setTarget(slave.getMaster());
            }
        }
    }


    @EventHandler
    public void BoneUsed(PlayerInteractEvent e) {

        if(e.getItem() == null) {
            return;
        }
        // Bukkit.getServer().getConsoleSender().sendMessage("FuckThisShit");
        if(e.getItem().getType().equals(Material.BOW)){
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
            if(!e.getPlayer().hasMetadata(SummonUnitStatus.MASTER))
                e.getPlayer().setMetadata(SummonUnitStatus.MASTER, unit);
            else {((MasterMetaDataValue)e.getPlayer().getMetadata(SummonUnitStatus.MASTER)).addSlave(unit.getSlave());}
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static class MasterMetaDataValue implements MetadataValue{

        public static final String IS_SLAVE = "isSlave";
        private Player master;
        private List<Entity> slaves;

        public MasterMetaDataValue(Player master, List<Entity> slaves) {
            this.master = master;
            this.slaves = slaves;
        }
        public void addSlave(Entity slave) {
            slaves.add(slave);
        }
        public void grandOrderForSlaves(Order order, Object[] args) {
            for(Entity entity : slaves){
                SummonUnitStatus slave = (SummonUnitStatus) entity.getMetadata(SummonUnitStatus.SLAVE).get(0);
                slave.executeOrder(order, args);
            }
        }

        public List<Entity> getSlaves() {
            return slaves;
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public int asInt() {
            return 0;
        }

        @Override
        public float asFloat() {
            return 0;
        }

        @Override
        public double asDouble() {
            return 0;
        }

        @Override
        public long asLong() {
            return 0;
        }

        @Override
        public short asShort() {
            return 0;
        }

        @Override
        public byte asByte() {
            return 0;
        }

        @Override
        public boolean asBoolean() {
            return false;
        }

        @Override
        public String asString() {
            return null;
        }

        @Override
        public Plugin getOwningPlugin() {
            return JavaPlugin.getProvidingPlugin(Example.class);
        }

        @Override
        public void invalidate() {

        }

        public Player getMaster() {
            return master;
        }
    }
    public static class SkeletonSlaveMetaDataValue implements SummonUnitStatus {
        private Player master;
        private Skeleton slave = null;
        private boolean isSummoned = false;

        public SkeletonSlaveMetaDataValue(Player master) {
            this.master = master;
        }

        @Override
        public void call(OnSummonActionCompleteListener onSummonActionCompleteListener) {
            slave = (Skeleton) Objects.requireNonNull(master.getLocation().getWorld())
                    .spawnEntity(master.getLocation(), EntityType.SKELETON);
            slave.setMetadata(SummonUnitStatus.SLAVE, this);

            List<MetadataValue> masterMetaDataValueList =
                     master.getMetadata(SummonUnitStatus.MASTER);
            if(masterMetaDataValueList.size() < 1)
                master.setMetadata(SummonUnitStatus.MASTER, new MasterMetaDataValue(master, new ArrayList<Entity>()));
            ((MasterMetaDataValue)master.getMetadata(SummonUnitStatus.MASTER).get(0)).addSlave(slave);

            for (MetadataValue val: slave.getMetadata(SummonUnitStatus.SLAVE)
                 ) {
                Bukkit.getServer().getConsoleSender().sendMessage(val.asString());
            }
            slave.setTarget(master);

            slave.setCustomName("Skeleton-slave (master :"+master.getDisplayName()+")");
            isSummoned = true;
            onSummonActionCompleteListener.onSuccess("You summoned skeleton");
            Bukkit.getServer().getConsoleSender().sendMessage("FuckThisShit");
        }

        @Override
        public void remove(OnSummonActionCompleteListener onSummonActionCompleteListener) {
            this.invalidate();
            master.sendMessage("skeleton is lost");
        }

        @Override
        public boolean unCall(OnSummonActionCompleteListener onSummonActionCompleteListener) {
            return false;
        }

        @Override
        public void onSlaveDeath() {

        }

        @Override
        public void onMasterDeath() {

        }

        @Override
        public void executeOrder(Order order, Object[] args) {
            switch (order) {
                case ATTACK:
                    slave.setTarget((LivingEntity) args[0]);
                    break;
            }
        }

        @Override
        public Object value() {
            return this;
        }
        @Override
        public int asInt() {
            return 0;
        }
        @Override
        public float asFloat() {
            return 0;
        }
        @Override
        public double asDouble() {
            return 0;
        }
        @Override
        public long asLong() {
            return 0;
        }
        @Override
        public short asShort() {
            return 0;
        }
        @Override
        public byte asByte() {
            return 0;
        }
        @Override
        public boolean asBoolean() {
            return false;
        }
        @Override
        public String asString() {
            if(slave == null)
                return null;
            return slave.toString();
        }
        @Override
        public Plugin getOwningPlugin() {
            return JavaPlugin.getProvidingPlugin(Example.class);
        }
        @Override
        public void invalidate() {
            if(slave != null) slave.remove();
        }

        public Player getMaster() {
            return master;
        }

        public Skeleton getSlave() {
            return slave;
        }
    }
    public interface SummonUnitStatus extends MetadataValue {
        String SLAVE = "isSlave";
        String MASTER = "isMaster";

        void call(OnSummonActionCompleteListener onSummonActionCompleteListener);
        void remove(OnSummonActionCompleteListener onSummonActionCompleteListener);
        boolean unCall(OnSummonActionCompleteListener onSummonActionCompleteListener);
        void onSlaveDeath();
        void onMasterDeath();
        void executeOrder(Order order, Object[] args);

        interface OnSummonActionCompleteListener {
            void onSuccess(String message);
            void onFail(String message);
        }
    }
    enum Order {
        ATTACK
    }

}
