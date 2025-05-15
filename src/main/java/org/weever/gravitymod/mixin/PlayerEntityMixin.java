package org.weever.gravitymod.mixin;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.weever.gravitymod.api.GravityDirection;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    @Shadow
    @Final
    public PlayerAbilities abilities;
    @Shadow
    protected int jumpTriggerTime;

    @Shadow
    public abstract void checkMovementStatistics(double x, double y, double z);

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract void aiStep();

    /**
     * @author Weever1337
     * @reason Override travel method to handle gravity direction for players.
     */
    @Overwrite
    public void travel(Vector3d travelVector) {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        if (this.isSwimming() && !this.isPassenger()) {
            double d3 = this.getLookAngle().y;
            double d4 = d3 < -0.2 ? 0.085 : 0.06;
            if (d3 <= 0.0D || this.jumping || !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0F - 0.1, this.getZ())).getFluidState().isEmpty()) {
                Vector3d vector3d1 = this.getDeltaMovement();
                this.setDeltaMovement(vector3d1.add(0.0F, (d3 - vector3d1.y) * d4, 0.0F));
            }
        }

        if (this.abilities.flying && !this.isPassenger()) {
            GravityDirection gravityDirection = this.getGravityDirection();

            double verticalVelocityComponent = this.getVerticalDelta(this.getDeltaMovement());

            float originalFlyingSpeed = this.flyingSpeed;
            this.flyingSpeed = this.abilities.getFlyingSpeed() * (float) (this.isSprinting() ? 2 : 1);

            super.travel(travelVector);

            Vector3d newVelocity = this.getDeltaMovement();
            double dampenedVerticalVelocity = verticalVelocityComponent * 0.6D;

            double finalX = newVelocity.x;
            double finalY = newVelocity.y;
            double finalZ = newVelocity.z;

            switch (gravityDirection) {
                case EAST:
                    finalX = dampenedVerticalVelocity;
                    break;
                case WEST:
                    finalX = -dampenedVerticalVelocity;
                    break;
                case UP:
                    finalY = -dampenedVerticalVelocity;
                    break;
                case DOWN:
                default:
                    finalY = dampenedVerticalVelocity;
                    break;
            }

            this.setDeltaMovement(finalX, finalY, finalZ);

            this.flyingSpeed = originalFlyingSpeed;
            this.fallDistance = 0.0F;
            this.setSharedFlag(7, false);
        } else {
            super.travel(travelVector);
        }

        this.checkMovementStatistics(this.getX() - d0, this.getY() - d1, this.getZ() - d2);
    }
}