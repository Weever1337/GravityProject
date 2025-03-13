package org.weever.gravitymod.init;

import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.effects.RotateUpEffect;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = GravityMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, GravityMod.MOD_ID);

    public static final RegistryObject<Effect> ROTATE_UP = EFFECTS.register("rotate_up",
            () -> new RotateUpEffect(EffectType.HARMFUL, 0x404040));
}
