package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(AbstractHorseEntity.class)
public class GravityAbstractHorseMixin {
    @ModifyVariable(method = "calculateFallDamage(FF)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gravitymod$diminishFallDamage(float value) {
        return value * (float) Math.sqrt(GravityAPI.getGravityStrength(((Entity) (Object) this)));
    }

    @Inject(
            method = "getDismountLocationForPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$getDismountLocation(LivingEntity passenger, CallbackInfoReturnable<Vector3d> cir) {
        Entity self = (Entity) (Object) this;
        Direction gravityDirection = GravityAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB localBox = RotationUtil.boxWorldToPlayer(self.getBoundingBox(), gravityDirection);
        Vector3d localPos = RotationUtil.vecWorldToPlayer(self.position(), gravityDirection);
        cir.setReturnValue(RotationUtil.vecPlayerToWorld(localPos.x, localBox.maxY, localPos.z, gravityDirection));
    }
}
