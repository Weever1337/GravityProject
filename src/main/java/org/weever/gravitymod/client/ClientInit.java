package org.wever.gravitymod.client;

import org.wever.gravitymod.GravityMod;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = GravityMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {
    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
//        ClientEvents.init(event.getMinecraftSupplier().get());
    }
}
