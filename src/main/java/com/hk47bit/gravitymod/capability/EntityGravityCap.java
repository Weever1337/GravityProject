package com.hk47bit.gravitymod.capability;

import com.hk47bit.gravitymod.api.GravityDirection;
import com.hk47bit.gravitymod.network.AddonPackets;
import com.hk47bit.gravitymod.network.server.SyncDirectionCap;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;

public class EntityGravityCap {
    private final Entity entity;
    public GravityDirection direction = GravityDirection.DOWN;

    public void setGravityDirection(GravityDirection direction){
        this.direction = direction;
        if (!entity.level.isClientSide()) {
        	AddonPackets.sendToClientsTrackingAndSelf(new SyncDirectionCap(entity.getId(), direction), entity);
        } else {
//            AddonPackets.sendToServer(new SyncDirectionCap(entity.getId(), direction), entity.getServerPlayer());
        }
        
        if (direction == GravityDirection.DOWN){
            entity.setNoGravity(true);
        } else {
            entity.setNoGravity(false);
        }
    }

    public GravityDirection getGravityDirection() {
        return direction;
    }

    public EntityGravityCap(Entity entity){
        this.entity = entity;
    }

}
