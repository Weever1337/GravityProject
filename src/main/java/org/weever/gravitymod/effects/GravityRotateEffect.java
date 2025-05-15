package org.weever.gravitymod.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.common.Mod;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.api.GravityAPI;
import org.weever.gravitymod.api.GravityDirection;

@Mod.EventBusSubscriber(modid = GravityMod.MODID)
public class GravityRotateEffect extends Effect {
    private final GravityDirection direction;

    public GravityRotateEffect(EffectType type, int liquidColor, GravityDirection gravityDirection) {
        super(type, liquidColor);
        this.direction = gravityDirection;
    }

    //    @Override
    //    public boolean isInstantenous(){
    //    	return true;
    //	} // like effects for a time

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide()) {
            GravityAPI.setGravityDirection(entity, direction);
        }
    }
}
