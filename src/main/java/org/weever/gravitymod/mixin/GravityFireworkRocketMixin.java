package org.weever.gravitymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(FireworkRocketEntity.class)
public abstract class GravityFireworkRocketMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getLookAngle()Lnet/minecraft/util/math/vector/Vector3d;"
            )
    )
    private Vector3d gravitymod$boostAlongLocalLook(LivingEntity attachedEntity) {
        Vector3d look = attachedEntity.getLookAngle();
        Direction gravityDirection = GravityAPI.getGravityDirection(attachedEntity);
        if (gravityDirection == Direction.DOWN) {
            return look;
        }
        return RotationUtil.vecWorldToPlayer(look, gravityDirection);
    }
}
