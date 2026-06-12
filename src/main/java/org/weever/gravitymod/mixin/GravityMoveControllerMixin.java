package org.weever.gravitymod.mixin;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(MovementController.class)
public abstract class GravityMoveControllerMixin {
    @Shadow
    @Final
    protected MobEntity mob;

    @Shadow
    protected double wantedX;

    @Shadow
    protected double wantedY;

    @Shadow
    protected double wantedZ;

    @Shadow
    protected double speedModifier;

    @Shadow
    protected MovementController.Action operation;

    @Shadow
    protected abstract float rotlerp(float current, float target, float maxDelta);

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$tickLocalFrame(CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.mob);
        if (gravityDirection == Direction.DOWN) return;
        if (this.operation != MovementController.Action.MOVE_TO) return;

        ci.cancel();
        this.operation = MovementController.Action.WAIT;
        Vector3d local = RotationUtil.vecWorldToPlayer(
                new Vector3d(this.wantedX, this.wantedY, this.wantedZ).subtract(this.mob.position()),
                gravityDirection);
        double dx = local.x;
        double dy = local.y;
        double dz = local.z;
        double distSqr = dx * dx + dy * dy + dz * dz;
        if (distSqr < 2.5E-7D) {
            this.mob.setZza(0.0F);
            return;
        }

        float yaw = (float) (MathHelper.atan2(dz, dx) * (double) (180F / (float) Math.PI)) - 90.0F;
        this.mob.yRot = this.rotlerp(this.mob.yRot, yaw, 90.0F);
        this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        if (dy > (double) this.mob.maxUpStep && dx * dx + dz * dz < (double) Math.max(1.0F, this.mob.getBbWidth())) {
            this.mob.getJumpControl().jump();
            this.operation = MovementController.Action.JUMPING;
        }
    }
}
