package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.Mth;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.access.IGravityLivingEntity;
import org.weever.gravitymod.util.GravityAPI;

@Mixin(MobEntity.class)
public abstract class GravityMobMixin extends LivingEntity {

    @Shadow protected abstract float rotlerp(float f, float g, float h);

    @Inject(
            method = "doHurtTarget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"
            )
    )
    private void gravitymod$doHurtTarget(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN)
            return;

        if (target instanceof LivingEntity) {
            ((IGravityLivingEntity)target).gravitymod$augmentKB(this);
        }
    }

    @Inject(
            method = "lookAt",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void gravitymod$lookat(Entity $$0, float $$1, float $$2, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection($$0);
        Direction gravityDirection2 = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN && gravityDirection2 == Direction.DOWN)
            return;
        ci.cancel();

        Vector3d targetPoint;
        if ($$0 instanceof LivingEntity) {
            targetPoint = $$0.getEyePosition(1.0F);
        } else {
            targetPoint = $$0.getBoundingBox().getCenter();
        }

        Vector3d local = RotationUtil.vecWorldToPlayer(
                targetPoint.subtract(this.getEyePosition(1.0F)), gravityDirection2);
        double horizontal = Math.sqrt(local.x * local.x + local.z * local.z);
        float yaw = (float) (Mth.atan2(local.z, local.x) * 180.0F / (float) Math.PI) - 90.0F;
        float pitch = (float) (-(Mth.atan2(local.y, horizontal) * 180.0F / (float) Math.PI));
        this.xRot = (this.rotlerp(this.xRot, pitch, $$2));
        this.yRot = (this.rotlerp(this.yRot, yaw, $$1));
        this.yHeadRot = this.rotlerp(this.yHeadRot, yaw, $$1);
    }

    protected GravityMobMixin(EntityType<? extends LivingEntity> $$0, World $$1) {
        super($$0, $$1);
    }
}
