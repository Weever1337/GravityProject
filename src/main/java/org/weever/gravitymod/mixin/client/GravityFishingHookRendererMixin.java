package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.FishRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(FishRenderer.class)
public abstract class GravityFishingHookRendererMixin extends EntityRenderer<FishingBobberEntity> {
    @Shadow
    @Final
    private static RenderType RENDER_TYPE;

    protected GravityFishingHookRendererMixin(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Shadow
    private static void vertex(IVertexBuilder buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {}

    @Shadow
    private static float fraction(int value, int max) {return 0.0F;}

    @Shadow
    private static void stringVertex(float p_229104_0_, float p_229104_1_, float p_229104_2_, IVertexBuilder p_229104_3_, Matrix4f p_229104_4_, float p_229104_5_) {}

    @Inject(
            method = "render(Lnet/minecraft/entity/projectile/FishingBobberEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void inject_render(FishingBobberEntity fishingBobberEntity, float yaw, float tickDelta, MatrixStack matrixStack, IRenderTypeBuffer vertexConsumerProvider, int light, CallbackInfo ci) {
        PlayerEntity playerEntity = fishingBobberEntity.getPlayerOwner();
        if (playerEntity == null) return;

        Direction gravityDirection = GravityAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();

        matrixStack.pushPose();
        matrixStack.pushPose();
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        MatrixStack.Entry entry = matrixStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();
        IVertexBuilder vertexConsumer = vertexConsumerProvider.getBuffer(RENDER_TYPE);
        vertex(vertexConsumer, matrix4f, matrix3f, light, 0.0F, 0, 0, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, light, 1.0F, 0, 1, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, light, 1.0F, 1, 1, 0);
        vertex(vertexConsumer, matrix4f, matrix3f, light, 0.0F, 1, 0, 0);
        matrixStack.popPose();
        int armOffset = playerEntity.getMainArm() == HandSide.RIGHT ? 1 : -1;
        ItemStack itemStack = playerEntity.getMainHandItem();
        if (itemStack.getItem() != Items.FISHING_ROD) {
            armOffset = -armOffset;
        }

        float handSwingProgress = playerEntity.getAttackAnim(tickDelta);
        float sinHandSwingProgress = MathHelper.sin(MathHelper.sqrt(handSwingProgress) * 3.1415927F);
        float radBodyYaw = MathHelper.lerp(tickDelta, playerEntity.yBodyRotO, playerEntity.yBodyRot) * 0.017453292F;
        double sinBodyYaw = MathHelper.sin(radBodyYaw);
        double cosBodyYaw = MathHelper.cos(radBodyYaw);
        double scaledArmOffset = (double) armOffset * 0.35D;
        Vector3d lineStart;

        if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && playerEntity == Minecraft.getInstance().player) {
            double fov = this.entityRenderDispatcher.options.fov;
            Vector3d lineOffset = new Vector3d((double)armOffset * -0.36D / fov, -0.045D / fov, 0.4D);
            lineOffset = lineOffset.xRot(-MathHelper.lerp(tickDelta, playerEntity.xRotO, playerEntity.xRot) * 0.017453292F);
            lineOffset = lineOffset.yRot(-MathHelper.lerp(tickDelta, playerEntity.yRotO, playerEntity.yRot) * 0.017453292F);
            lineOffset = lineOffset.yRot(sinHandSwingProgress * 0.5F);
            lineOffset = lineOffset.xRot(-sinHandSwingProgress * 0.7F);

            lineStart = new Vector3d(
                    MathHelper.lerp(tickDelta, playerEntity.xo, playerEntity.getX()),
                    MathHelper.lerp(tickDelta, playerEntity.yo, playerEntity.getY()) + playerEntity.getEyeHeight(),
                    MathHelper.lerp(tickDelta, playerEntity.zo, playerEntity.getZ())
            ).add(RotationUtil.vecPlayerToWorld(lineOffset, gravityDirection));
        } else {
            lineStart = new Vector3d(
                    MathHelper.lerp(tickDelta, playerEntity.xo, playerEntity.getX()),
                    playerEntity.yo + (playerEntity.getY() - playerEntity.yo) * tickDelta,
                    MathHelper.lerp(tickDelta, playerEntity.zo, playerEntity.getZ())
            ).add(RotationUtil.vecPlayerToWorld(
                    new Vector3d(-cosBodyYaw * scaledArmOffset - sinBodyYaw * 0.8D,
                            playerEntity.getEyeHeight() + (playerEntity.isCrouching() ? -0.1875D : 0.0D) - 0.45D,
                            -sinBodyYaw * scaledArmOffset + cosBodyYaw * 0.8D),
                    gravityDirection
            ));
        }


        double bobberX = MathHelper.lerp(tickDelta, fishingBobberEntity.xo, fishingBobberEntity.getX());
        double bobberY = MathHelper.lerp(tickDelta, fishingBobberEntity.yo, fishingBobberEntity.getY()) + 0.25D;
        double bobberZ = MathHelper.lerp(tickDelta, fishingBobberEntity.zo, fishingBobberEntity.getZ());
        float relX = (float) (lineStart.x - bobberX);
        float relY = (float) (lineStart.y - bobberY);
        float relZ = (float) (lineStart.z - bobberZ);
        IVertexBuilder vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderType.lines());

        for (int i = 0; i <= 16; ++i) {
            stringVertex(relX, relY, relZ, vertexConsumer2, matrix4f, fraction(i, 16));
            stringVertex(relX, relY, relZ, vertexConsumer2, matrix4f, fraction(i + 1, 16));
        }

        matrixStack.popPose();
        super.render(fishingBobberEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }
}