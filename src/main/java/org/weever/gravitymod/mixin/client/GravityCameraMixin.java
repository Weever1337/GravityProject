package org.weever.gravitymod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationAnimation;

@Mixin(value = ActiveRenderInfo.class, priority = 1001)
public abstract class GravityCameraMixin {
    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    private Entity entity;

    @Shadow
    @Final
    private Quaternion rotation;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @Shadow private boolean initialized;

    @Shadow private boolean detached;

    @Shadow protected abstract void setRotation(float f, float g);

    @Shadow private float yRot;

    @Shadow private float xRot;

    @Shadow protected abstract double getMaxZoom(double d);

    @Shadow protected abstract void move(double d, double e, double f);

    @Shadow
    private IBlockReader level;

    @Inject(
            method = "setup",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$setup(
            IBlockReader $$0, Entity focusedEntity, boolean $$2, boolean $$3, float tickDelta, CallbackInfo ci
    ) {
        if (focusedEntity != null) {
            Direction gravityDirection = GravityAPI.getGravityDirection(focusedEntity);
            RotationAnimation animation = GravityAPI.getRotationAnimation(focusedEntity);
            if (animation == null) {
                return;
            }

            float partialTick = Minecraft.getInstance().getFrameTime();
            long timeMs = focusedEntity.level.getGameTime() * 50 + (long) (partialTick * 50);
            animation.update(timeMs);
            if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) {
                return;
            }


            ci.cancel();
            this.initialized = true;
            this.level = $$0;
            this.entity = focusedEntity;
            this.detached = $$2;

            this.setRotation(focusedEntity.getViewYRot(tickDelta), focusedEntity.getViewXRot(tickDelta));


            Quaternion gravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);

            double entityX = MathHelper.lerp((double) tickDelta, focusedEntity.xo, focusedEntity.getX());
            double entityY = MathHelper.lerp((double) tickDelta, focusedEntity.yo, focusedEntity.getY());
            double entityZ = MathHelper.lerp((double) tickDelta, focusedEntity.zo, focusedEntity.getZ());

            double currentCameraY = MathHelper.lerp(tickDelta, this.eyeHeightOld, this.eyeHeight);

            Vector3d eyeOffset = animation.getEyeOffset(
                    gravityRotation,
                    new Vector3d(0, currentCameraY, 0),
                    gravityDirection
            );

            this.setPosition(
                    entityX + eyeOffset.x(),
                    entityY + eyeOffset.y(),
                    entityZ + eyeOffset.z()
            );
            if ($$2) {
                if ($$3) {
                    this.setRotation(this.yRot + 180.0F, -this.xRot);
                }

                this.move(-this.getMaxZoom(4.0), 0.0, 0.0);
            } else if (focusedEntity instanceof LivingEntity && ((LivingEntity) focusedEntity).isSleeping()) {
                Direction $$5 = ((LivingEntity) focusedEntity).getBedOrientation();
                this.setRotation($$5 != null ? $$5.toYRot() - 180.0F : 0.0F, 0.0F);
                this.move(0.0, 0.3, 0.0);
            }
        }
    }

    @Inject(
            method = "setRotation(FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/vector/Quaternion;mul(Lnet/minecraft/util/math/vector/Quaternion;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$setRotation(float pPitch, float pYaw, CallbackInfo ci) {
        if (this.entity != null) {
            Direction gravityDirection = GravityAPI.getGravityDirection(this.entity);
            RotationAnimation animation = GravityAPI.getRotationAnimation(entity);
            if (animation == null) {
                return;
            }
            if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) {
                return;
            }

            float partialTick = Minecraft.getInstance().getDeltaFrameTime();
            long timeMs = entity.level.getGameTime() * 50 + (long) (partialTick * 50);

            Quaternion gravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs).copy();
            Quaternion result = new Quaternion(this.rotation);
            result.mul(gravityRotation);
            this.rotation.set(result.i(), result.j(), result.k(), result.r()); // todo: fix rotations
        }
    }
}