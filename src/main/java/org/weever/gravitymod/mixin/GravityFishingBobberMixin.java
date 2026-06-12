package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(FishingBobberEntity.class)
public abstract class GravityFishingBobberMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V",
            at = @At("TAIL")
    )
    private void gravitymod$castWithGravity(PlayerEntity player, World world, int luck, int lureSpeed, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) return;

        Entity self = (Entity) (Object) this;

        float yawCos = MathHelper.cos(-player.yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float yawSin = MathHelper.sin(-player.yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        Vector3d spawnPos = player.getEyePosition(1.0F)
                .add(RotationUtil.vecPlayerToWorld(-yawSin * 0.3D, 0.0D, -yawCos * 0.3D, gravityDirection));

        Vector3d worldVelocity = RotationUtil.vecPlayerToWorld(self.getDeltaMovement(), gravityDirection);

        float newYRot = (float) (MathHelper.atan2(worldVelocity.x, worldVelocity.z) * (double) (180F / (float) Math.PI));
        float newXRot = (float) (MathHelper.atan2(worldVelocity.y,
                Math.sqrt(worldVelocity.x * worldVelocity.x + worldVelocity.z * worldVelocity.z)) * (double) (180F / (float) Math.PI));
        self.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, newYRot, newXRot);
        self.setDeltaMovement(worldVelocity);
    }
}