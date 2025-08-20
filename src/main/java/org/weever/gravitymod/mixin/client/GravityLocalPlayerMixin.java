package org.weever.gravitymod.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.BlockPosUtil;

@Mixin(ClientPlayerEntity.class)
public abstract class GravityLocalPlayerMixin extends AbstractClientPlayerEntity {
    public GravityLocalPlayerMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract boolean suffocatesAt(BlockPos pos);

    @Inject(
            method = "suffocatesAt",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$collision(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN)
            return;

        AxisAlignedBB $$1 = this.getBoundingBox();
        AxisAlignedBB playerBox = this.getBoundingBox();
        Vector3d playerMask = RotationUtil.maskPlayerToWorld(0.0D, 1.0D, 0.0D, gravityDirection);
        AxisAlignedBB posBox = new AxisAlignedBB(pos);
        Vector3d posMask = RotationUtil.maskPlayerToWorld(1.0D, 0.0D, 1.0D, gravityDirection);

        AxisAlignedBB $$2 = new AxisAlignedBB(
                playerMask.multiply(playerBox.minX, playerBox.minY, playerBox.minZ).add(posMask.multiply(posBox.minX, posBox.minY, posBox.minZ)),
                playerMask.multiply(playerBox.maxX, playerBox.maxY, playerBox.maxZ).add(posMask.multiply(posBox.maxX, posBox.maxY, posBox.maxZ))
        ).deflate(1.0E-7);
        cir.setReturnValue(!this.level.noBlockCollision(this, $$2, (p_243494_1_, p_243494_2_) -> p_243494_1_.isSuffocating(this.level, p_243494_2_)));
    }


    @Inject(
            method = "moveTowardsClosestSpace(DD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$inject_pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();

        Vector3d pos = RotationUtil.vecPlayerToWorld(x - this.getX(), 0.0D, z - this.getZ(), gravityDirection).add(this.position());
        BlockPos blockPos = BlockPosUtil.containing(pos);
        if (this.suffocatesAt(blockPos)) {
            double dx = pos.x - (double) blockPos.getX();
            double dy = pos.y - (double) blockPos.getY();
            double dz = pos.z - (double) blockPos.getZ();
            Direction direction = null;
            double minDistToEdge = Double.MAX_VALUE;

            Direction[] directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
            for (Direction playerDirection : directions) {
                Direction worldDirection = RotationUtil.dirPlayerToWorld(playerDirection, gravityDirection);

                double g = worldDirection.getAxis().choose(dx, dy, dz);
                double distToEdge = worldDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - g : g;
                if (distToEdge < minDistToEdge && !this.suffocatesAt(blockPos.relative(worldDirection))) {
                    minDistToEdge = distToEdge;
                    direction = playerDirection;
                }
            }

            if (direction != null) {
                Vector3d velocity = this.getDeltaMovement();
                if (direction.getAxis() == Direction.Axis.X) {
                    this.setDeltaMovement(0.1D * (double) direction.getStepX(), velocity.y, velocity.z);
                }
                else if (direction.getAxis() == Direction.Axis.Z) {
                    this.setDeltaMovement(velocity.x, velocity.y, 0.1D * (double) direction.getStepZ());
                }
            }
        }
    }
}