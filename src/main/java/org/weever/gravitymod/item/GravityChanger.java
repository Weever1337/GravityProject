package org.weever.gravitymod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.weever.gravitymod.api.GravityAPI;
import org.weever.gravitymod.api.GravityDirection;

public class GravityChanger extends Item {
    public String gravityDirection;

    public GravityChanger(GravityProperties properties) {
        super(properties);
        this.gravityDirection = properties.gravityDirection;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isAlive()) {
            GravityDirection playerGravityDirection = GravityAPI.getGravityDirection(player);
            GravityDirection gravityDirection = GravityDirection.valueOf(this.gravityDirection.toUpperCase());
            if (playerGravityDirection != GravityDirection.DOWN && playerGravityDirection == gravityDirection) {
                GravityAPI.setGravityDirection(player, GravityDirection.DOWN);
            } else {
                GravityAPI.setGravityDirection(player, gravityDirection);
            }
        }
        return ActionResult.pass(this.getDefaultInstance());
    }

    public static class GravityProperties extends Item.Properties {
        public String gravityDirection;

        public GravityChanger.GravityProperties gravityDirection(String direction) {
            this.gravityDirection = direction;
            return this;
        }
    }
}

