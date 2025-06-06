package org.weever.gravitymod;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.weever.gravitymod.init.ModCapabilities;
import org.weever.gravitymod.init.ModEffects;
import org.weever.gravitymod.init.ModItems;
import org.weever.gravitymod.network.ModPackets;

@Mod(GravityMod.MODID)
public class GravityMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "gravitymod";

    public GravityMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEffects.EFFECTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::onFMLCommonSetup);
    }

    public void onFMLCommonSetup(FMLCommonSetupEvent event) {
        ModPackets.init();
        ModCapabilities.registerCapabilities();
    }
}
