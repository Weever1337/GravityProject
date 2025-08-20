package org.weever.gravitymod.util;


import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.weever.gravitymod.access.IClientEntity;
import org.weever.gravitymod.access.IGravityEntity;

public class GravityAPI {
    /**
     * Returns the applied gravity direction for the given entity
     */
    public static Direction getGravityDirection(Entity entity) {
        if (entity == null) {
            return Direction.DOWN;
        }

        return ((IGravityEntity)entity).gravitymod$getGravityDirection();
    }

    public static double getGravityStrength(Entity entity) {
        if (entity == null) {
            return 1;
        }

        return ((IGravityEntity)entity).gravitymod$getGravityStrength();
    }
    /**
     * Sets the world relative velocity for the given player
     * Using minecraft's methods to set the velocity of an entity will set player relative velocity
     */
    public static void setWorldVelocity(Entity entity, Vector3d worldVelocity) {
        entity.setDeltaMovement(RotationUtil.vecWorldToPlayer(worldVelocity, getGravityDirection(entity)));
    }

    public static RotationAnimation getRotationAnimation(Entity entity) {
        return ((IClientEntity)entity).gravitymod$getGravityAnimation();
    }
}
