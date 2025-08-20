package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;

@Mixin(ExperienceBottleEntity.class)
public class GravityThrownExperienceBottleMixin {
    @Inject(method = "getGravity", at = @At("HEAD"), cancellable = true)
    private void gravitymod$multiplyGravity(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.07F * (float) GravityAPI.getGravityStrength(((Entity) (Object) this)));
    }
}
