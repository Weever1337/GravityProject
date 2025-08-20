package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.weever.gravitymod.util.GravityAPI;

@Mixin(AbstractHorseEntity.class)
public class GravityAbstractHorseMixin {
    @ModifyVariable(method = "calculateFallDamage(FF)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gravitymod$diminishFallDamage(float value) {
        return value * (float) Math.sqrt(GravityAPI.getGravityStrength(((Entity) (Object) this)));
    }
}
