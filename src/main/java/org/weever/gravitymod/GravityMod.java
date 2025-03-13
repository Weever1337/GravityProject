package org.wever.gravitymod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.wever.gravitymod.init.CapabilityInit;
import org.wever.gravitymod.init.ModEffects;
import org.wever.gravitymod.init.ModItems;
import org.wever.gravitymod.network.AddonPackets;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GravityMod.MOD_ID)
public class GravityMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "gravitymod";

    public GravityMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEffects.EFFECTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::onFMLCommonSetup);
    }

    public void onFMLCommonSetup(FMLCommonSetupEvent event) {
    	AddonPackets.init();
        CapabilityInit.registerCapabilities();
    }
}
