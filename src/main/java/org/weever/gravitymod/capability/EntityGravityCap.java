package org.wever.gravitymod.capability;

import org.wever.gravitymod.network.AddonPackets;
import org.wever.gravitymod.network.server.SyncDirectionCap;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;

public class EntityGravityCap {
    private final Entity entity;
    public Direction direction = Direction.DOWN;

    public void setGravityDirection(Direction direction){
        this.direction = direction;
        if (!entity.level.isClientSide()) {
        	AddonPackets.sendToClientsTrackingAndSelf(new SyncDirectionCap(entity.getId(), direction), entity);
        } else {
//            AddonPackets.sendToServer(new SyncDirectionCap(entity.getId(), direction), entity.getServerPlayer());
        }
        
//        if (direction == Direction.DOWN){
//            entity.setNoGravity(true);
//        } else {
//            entity.setNoGravity(false);
//        }
    }

    public Direction getGravityDirection() {
        return direction;
    }

    public EntityGravityCap(Entity entity){
        this.entity = entity;
    }
}
