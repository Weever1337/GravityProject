package org.weever.gravitymod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class EntityGravityStorage implements Capability.IStorage<EntityGravityCap> {

    @Override
    public INBT writeNBT(Capability<EntityGravityCap> capability, EntityGravityCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        cnbt.putString("gravityDirection", instance.getGravityDirection().toString());
        return cnbt;
    }

    @Override
    public void readNBT(Capability<EntityGravityCap> capability, EntityGravityCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        try {
            instance.setGravityDirection(Direction.valueOf(cnbt.getString("gravityDirection").toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
