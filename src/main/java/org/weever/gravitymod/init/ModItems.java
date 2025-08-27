package org.weever.gravitymod.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.item.GravityChanger;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GravityMod.MODID);
    public static final ItemGroup GRAVITY_TAB = new ItemGroup("gravitymod") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(Items.POTION);
        }
    };

    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_UP = ITEMS.register("gravity_changer_up", () ->
            new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(Direction.UP.name()).tab(GRAVITY_TAB).stacksTo(1)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_DOWN = ITEMS.register("gravity_changer_down", () ->
            new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(Direction.DOWN.name()).tab(GRAVITY_TAB).stacksTo(1)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_EAST = ITEMS.register("gravity_changer_east", () ->
            new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(Direction.EAST.name()).tab(GRAVITY_TAB).stacksTo(1)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_WEST = ITEMS.register("gravity_changer_west", () ->
            new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(Direction.WEST.name()).tab(GRAVITY_TAB).stacksTo(1)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_NORTH = ITEMS.register("gravity_changer_north", () ->
            new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(Direction.NORTH.name()).tab(GRAVITY_TAB).stacksTo(1)));
    public static final RegistryObject<GravityChanger> GRAVITY_CHANGER_SOUTH = ITEMS.register("gravity_changer_south", () ->
            new GravityChanger((GravityChanger.GravityProperties) new GravityChanger.GravityProperties().gravityDirection(Direction.SOUTH.name()).tab(GRAVITY_TAB).stacksTo(1)));
}
