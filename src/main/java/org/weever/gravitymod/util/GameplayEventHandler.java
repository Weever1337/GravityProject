package org.weever.gravitymod.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.api.GravityAPI;
import org.weever.gravitymod.api.GravityDirection;

@Mod.EventBusSubscriber(modid = GravityMod.MODID)
public class GameplayEventHandler {
//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        PlayerEntity player = event.player;
//        if (player != null && GravityAPI.getGravityDirection(player) != GravityDirection.DOWN && !player.level.isClientSide) {
//            GravityDirection gravityDirection = GravityAPI.getGravityDirection(player);
//            System.out.println("SERVER: " + gravityDirection);
//        }
//    }
}