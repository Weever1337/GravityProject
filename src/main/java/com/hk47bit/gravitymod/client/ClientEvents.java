package com.hk47bit.gravitymod.client;


import com.hk47bit.gravitymod.GravityMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GravityMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    private final Minecraft mc;

    private ClientEvents(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents(mc));
    }
}
