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
     * @reason Remove the vanilla overlay block detection and replace it with a custom one that affects gravity users. This version is clearer and more robust.
     */
    @Overwrite
    private static @Nullable Pair<BlockState, BlockPos> getOverlayBlock(PlayerEntity player) {
        if (!(player instanceof IGravityEntity)) {
            return null;
        }

        IGravityEntity gravityEntity = (IGravityEntity) player;
        GravityDirection gravity = gravityEntity.getGravityDirection();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        float horizontalSize = player.getBbWidth() * 0.8F;
        float verticalSize = 0.1F;

        Vector3d eyePos = player.getEyePosition(1.0F);

        for (int i = 0; i < 8; ++i) {
            float h_offset1 = ((i & 1) * 2 - 1) * 0.5F;
            float v_offset = (((i >> 1) & 1) * 2 - 1) * 0.5F;
            float h_offset2 = (((i >> 2) & 1) * 2 - 1) * 0.5F;

            double checkX = eyePos.x;
            double checkY = eyePos.y;
            double checkZ = eyePos.z;

            switch (gravity) {
                case EAST:
                case WEST:
                    checkX += v_offset * verticalSize;
                    checkY += h_offset1 * horizontalSize;
                    checkZ += h_offset2 * horizontalSize;
                    break;
                case UP:
                case DOWN:
                default:
                    checkX += h_offset1 * horizontalSize;
                    checkY += v_offset * verticalSize;
                    checkZ += h_offset2 * horizontalSize;
                    break;
            }

            mutablePos.set(MathHelper.floor(checkX), MathHelper.floor(checkY), MathHelper.floor(checkZ));
            BlockState state = player.level.getBlockState(mutablePos);

            if (state.getRenderShape() != BlockRenderType.INVISIBLE && state.isViewBlocking(player.level, mutablePos)) {
                return Pair.of(state, mutablePos.immutable());
            }
        }

        return null;
    }
}