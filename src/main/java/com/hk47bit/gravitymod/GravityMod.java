package com.hk47bit.gravitymod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hk47bit.gravitymod.client.ClientEvents;
import com.hk47bit.gravitymod.init.CapabilityInit;
import com.hk47bit.gravitymod.init.ModEffects;
import com.hk47bit.gravitymod.init.ModItems;
import com.hk47bit.gravitymod.network.AddonPackets;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
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
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::onFMLCommonSetup);
        Minecraft mc = Minecraft.getInstance();
        ClientEvents.init(mc);
    }

    public void onFMLCommonSetup(FMLCommonSetupEvent event) {
    	AddonPackets.init();
        CapabilityInit.registerCapabilities();
    }

}
