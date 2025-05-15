package org.weever.gravitymod.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.network.ModPackets;
import org.weever.gravitymod.network.server.SyncDirectionCap;

public class EntityGravityCap {
    private final Entity entity;
    private GravityDirection direction = GravityDirection.DOWN;

    public EntityGravityCap(Entity entity) {
        this.entity = entity;
    }

    public GravityDirection getGravityDirection() {
        return direction;
    }

    public void setGravityDirection(GravityDirection direction) {
        this.direction = direction;
        if (!entity.level.isClientSide()) {
            ModPackets.sendToClientsTrackingAndSelf(new SyncDirectionCap(entity.getId(), direction), entity);
        }

        //        if (direction == Direction.DOWN){
        //            entity.setNoGravity(true);
        //        } else {
        //            entity.setNoGravity(false);
        //        }
    }

    public void syncWithClient() {
        ServerPlayerEntity player = (ServerPlayerEntity) this.entity;
        ModPackets.sendToClient(new SyncDirectionCap(entity.getId(), this.getGravityDirection()), player);
    }
}
