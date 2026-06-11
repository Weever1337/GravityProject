package org.weever.gravitymod.mixin.client;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(OverlayRenderer.class)
public abstract class GravityScreenEffectRenderer {
    @Inject(
            method = "getOverlayBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gravitymod$inject_getOverlayBlock(PlayerEntity player, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) return;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3f multipliers = RotationUtil.vecPlayerToWorld(player.getBbWidth() * 0.8F, 0.1F, player.getBbWidth() * 0.8F, gravityDirection);
        for (int i = 0; i < 8; ++i) {
            double d = eyePos.x + (double) (((float) ((i >> 0) % 2) - 0.5F) * multipliers.x());
            double e = eyePos.y + (double) (((float) ((i >> 1) % 2) - 0.5F) * multipliers.y());
            double f = eyePos.z + (double) (((float) ((i >> 2) % 2) - 0.5F) * multipliers.z());
            mutable.set(d, e, f);
            BlockState blockState = player.level.getBlockState(mutable);
            if (blockState.getRenderShape() != BlockRenderType.INVISIBLE && blockState.isViewBlocking(player.level, mutable)) {
                cir.setReturnValue(Pair.of(blockState, mutable.immutable()));
                return;
            }
        }

        cir.setReturnValue(null);
    }
}