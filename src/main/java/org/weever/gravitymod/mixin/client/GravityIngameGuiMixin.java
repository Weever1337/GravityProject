package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationAnimation;

@Mixin(IngameGui.class)
public abstract class GravityIngameGuiMixin {
    // F3 debug crosshair (xyz axes gizmo) is built from the camera's local, so I'm fixing this
    @Inject(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;rotatef(FFFF)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$rotateDebugCrosshair(MatrixStack matrixStack, CallbackInfo ci) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) return;

        Direction gravityDirection = GravityAPI.getGravityDirection(cameraEntity);
        RotationAnimation animation = GravityAPI.getRotationAnimation(cameraEntity);
        if (animation == null) return;

        long timeMs = cameraEntity.level.getGameTime() * 50 + (long) (Minecraft.getInstance().getFrameTime() * 50);
        animation.update(timeMs);
        if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) return;

        Quaternion gravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);
        RenderSystem.multMatrix(new Matrix4f(gravityRotation));
    }
}