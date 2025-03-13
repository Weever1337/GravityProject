package org.weever.gravitymod.mixin.client;

import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.weever.gravitymod.api.GravityAPI;
import com.hk47bit.gravitymod.util.RotationUtil;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

@Mixin(ActiveRenderInfo.class)
public abstract class CameraMixin {
    @Shadow private Entity entity;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;

    @Shadow @Final private Quaternion rotation;

    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    public void setup_applyGravityOffset(IBlockReader blockReader, Entity entity, boolean detached, boolean mirror, float partialTicks, CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }
        Direction gravityDirection = GravityAPI.getGravityDirection(this.entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        double lerpX = MathHelper.lerp(partialTicks, this.entity.xo, this.entity.getX());
        double lerpY = MathHelper.lerp(partialTicks, this.entity.yo, this.entity.getY());
        double lerpZ = MathHelper.lerp(partialTicks, this.entity.zo, this.entity.getZ());

        float relativeEyeHeight = MathHelper.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight);

        Vector3d eyeOffsetPlayerSpace = new Vector3d(0.0D, relativeEyeHeight, 0.0D);
        Vector3d eyeOffsetWorldSpace = RotationUtil.vecPlayerToWorld(eyeOffsetPlayerSpace, gravityDirection);

        this.setPosition(
                lerpX + eyeOffsetWorldSpace.x,
                lerpY + eyeOffsetWorldSpace.y,
                lerpZ + eyeOffsetWorldSpace.z
        );
    }

    @Inject(method = "setRotation(FF)V", at = @At("RETURN"))
    private void setRotation_applyGravityRotation(float yaw, float pitch, CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }
        Direction gravityDirection = GravityAPI.getGravityDirection(this.entity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        Quaternion gravityRotation = RotationUtil.getCameraRotationQuaternion(gravityDirection);
        gravityRotation = gravityRotation.copy();

        gravityRotation.mul(this.rotation);

        this.rotation.set(gravityRotation.i(), gravityRotation.j(), gravityRotation.k(), gravityRotation.r());
    }
}