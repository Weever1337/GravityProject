package com.hk47bit.gravitymod.capability;

import com.hk47bit.gravitymod.api.GravityDirection;
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
            instance.setGravityDirection(GravityDirection.valueOf(cnbt.getString("gravityDirection")));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
