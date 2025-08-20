//package org.weever.gravitymod.mixin.client;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.MouseHelper;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.Direction;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.ModifyVariable;
//import org.weever.gravitymod.util.GravityAPI;
//
//@Mixin(MouseHelper.class)
//public abstract class GravityMouseHelperMixin {
//    @ModifyVariable(method = "turnPlayer", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
//    private double gravitymod$fixHorizontalInvert(double dx) {
//        Entity e = Minecraft.getInstance().getCameraEntity();
//        if (e == null) {
//            return dx;
//        }
//        Direction g = GravityAPI.getGravityDirection(e);
//        return g == Direction.UP ? -dx : dx;
//    }
//}
