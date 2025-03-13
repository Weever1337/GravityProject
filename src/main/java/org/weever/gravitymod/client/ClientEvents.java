package org.wever.gravitymod.client;

import org.wever.gravitymod.GravityMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GravityMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
}