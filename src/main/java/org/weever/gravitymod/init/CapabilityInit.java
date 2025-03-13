package org.weever.gravitymod.init;

import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.capability.EntityGravityCap;
import org.weever.gravitymod.capability.EntityGravityCapProvider;
import org.weever.gravitymod.capability.EntityGravityStorage;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GravityMod.MOD_ID)
public class CapabilityInit {
    private static final ResourceLocation ENTITY_UTIL_CAP = new ResourceLocation(GravityMod.MOD_ID, "entity_util");

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(EntityGravityCap.class, new EntityGravityStorage(), () -> new EntityGravityCap(null));
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        event.addCapability(ENTITY_UTIL_CAP, new EntityGravityCapProvider(entity));
    }
}
