package com.hk47bit.gravitymod.init;

import com.hk47bit.gravitymod.GravityMod;
import com.hk47bit.gravitymod.api.GravityDirection;
import com.hk47bit.gravitymod.item.GravityChanger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GravityMod.MOD_ID);
    public static final ItemGroup GRAVITY_TAB = new ItemGroup("gravitymod") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(Items.POTION);
        }
    };

    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_UP = ITEMS.register("gravity_changer_up",
            () -> new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(GravityDirection.UP.toString()).tab(GRAVITY_TAB)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_DOWN = ITEMS.register("gravity_changer_down",
            () -> new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(GravityDirection.DOWN.toString()).tab(GRAVITY_TAB)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_EAST = ITEMS.register("gravity_changer_east",
            () -> new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(GravityDirection.EAST.toString()).tab(GRAVITY_TAB)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_WEST = ITEMS.register("gravity_changer_west",
            () -> new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(GravityDirection.WEST.toString()).tab(GRAVITY_TAB)));

}
