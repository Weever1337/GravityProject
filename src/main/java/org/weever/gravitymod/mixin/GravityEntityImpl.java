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
        if (getGravityDirection() == GravityDirection.EAST) {
            return this.position.x + (double) this.eyeHeight;
        } else if (getGravityDirection() == GravityDirection.WEST) {
            return this.position.x - (double) this.eyeHeight;
        } else {
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
        if (this.getGravityDirection() != GravityDirection.EAST && this.getGravityDirection() != GravityDirection.WEST) {
            x *= speedFactor;
        }
        double y = deltaMovement.y;
        if (this.getGravityDirection() != GravityDirection.DOWN) {
            y *= speedFactor;
        }
        double z = deltaMovement.z;
        return new Vector3d(x, y, z);
    }

    @Override
    public double getSwimProbability(Vector3d vector3d2) {
        float result = 0;
        if (this.getGravityDirection() == GravityDirection.EAST || this.getGravityDirection() == GravityDirection.WEST) {
            result += vector3d2.x * vector3d2.x;
        } else {
            result += vector3d2.x * vector3d2.x * 0.2;
        }
        if (this.getGravityDirection() == GravityDirection.DOWN) {
            result += vector3d2.y * vector3d2.y;
        } else {
            result += vector3d2.y * vector3d2.y * 0.2;
        }
        result += vector3d2.z * vector3d2.z * 0.2;
        return result;
    }

    @Override
    public void handleHorizontalCollide(Vector3d requestedDeltaMovement, Vector3d requestedMove, Vector3d actualMove) {
        if (this.getGravityDirection() != GravityDirection.DOWN) {
            if (!MathHelper.equal(requestedMove.y, actualMove.y)) {
                this.setDeltaMovement(requestedDeltaMovement.x, 0, requestedDeltaMovement.z);
            }
        }
        if (this.getGravityDirection() != GravityDirection.EAST && this.getGravityDirection() != GravityDirection.WEST) {
            if (!MathHelper.equal(requestedMove.x, actualMove.x)) {
                this.setDeltaMovement(0, requestedDeltaMovement.y, requestedDeltaMovement.z);
            }
        }
    }

    @Override
    public double getVerticalCoordinate() {
        if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
            return getX();
        }
        return getY();
    }

    @Override
    public double getVerticalDelta(Vector3d vector3d) {
        GravityDirection dir = getGravityDirection();
        if (dir == GravityDirection.EAST) {
            return vector3d.x;
        } else if (dir == GravityDirection.WEST) {
            return -vector3d.x;
        } else if (dir == GravityDirection.UP) {
            return -vector3d.y;
        }
        return vector3d.y;
    }

    @Override
    public double getHorizontalDistanceSquared(Vector3d vector) {
        if (this.getGravityDirection() == GravityDirection.EAST || this.getGravityDirection() == GravityDirection.WEST) {
            return vector.y * vector.y + vector.z * vector.z;
        }
        return vector.x * vector.x + vector.z * vector.z;
    }

    @Override
    public Vector3d transformInputToWorldCoordinates(Vector3d input, float speed, float yRot) {
        double d0 = input.lengthSqr();
        if (d0 < 1.0E-7D) {
            return Vector3d.ZERO;
        } else {
            Vector3d vector3d = (d0 > 1.0D ? input.normalize() : input).scale(speed);
            float f = MathHelper.sin(yRot * ((float) Math.PI / 180F));
            float f1 = MathHelper.cos(yRot * ((float) Math.PI / 180F));
            if (this.getGravityDirection() == GravityDirection.EAST) {
                return new Vector3d(vector3d.y, -(vector3d.z * (double) f1 + vector3d.x * (double) f), -(vector3d.x * (double) f1 - vector3d.z * (double) f));
            } else if (this.getGravityDirection() == GravityDirection.WEST) {
                return new Vector3d(vector3d.y, -(vector3d.z * (double) f1 + vector3d.x * (double) f), (vector3d.x * (double) f1 - vector3d.z * (double) f));
            } else {
                return new Vector3d(vector3d.x * (double) f1 - vector3d.z * (double) f, vector3d.y, vector3d.z * (double) f1 + vector3d.x * (double) f);
            }
        }
    }

    @Override
    public double verticalDelta(Vector3d actualMove) {
        if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
            return actualMove.x;
        }
        return actualMove.y;
    }

    @Override
    public GravityDirection getGravityDirection() {
        Entity entity = (Entity) (Object) this;
        return entity.getCapability(EntityGravityCapProvider.CAPABILITY).map(EntityGravityCap::getGravityDirection).orElse(GravityDirection.DOWN);
    }

    @Override
    public void setGravityDirection(GravityDirection gravityDirection) {
        Entity entity = (Entity) (Object) this;
        entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap ->
        {
            cap.setGravityDirection(gravityDirection);
        });
    }

    @Override
    public boolean verticalCollision(Vector3d moveRequest, Vector3d actualMove) {
        if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
            return !MathHelper.equal(moveRequest.x, actualMove.x);
        }
        return !MathHelper.equal(moveRequest.y, actualMove.y);
    }

    @Override
    public boolean horizontalCollision(Vector3d moveRequest, Vector3d actualMove) {
        if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
            return !MathHelper.equal(moveRequest.y, actualMove.y) || !MathHelper.equal(moveRequest.z, actualMove.z);
        }
        return !MathHelper.equal(moveRequest.x, actualMove.x) || !MathHelper.equal(moveRequest.z, actualMove.z);
    }

    @Override
    public boolean isOnGroundBasedOnMove(boolean verticalCollision, Vector3d requestedMove) {
        if (getGravityDirection() == GravityDirection.EAST) {
            return verticalCollision && requestedMove.x < 0;
        }
        if (getGravityDirection() == GravityDirection.WEST) {
            return verticalCollision && requestedMove.x > 0;
        }
        return verticalCollision && requestedMove.y < 0;
    }

    @Override
    public double getEyeVerticalCoordinate() {
        if (getGravityDirection() == GravityDirection.EAST) {
            return this.position.x + this.eyeHeight;
        } else if (getGravityDirection() == GravityDirection.WEST) {
            return this.position.x - this.eyeHeight;
        }
        return this.position.y + (double) this.eyeHeight;
    }

    @Override
    public Vector3d setVerticalCoordinate(float deltaY, Vector3d vector3d) {
        GravityDirection direction = getGravityDirection();
        if (direction == GravityDirection.EAST) {
            return new Vector3d(deltaY, vector3d.y, vector3d.z);
        } else if (direction == GravityDirection.WEST) {
            return new Vector3d(-deltaY, vector3d.y, vector3d.z);
        } else if (getGravityDirection() == GravityDirection.UP) {
            return new Vector3d(vector3d.x, -deltaY, vector3d.z);
        }
        return new Vector3d(vector3d.x, deltaY, vector3d.z);
    }
}
