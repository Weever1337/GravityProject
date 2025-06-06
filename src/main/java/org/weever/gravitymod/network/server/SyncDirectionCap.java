package org.weever.gravitymod.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.capability.EntityGravityCapProvider;

import java.util.function.Supplier;

public class SyncDirectionCap {
    private final int entityId;
    private final GravityDirection direction;

    public SyncDirectionCap(int entityId, GravityDirection direction) {
        this.entityId = entityId;
        this.direction = direction;
    }

    public SyncDirectionCap(PacketBuffer buf) {
        this.entityId = buf.readInt();
        this.direction = buf.readEnum(GravityDirection.class);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeEnum(direction);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
        {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(entityId);
            if (entity != null) {
                entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap ->
                {
                    cap.setGravityDirection(direction);
                });
            }
        });
        return true;
    }
}
