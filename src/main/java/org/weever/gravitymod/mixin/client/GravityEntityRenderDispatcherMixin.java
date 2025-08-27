package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.weever.gravitymod.v1_20_1.util.Mth;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationAnimation;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.BlockPosUtil;

@Mixin(EntityRendererManager.class)
public abstract class GravityEntityRenderDispatcherMixin {
    @Shadow
    @Final
    private static RenderType SHADOW_RENDER_TYPE;

    @Shadow
    private boolean shouldRenderShadow;

    @Shadow
    private static void shadowVertex(MatrixStack.Entry pMatrixEntry, IVertexBuilder pBuffer, float pAlpha, float pX, float pY, float pZ, float pTexU, float pTexV) {}

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$inject_render_0(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof ProjectileEntity) && !(entity instanceof ExperienceOrbEntity)) {
            Direction gravityDirection = GravityAPI.getGravityDirection(entity);

            matrices.pushPose();
            RotationAnimation animation = GravityAPI.getRotationAnimation(entity);
            if (animation == null) {
                return;
            }
            long timeMs = entity.level.getGameTime() * 50 + (long) (tickDelta * 50);
            Quaternion gravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs).copy();
            gravityRotation.conj();
            matrices.mulPose(gravityRotation);
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V",
                    ordinal = 1
            )
    )
    private void gravitymod$inject_render_1(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof ProjectileEntity) && !(entity instanceof ExperienceOrbEntity)) {
            Direction gravityDirection = GravityAPI.getGravityDirection(entity);

            matrices.popPose();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$inject_render_2(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, IRenderTypeBuffer vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof ProjectileEntity) && !(entity instanceof ExperienceOrbEntity)) {
            Direction gravityDirection = GravityAPI.getGravityDirection(entity);
            if (gravityDirection == Direction.DOWN) return;

            matrices.mulPose(RotationUtil.getCameraRotationQuaternion(gravityDirection));
        }
    }

    @Inject(
            method = "renderShadow",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void inject_renderShadow(MatrixStack matrices, IRenderTypeBuffer vertexConsumers, Entity entity, float opacity, float tickDelta, IWorldReader world, float radius, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();

        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
        Vector3d minShadowPos = RotationUtil.vecPlayerToWorld(-radius, -radius, (double) -radius, gravityDirection).add(x, y, z);
        Vector3d maxShadowPos = RotationUtil.vecPlayerToWorld(radius, 0.0D, radius, gravityDirection).add(x, y, z);
        MatrixStack.Entry entry = matrices.last();
        IVertexBuilder vertexConsumer = vertexConsumers.getBuffer(SHADOW_RENDER_TYPE);

        for (BlockPos blockPos : BlockPos.betweenClosed(BlockPosUtil.containing(minShadowPos), BlockPosUtil.containing(maxShadowPos))) {
            gravitymod$$renderShadowPartPlayer(entry, vertexConsumer, world, blockPos, x, y, z, radius, opacity, gravityDirection);
        }
    }

    @Unique
    private static void gravitymod$$renderShadowPartPlayer(MatrixStack.Entry entry, IVertexBuilder vertices, IWorldReader world, BlockPos pos, double x, double y, double z, float radius, float opacity, Direction gravityDirection) {
        BlockPos posBelow = pos.relative(gravityDirection);
        BlockState blockStateBelow = world.getBlockState(posBelow);
        if (blockStateBelow.getRenderShape() != BlockRenderType.INVISIBLE && world.getMaxLocalRawBrightness(pos) > 3) {
            if (blockStateBelow.isCollisionShapeFullBlock(world, posBelow)) {
                VoxelShape voxelShape = blockStateBelow.getShape(world, posBelow);
                if (!voxelShape.isEmpty()) {
                    Vector3d playerPos = RotationUtil.vecWorldToPlayer(x, y, z, gravityDirection);
                    float alpha = (float)(((double)opacity - (y - (double)pos.getY()) / 2.0D) * 0.5D * (double)world.getBrightness(pos));
                    if (alpha >= 0.0F) {
                        if (alpha > 1.0F) {
                            alpha = 1.0F;
                        }

                        Vector3d centerPos = Vector3d.atCenterOf(pos);
                        Vector3d playerCenterPos = RotationUtil.vecWorldToPlayer(centerPos, gravityDirection);

                        Vector3d playerRelNN = playerCenterPos.add(-0.5D, -0.5D, -0.5D).subtract(playerPos);
                        Vector3d playerRelPP = playerCenterPos.add(0.5D, -0.5D, 0.5D).subtract(playerPos);

                        Vector3d relNN = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(-0.5D, -0.5D, -0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);
                        Vector3d relNP = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(-0.5D, -0.5D, 0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);
                        Vector3d relPN = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(0.5D, -0.5D, -0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);
                        Vector3d relPP = RotationUtil.vecWorldToPlayer(centerPos.add(RotationUtil.vecPlayerToWorld(0.5D, -0.5D, 0.5D, gravityDirection)).subtract(x, y, z), gravityDirection);

                        float minU = -(float) playerRelNN.x / 2.0F / radius + 0.5F;
                        float maxU = -(float) playerRelPP.x / 2.0F / radius + 0.5F;
                        float minV = -(float) playerRelNN.z / 2.0F / radius + 0.5F;
                        float maxV = -(float) playerRelPP.z / 2.0F / radius + 0.5F;

                        shadowVertex(entry, vertices, alpha, (float) relNN.x, (float) relNN.y, (float) relNN.z, minU, minV);
                        shadowVertex(entry, vertices, alpha, (float) relNP.x, (float) relNP.y, (float) relNP.z, minU, maxV);
                        shadowVertex(entry, vertices, alpha, (float) relPP.x, (float) relPP.y, (float) relPP.z, maxU, maxV);
                        shadowVertex(entry, vertices, alpha, (float) relPN.x, (float) relPN.y, (float) relPN.z, maxU, minV);
                    }
                }
            }
        }
    }

    @ModifyVariable(
            method = "renderBox",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;renderLineBox(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/util/math/AxisAlignedBB;FFFF)V"
            ),
            ordinal = 0)
    private AxisAlignedBB gravitymod$renderHitboxAAB(AxisAlignedBB box, MatrixStack p_229094_1_, IVertexBuilder p_229094_2_, Entity entity, float p_229094_4_, float p_229094_5_, float p_229094_6_) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return box;
        }

        return RotationUtil.boxPlayerToWorld(box, gravityDirection);
    }

    @ModifyVariable(
            method = "renderHitbox",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private Vector3d gravitymod$renderHitboxViewVec(Vector3d viewVector, MatrixStack matrices, IVertexBuilder vertices, Entity entity, float tickDelta) {
        Direction gravityDirection = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return viewVector;
        }

        return RotationUtil.vecWorldToPlayer(viewVector, gravityDirection);
    }
}
