package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(PlayerRenderer.class)
public abstract class GravityPlayerRendererMixin {
    // crouch rotation fix
    @Inject(
            method = "getRenderOffset(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;F)Lnet/minecraft/util/math/vector/Vector3d;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void gravitymod$rotateRenderOffset(AbstractClientPlayerEntity player, float partialTicks, CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) return;

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
    }

    /**elytra rotation fix*/
    @ModifyVariable(
            method = "setupRotations*",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private Vector3d modify_setupTransforms_Vector3dd_0(Vector3d vec, AbstractClientPlayerEntity instance, MatrixStack $$1, float $$2, float $$3, float partialTick) {
        Direction gravityDirection = GravityAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN)
            return vec;
        Vector3d viewVector = instance.getViewVector(partialTick);

        return RotationUtil.vecWorldToPlayer(viewVector, gravityDirection);
    }
}