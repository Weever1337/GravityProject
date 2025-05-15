package org.weever.gravitymod.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.capability.EntityGravityCap;
import org.weever.gravitymod.capability.EntityGravityCapProvider;
import org.weever.gravitymod.capability.EntityGravityStorage;

@Mod.EventBusSubscriber(modid = GravityMod.MODID)
public class ModCapabilities {
    private static final ResourceLocation ENTITY_UTIL_CAP = new ResourceLocation(GravityMod.MODID, "entity_util");

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(EntityGravityCap.class, new EntityGravityStorage(), () -> new EntityGravityCap(null));
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        event.addCapability(ENTITY_UTIL_CAP, new EntityGravityCapProvider(entity));
    }

    private static void syncData(PlayerEntity player) {
        player.getCapability(EntityGravityCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.syncWithClient();
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncData(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncData(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncData(event.getPlayer());
    }
}
