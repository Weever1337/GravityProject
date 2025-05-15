package org.weever.gravitymod.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.common.Mod;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.api.GravityAPI;
import org.weever.gravitymod.api.GravityDirection;

@Mod.EventBusSubscriber(modid = GravityMod.MODID)
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
        if (!entity.level.isClientSide()) {
            GravityAPI.setGravityDirection(entity, GravityDirection.UP);
        }
    }
}
