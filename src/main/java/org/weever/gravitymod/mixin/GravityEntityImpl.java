package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;
import org.weever.gravitymod.capability.EntityGravityCap;
import org.weever.gravitymod.capability.EntityGravityCapProvider;

@Mixin(value = Entity.class, remap = false)
public abstract class GravityEntityImpl implements IGravityEntity {
    @Shadow
    private Vector3d position;
    @Shadow
    private float eyeHeight;

    @Shadow
    public abstract void setDeltaMovement(double p_213293_1_, double p_213293_3_, double p_213293_5_);

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Override
    public double getEyeX() {
        switch (getGravityDirection()) {
            case EAST:
                return this.position.x + (double) this.eyeHeight;
            case WEST:
                return this.position.x - (double) this.eyeHeight;
            default:
                return this.position.x;
        }
    }

    @Override
    public double getEyeZ() {
        return this.position.z;
    }

    @Override
    public Vector3d multiplyDeltaMovementBySpeedFactor(Vector3d deltaMovement, float speedFactor) {
        double x = deltaMovement.x;
        double y = deltaMovement.y;
        double z = deltaMovement.z;

        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                y *= speedFactor;
                z *= speedFactor;
                break;
            case UP:
            case DOWN:
            default:
                x *= speedFactor;
                z *= speedFactor;
                break;
        }
        return new Vector3d(x, y, z);
    }

    @Override
    public double getSwimProbability(Vector3d vector3d) {
        double xSqr = vector3d.x * vector3d.x;
        double ySqr = vector3d.y * vector3d.y;
        double zSqr = vector3d.z * vector3d.z;

        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                return xSqr + (ySqr + zSqr) * 0.2D;
            case UP:
            case DOWN:
            default:
                return ySqr + (xSqr + zSqr) * 0.2D;
        }
    }

    @Override
    public void handleHorizontalCollide(Vector3d requestedDeltaMovement, Vector3d requestedMove, Vector3d actualMove) {
        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                if (!MathHelper.equal(requestedMove.y, actualMove.y)) {
                    this.setDeltaMovement(requestedDeltaMovement.x, 0, requestedDeltaMovement.z);
                }
                if (!MathHelper.equal(requestedMove.z, actualMove.z)) {
                    this.setDeltaMovement(requestedDeltaMovement.x, requestedDeltaMovement.y, 0);
                }
                break;
            case UP:
            case DOWN:
            default:
                if (!MathHelper.equal(requestedMove.x, actualMove.x)) {
                    this.setDeltaMovement(0, requestedDeltaMovement.y, requestedDeltaMovement.z);
                }
                if (!MathHelper.equal(requestedMove.z, actualMove.z)) {
                    this.setDeltaMovement(requestedDeltaMovement.x, requestedDeltaMovement.y, 0);
                }
                break;
        }
    }

    @Override
    public double getVerticalCoordinate() {
        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                return getX();
            default:
                return getY();
        }
    }

    @Override
    public double getVerticalDelta(Vector3d vector3d) {
        switch (getGravityDirection()) {
            case EAST:
                return vector3d.x;
            case WEST:
                return -vector3d.x;
            case UP:
                return -vector3d.y;
            case DOWN:
            default:
                return vector3d.y;
        }
    }

    @Override
    public double getHorizontalDistanceSquared(Vector3d vector) {
        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                return vector.y * vector.y + vector.z * vector.z;
            case UP:
            case DOWN:
            default:
                return vector.x * vector.x + vector.z * vector.z;
        }
    }

    @Override
    public Vector3d transformInputToWorldCoordinates(Vector3d input, float speed, float yRot) {
        double d0 = input.lengthSqr();
        if (d0 < 1.0E-7D) {
            return Vector3d.ZERO;
        }
        Vector3d vector3d = (d0 > 1.0D ? input.normalize() : input).scale(speed);
        float f = MathHelper.sin(yRot * ((float) Math.PI / 180F));
        float f1 = MathHelper.cos(yRot * ((float) Math.PI / 180F));

        switch (this.getGravityDirection()) {
            case EAST:
                return new Vector3d(vector3d.y, -(vector3d.z * f1 + vector3d.x * f), -(vector3d.x * f1 - vector3d.z * f));
            case WEST:
                return new Vector3d(vector3d.y, -(vector3d.z * f1 + vector3d.x * f), (vector3d.x * f1 - vector3d.z * f));
            case UP:
            case DOWN:
            default:
                return new Vector3d(vector3d.x * f1 - vector3d.z * f, vector3d.y, vector3d.z * f1 + vector3d.x * f);
        }
    }

    @Override
    public double verticalDelta(Vector3d actualMove) {
        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                return actualMove.x;
            default:
                return actualMove.y;
        }
    }

    @Override
    public GravityDirection getGravityDirection() {
        Entity entity = (Entity) (Object) this;
        return entity.getCapability(EntityGravityCapProvider.CAPABILITY).map(EntityGravityCap::getGravityDirection).orElse(GravityDirection.DOWN);
    }

    @Override
    public void setGravityDirection(GravityDirection gravityDirection) {
        Entity entity = (Entity) (Object) this;
        entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap -> cap.setGravityDirection(gravityDirection));
    }

    @Override
    public boolean verticalCollision(Vector3d moveRequest, Vector3d actualMove) {
        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                return !MathHelper.equal(moveRequest.x, actualMove.x);
            default:
                return !MathHelper.equal(moveRequest.y, actualMove.y);
        }
    }

    @Override
    public boolean horizontalCollision(Vector3d moveRequest, Vector3d actualMove) {
        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                return !MathHelper.equal(moveRequest.y, actualMove.y) || !MathHelper.equal(moveRequest.z, actualMove.z);
            default:
                return !MathHelper.equal(moveRequest.x, actualMove.x) || !MathHelper.equal(moveRequest.z, actualMove.z);
        }
    }

    @Override
    public boolean isOnGroundBasedOnMove(boolean verticalCollision, Vector3d requestedMove) {
        if (!verticalCollision) return false;

        switch (getGravityDirection()) {
            case EAST:
                return requestedMove.x < 0;
            case WEST:
                return requestedMove.x > 0;
            case UP:
                return requestedMove.y > 0;
            case DOWN:
            default:
                return requestedMove.y < 0;
        }
    }

    @Override
    public double getEyeVerticalCoordinate() {
        switch (getGravityDirection()) {
            case EAST:
                return this.position.x + this.eyeHeight;
            case WEST:
                return this.position.x - this.eyeHeight;
            case UP:
            case DOWN:
            default:
                return this.position.y + (double) this.eyeHeight;
        }
    }

    @Override
    public Vector3d setVerticalCoordinate(float delta, Vector3d vector3d) {
        switch (getGravityDirection()) {
            case EAST:
                return new Vector3d(delta, vector3d.y, vector3d.z);
            case WEST:
                return new Vector3d(-delta, vector3d.y, vector3d.z);
            case UP:
                return new Vector3d(vector3d.x, -delta, vector3d.z);
            case DOWN:
            default:
                return new Vector3d(vector3d.x, delta, vector3d.z);
        }
    }
}