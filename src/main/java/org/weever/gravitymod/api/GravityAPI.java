package org.weever.gravitymod.api;

import net.minecraft.entity.Entity;

public abstract class GravityAPI {
    /*
     * Returns the gravity direction of the entity
     * This is the direction that directly affects this entity
     */
    public static GravityDirection getGravityDirection(Entity entity) {
        if (!(entity instanceof IGravityEntity)) {
            return GravityDirection.DOWN;
        }
        return ((IGravityEntity) entity).getGravityDirection();
    }

    /*
     * Set the gravity direction of the entity
     * This is the direction that directly affects this entity
     */
    public static void setGravityDirection(Entity entity, GravityDirection direction) {
        if (!(entity instanceof IGravityEntity)) {
            return;
        }
        ((IGravityEntity) entity).setGravityDirection(direction);
    }
}
