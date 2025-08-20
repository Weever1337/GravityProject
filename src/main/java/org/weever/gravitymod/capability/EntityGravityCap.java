package org.weever.gravitymod.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.weever.gravitymod.network.ModPackets;
import org.weever.gravitymod.network.server.SyncDirectionCap;

public class EntityGravityCap {
    private final Entity entity;

    public EntityGravityCap(Entity entity) {
        this.entity = entity;
    }

    public void syncWithClient() {
        ServerPlayerEntity player = (ServerPlayerEntity) this.entity;
//        ModPackets.sendToClient(new SyncDirectionCap(entity.getId(), this.getGravityDirection()), player);
    }
}
