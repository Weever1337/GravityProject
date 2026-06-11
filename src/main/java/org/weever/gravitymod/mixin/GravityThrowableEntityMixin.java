package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(ThrowableEntity.class)
public abstract class GravityThrowableEntityMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V",
            at = @At("TAIL")
    )
    private void gravitymod$spawnAtRotatedEyes(EntityType<?> entityType, LivingEntity shooter, World world, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(shooter);
        if (gravityDirection == Direction.DOWN) return;

        Vector3d pos = shooter.getEyePosition(1.0F)
                .subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.1D, 0.0D, gravityDirection));
        ((Entity) (Object) this).setPos(pos.x, pos.y, pos.z);
    }
}
