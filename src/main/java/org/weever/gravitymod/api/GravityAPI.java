package org.weever.gravitymod.api;

import org.weever.gravitymod.capability.EntityGravityCap;
import org.weever.gravitymod.capability.EntityGravityCapProvider;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;

public abstract class GravityAPI {
	/* 
	 * Returns the gravity direction of the entity
	 * This is the direction that directly affects this entity
	 */
	public static Direction getGravityDirection(Entity entity) {
		return entity.getCapability(EntityGravityCapProvider.CAPABILITY).map(EntityGravityCap::getGravityDirection).orElse(Direction.DOWN);
	}
	
	/* 
	 * Set the gravity direction of the entity
	 * This is the direction that directly affects this entity
	 */
	public static void setGravityDirection(Entity entity, Direction direction) {
		entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap -> {
			cap.setGravityDirection(direction);
		});
	}
}
