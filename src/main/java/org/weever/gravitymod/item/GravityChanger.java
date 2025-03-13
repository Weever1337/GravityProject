package org.weever.gravitymod.item;

import org.weever.gravitymod.capability.EntityGravityCapProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class GravityChanger extends Item {
    public String gravityDirection;

    public GravityChanger(GravityProperties properties) {
        super(properties);
        this.gravityDirection = properties.gravityDirection;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isAlive() && player.getCapability(EntityGravityCapProvider.CAPABILITY).isPresent()){
            player.getCapability(EntityGravityCapProvider.CAPABILITY).resolve().ifPresent(entityGravityCap -> {
                if (entityGravityCap.getGravityDirection() != Direction.DOWN) {
                    entityGravityCap.setGravityDirection(Direction.DOWN);
                } else {
                    entityGravityCap.setGravityDirection(Direction.byName(this.gravityDirection));
                }
            });
        }
        return ActionResult.pass(this.getDefaultInstance());
    }

    public static class GravityProperties extends Item.Properties {
        public String gravityDirection;
        public GravityChanger.GravityProperties gravityDirection(String direction){
            this.gravityDirection = direction;
            return this;
        }
    }
}

