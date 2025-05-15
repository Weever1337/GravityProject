package org.weever.gravitymod.mixin.client;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;

import javax.annotation.Nullable;

@Mixin(OverlayRenderer.class)
public abstract class OverlayRendererMixin {
    /**
     * @author Weever1337
     * @reason Remove the vanilla overlay block detection and replace it with a custom one that affects gravity users.
     */
    @Overwrite
    private static @Nullable Pair<BlockState, BlockPos> getOverlayBlock(PlayerEntity player) {
        if (!(player instanceof IGravityEntity)) {
            return null;
        }

        IGravityEntity gravityEntity = (IGravityEntity) player;
        GravityDirection gravity = gravityEntity.getGravityDirection();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        float width = player.getBbWidth() * 0.8F;
        Vector3d eyePos = player.getEyePosition(1.0F);

        for (int i = 0; i < 8; ++i) {
            float xFactor = (i % 2) - 0.5F;
            float yFactor = ((i >> 1) % 2) - 0.5F;
            float zFactor = ((i >> 2) % 2) - 0.5F;

            double x = eyePos.x;
            double y = eyePos.y;
            double z = eyePos.z;

            switch (gravity) {
                case EAST:
                    x += yFactor * 0.1F;
                    y += xFactor * width;
                    z += zFactor * width;
                    break;
                case WEST:
                    x -= yFactor * 0.1F;
                    y += xFactor * width;
                    z += zFactor * width;
                    break;
                case UP:
                    x += xFactor * width;
                    y += zFactor * width;
                    z -= yFactor * 0.1F;
                    break;
                default:
                    x += xFactor * width;
                    y += yFactor * 0.1F;
                    z += zFactor * width;
                    break;
            }

            mutablePos.set(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
            BlockState state = player.level.getBlockState(mutablePos);

            if (state.getRenderShape() != BlockRenderType.INVISIBLE &&
                    state.isViewBlocking(player.level, mutablePos) &&
                    isBlockActuallyCoveringPlayer(player, mutablePos, gravity)) {
                return Pair.of(state, mutablePos.immutable()); // TODO: Fix some problems with it. It happens randomly (for me now).
            }
        }

        return null;
    }

    private static boolean isBlockActuallyCoveringPlayer(PlayerEntity player, BlockPos pos, GravityDirection gravity) {
        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3d blockCenter = new Vector3d(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        );

        Vector3d direction = eyePos.subtract(blockCenter).normalize();

        Vector3d lookVec = player.getLookAngle();
        double dot = direction.dot(lookVec);

        return dot > 0;
    }
}