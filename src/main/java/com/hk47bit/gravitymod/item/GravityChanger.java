package com.hk47bit.gravitymod.item;

import com.hk47bit.gravitymod.api.GravityDirection;
import com.hk47bit.gravitymod.capability.EntityGravityCapProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class GravityChanger extends Item {
    public String gravityDirection;

    public GravityChanger(GravityProperties properties) {
        super(properties);
        this.gravityDirection = properties.gravityDirection;
    }

    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isAlive() && player.getCapability(EntityGravityCapProvider.CAPABILITY).isPresent()){
            player.getCapability(EntityGravityCapProvider.CAPABILITY).resolve().ifPresent(entityGravityCap -> {
                if (entityGravityCap.getGravityDirection() != GravityDirection.DOWN) {
                    entityGravityCap.setGravityDirection(GravityDirection.DOWN);
                } else {
                    entityGravityCap.setGravityDirection(GravityDirection.valueOf(this.gravityDirection));
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

