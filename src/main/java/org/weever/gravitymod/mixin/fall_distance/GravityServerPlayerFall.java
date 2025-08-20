package org.weever.gravitymod.mixin.fall_distance;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(ServerPlayerEntity.class)
public abstract class GravityServerPlayerFall extends PlayerEntity {
    public GravityServerPlayerFall(World $$0, BlockPos $$1, float $$2, GameProfile $$3) {
        super($$0, $$1, $$2, $$3);
    }

    // make sure fall distance is correct on server side of the player
    @Inject(
            method = "doCheckFallDamage",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$wrapCheckFallDamage(
            double $$1, boolean $$3, CallbackInfo ci
    ) {
        ServerPlayerEntity this_ = (ServerPlayerEntity) (Object) this;
        Direction gravityDirection = GravityAPI.getGravityDirection(this_);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();
        BlockPos blockpos = this.getOnPos();
        if (this.level.hasChunkAt(blockpos)) {
            BlockPos $$4 = this.getOnPos();
            Vector3d localVec = RotationUtil.vecWorldToPlayer(this_.position().x, $$1, this_.position().z, gravityDirection);
            super.checkFallDamage(localVec.y, $$3, this.level.getBlockState($$4), $$4);
        }
    }
}