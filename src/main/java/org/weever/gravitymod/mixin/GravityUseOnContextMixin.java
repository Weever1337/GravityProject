package org.weever.gravitymod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(ItemUseContext.class)
public class GravityUseOnContextMixin {

    @Shadow @Final @Nullable private PlayerEntity player;

    @Inject(
            method = "getRotation",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void wrapOperation_getPlayerYaw_getYaw_0(CallbackInfoReturnable<Float> cir) {
        if (this.player == null)
            return;
        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN)
            return;

        cir.setReturnValue(RotationUtil.rotPlayerToWorld(this.player.yRot, this.player.xRot, gravityDirection).x);
    }
}
