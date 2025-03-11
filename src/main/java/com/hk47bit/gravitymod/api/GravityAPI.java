package com.hk47bit.gravitymod.api;

import com.hk47bit.gravitymod.capability.EntityGravityCap;
import com.hk47bit.gravitymod.capability.EntityGravityCapProvider;
import net.minecraft.entity.Entity;

public abstract class GravityAPI {
	/* 
	 * Returns the gravity direction of the entity
	 * This is the direction that directly affects this entity
	 */
	public static GravityDirection getGravityDirection(Entity entity) {
		return entity.getCapability(EntityGravityCapProvider.CAPABILITY).map(EntityGravityCap::getGravityDirection).orElse(GravityDirection.DOWN);
	}
	
	/* 
	 * Set the gravity direction of the entity
	 * This is the direction that directly affects this entity
	 */
	public static void setGravityDirection(Entity entity, GravityDirection direction) {
		entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap -> {
			cap.setGravityDirection(direction);
		});
	}
}
