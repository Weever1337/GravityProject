package com.hk47bit.gravitymod.effects;

import com.hk47bit.gravitymod.GravityMod;
import com.hk47bit.gravitymod.api.GravityDirection;
import com.hk47bit.gravitymod.capability.EntityGravityCapProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GravityMod.MOD_ID)
public class RotateUpEffect extends Effect {
    public RotateUpEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

//    @Override
//    public boolean isInstantenous(){
//    	return true;
//	} // like effects for a time

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide()){
            entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(entityGravityCap -> {
                entityGravityCap.setGravityDirection(GravityDirection.UP);
            });
        }
    }
}
