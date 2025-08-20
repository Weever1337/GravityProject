package org.weever.gravitymod.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.weever.gravitymod.capability.EntityGravityCapProvider;

import java.util.function.Supplier;

public class SyncDirectionCap {
    private final int entityId;

    public SyncDirectionCap(PacketBuffer buf) {
        this.entityId = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
        {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(entityId);
            if (entity != null) {
                entity.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap ->
                {
//                    cap.setGravityDirection(direction);
                });
            }
        });
        return true;
    }
}
