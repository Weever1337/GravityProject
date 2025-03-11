package com.hk47bit.gravitymod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hk47bit.gravitymod.api.GravityAPI;
import com.hk47bit.gravitymod.api.GravityDirection;
import com.hk47bit.gravitymod.util.RotationUtil;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

@Mixin(ActiveRenderInfo.class)
public abstract class CameraMixin {
    @Shadow
    private Entity entity;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    public void setup(IBlockReader blockReader, Entity entity, boolean detached, boolean mirror, float partialTicks, CallbackInfo ci) {
        GravityDirection gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == GravityDirection.DOWN) {
            return;
        }

        double entityLerpedY = MathHelper.lerp(partialTicks, entity.yo, entity.getY());
        double y = MathHelper.lerp((double)partialTicks, this.eyeHeightOld, this.eyeHeight);

        Vector3d eyeOffset = RotationUtil.vectorPlayerToWorld(0, y - entityLerpedY, 0, gravityDirection);
        this.setPosition(
                MathHelper.lerp(partialTicks, entity.xo, entity.getX()) + eyeOffset.x,
                entityLerpedY + eyeOffset.y,
                MathHelper.lerp(partialTicks, entity.zo, entity.getZ()) + eyeOffset.z
        );
    }

    @ModifyArg(
            method = "setRotation(FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/vector/Quaternion;mul(Lnet/minecraft/util/math/vector/Quaternion;)V",
                    ordinal = 0
            )
    )
    public Quaternion modifySetRotation(Quaternion original) {
        GravityDirection gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == GravityDirection.DOWN) {
            return original;
        }

        Quaternion gravityRotation = RotationUtil.getCameraRotationQuaternion(gravityDirection);
        gravityRotation.mul(original);
        return gravityRotation;
    }
}