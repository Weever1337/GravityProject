package org.weever.gravitymod.mixin;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(LookController.class)
public abstract class GravityLookControllerMixin {
    @Shadow
    @Final
    protected MobEntity mob;

    @Shadow
    protected double wantedX;

    @Shadow
    protected double wantedY;

    @Shadow
    protected double wantedZ;

    @Inject(
            method = "getYRotD",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$localYRot(CallbackInfoReturnable<Float> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.mob);
        if (gravityDirection == Direction.DOWN) return;

        Vector3d local = RotationUtil.vecWorldToPlayer(
                new Vector3d(this.wantedX, this.wantedY, this.wantedZ).subtract(this.mob.position()),
                gravityDirection);
        cir.setReturnValue((float) (MathHelper.atan2(local.z, local.x) * (double) (180F / (float) Math.PI)) - 90.0F);
    }

    @Inject(
            method = "getXRotD",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$localXRot(CallbackInfoReturnable<Float> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.mob);
        if (gravityDirection == Direction.DOWN) return;

        Vector3d local = RotationUtil.vecWorldToPlayer(
                new Vector3d(this.wantedX, this.wantedY, this.wantedZ).subtract(this.mob.getEyePosition(1.0F)),
                gravityDirection);
        double horizontal = Math.sqrt(local.x * local.x + local.z * local.z);
        cir.setReturnValue((float) (-(MathHelper.atan2(local.y, horizontal) * (double) (180F / (float) Math.PI))));
    }
}
