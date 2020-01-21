package com.example;



import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MasterMetaDataValue implements MetadataValue {

    public static final String IS_SLAVE = "isSlave";
    private Player master;
    private List<Entity> slaves;
    private LivingEntity target;

    public MasterMetaDataValue(Player master, List<Entity> slaves) {
        this.master = master;
        this.slaves = slaves;

    }
    public void addSlave(Entity slave) {
        slaves.add(slave);
    }
    public void grandOrderForSlaves(Order order, Object... args) {
        if(order.equals(Order.ATTACK))
            target = (LivingEntity) args[0];
        for(Entity entity : slaves){
            SummonUnitStatus slave = (SummonUnitStatus) entity.getMetadata(SummonUnitStatus.SLAVE).get(0);
            slave.executeOrder(order, args);
        }
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
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
