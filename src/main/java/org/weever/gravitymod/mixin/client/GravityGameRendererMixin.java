package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationAnimation;

import java.util.Objects;

@Mixin(GameRenderer.class)
public abstract class GravityGameRendererMixin {

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/matrix/MatrixStack;mulPose(Lnet/minecraft/util/math/vector/Quaternion;)V",
                    ordinal = 4,
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$inject_renderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        Entity focusedEntity = this.mainCamera.getEntity();
        Direction gravityDirection = GravityAPI.getGravityDirection(focusedEntity);
        RotationAnimation animation = GravityAPI.getRotationAnimation(focusedEntity);
        if (animation == null) {
            return;
        }
        long timeMs = focusedEntity.level.getGameTime() * 50 + (long) (tickDelta * 50);
        Quaternion currentGravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);


        if (animation.isInAnimation()) {
            // make sure that frustum culling updates when running rotation animation
            Minecraft.getInstance().levelRenderer.needsUpdate();
        }

        matrix.mulPose(currentGravityRotation);
    }

    @Shadow
    @Final
    private ActiveRenderInfo mainCamera;
}