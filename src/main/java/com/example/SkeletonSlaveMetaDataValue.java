package com.example;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkeletonSlaveMetaDataValue implements SummonUnitStatus {
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
        else
        ((MasterMetaDataValue)master.getMetadata(SummonUnitStatus.MASTER).get(0)).addSlave(slave);


        slave.setTarget(master);
        slave.setCustomName("Skeleton-slave (master: "+master.getDisplayName()+")");
        isSummoned = true;
        onSummonActionCompleteListener.onSuccess("You summoned skeleton");
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
    public void executeOrder(Order order, Object... args) {
        switch (order) {
            case ATTACK:
                slave.setTarget((LivingEntity) args[0]);
                break;

            case FOLLOW:
                slave.setTarget(master);
                Vector pos = slave.getLocation().toVector();
                Vector target = master.getLocation().toVector();
                Vector velocity = target.subtract(pos);
                slave.setVelocity(velocity.normalize().multiply(0.07));
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