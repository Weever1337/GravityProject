package org.weever.gravitymod.api;

import net.minecraft.util.math.vector.Vector3d;

public interface IGravityEntity {
    double getHorizontalDistanceSquared(Vector3d vector);

    void handleHorizontalCollide(Vector3d requestedDeltaMovement, Vector3d requestedMove, Vector3d actualMove);

    GravityDirection getGravityDirection();

    void setGravityDirection(GravityDirection gravityDirection);

    boolean verticalCollision(Vector3d moveRequest, Vector3d actualMove);

    boolean horizontalCollision(Vector3d moveRequest, Vector3d actualMove);

    boolean isOnGroundBasedOnMove(boolean verticalCollision, Vector3d requestedMove);

    double verticalDelta(Vector3d actualMove);

    double getVerticalCoordinate();

    Vector3d getVerticalVectorNullifier();

    double getEyeX();

    double getEyeZ();

    double getEyeVerticalCoordinate();

    Vector3d setVerticalCoordinate(float deltaY, Vector3d vector3d);

    double getSwimProbability(Vector3d vector3d);

    Vector3d multiplyDeltaMovementBySpeedFactor(Vector3d vector3d, float speedFactor);

    double getVerticalDelta(Vector3d vector3d);

    Vector3d transformInputToWorldCoordinates(Vector3d input, float speed, float yRot);
}