package org.weever.gravitymod.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.common.Mod;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.access.IGravityEntity;

@Mod.EventBusSubscriber(modid = GravityMod.MODID)
public class GravityRotateEffect extends Effect {
    private final Direction direction;

    public GravityRotateEffect(EffectType type, int liquidColor, Direction gravityDirection) {
        super(type, liquidColor);
        this.direction = gravityDirection;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    //    @Override
    //    public boolean isInstantenous(){
    //    	return true;
    //	} // like effects for a time

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide()) {
            ((IGravityEntity) entity).gravitymod$setGravityDirection(direction);
        }
    }
}
