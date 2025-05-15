package org.weever.gravitymod.init;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.effects.GravityRotateEffect;

@Mod.EventBusSubscriber(modid = GravityMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, GravityMod.MODID);

    public static final RegistryObject<Effect> ROTATE_UP = EFFECTS.register("rotate_up", () -> new GravityRotateEffect(EffectType.HARMFUL, 0x404040, GravityDirection.UP));
    public static final RegistryObject<Effect> ROTATE_EAST = EFFECTS.register("rotate_east", () -> new GravityRotateEffect(EffectType.HARMFUL, 0x404040, GravityDirection.EAST));
    public static final RegistryObject<Effect> ROTATE_WEST = EFFECTS.register("rotate_west", () -> new GravityRotateEffect(EffectType.HARMFUL, 0x404040, GravityDirection.WEST));
}
