package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(ProjectileEntity.class)
public abstract class GravityProjectileMixin {
    @ModifyVariable(
            method = "shootFromRotation(Lnet/minecraft/entity/Entity;FFFFF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private float modify_setProperties_pitch(float value, Entity user, float yaw, float roll, float speed, float divergence) {
        Direction gravityDirection = GravityAPI.getGravityDirection(user);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }

        return RotationUtil.rotPlayerToWorld(user.yRot, user.xRot, gravityDirection).y;
    }

    @ModifyVariable(
            method = "shootFromRotation(Lnet/minecraft/entity/Entity;FFFFF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private float modify_setProperties_yaw(float value, Entity user, float pitch, float roll, float speed, float divergence) {
        Direction gravityDirection = GravityAPI.getGravityDirection(user);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }

        return RotationUtil.rotPlayerToWorld(user.yRot, user.xRot, gravityDirection).x;
    }
}
