package org.weever.gravitymod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.weever.gravitymod.access.IGravityEntity;
import org.weever.gravitymod.util.GravityAPI;

public class GravityChanger extends Item {
    public String gravityDirection;

    public GravityChanger(GravityProperties properties) {
        super(properties);
        this.gravityDirection = properties.gravityDirection;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isAlive()) {
            Direction playerGravityDirection = GravityAPI.getGravityDirection(player);
            Direction gravityDirection = Direction.valueOf(this.gravityDirection.toUpperCase());
            if (playerGravityDirection != Direction.DOWN && playerGravityDirection == gravityDirection) {
                ((IGravityEntity) player).gravitymod$setBaseGravityDirection(Direction.DOWN);
            } else {
                ((IGravityEntity) player).gravitymod$setBaseGravityDirection(gravityDirection);
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

