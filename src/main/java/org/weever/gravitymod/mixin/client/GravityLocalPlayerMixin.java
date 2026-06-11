package org.weever.gravitymod.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MoverType;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.BlockPosUtil;
import org.weever.gravitymod.v1_20_1.util.Mth;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public abstract class GravityLocalPlayerMixin extends AbstractClientPlayerEntity {
    public GravityLocalPlayerMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract boolean suffocatesAt(BlockPos pos);

    @Shadow
    public MovementInput input;

    @Shadow
    private int autoJumpTime;

    @Shadow
    public abstract boolean isAutoJumpEnabled();

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

    @Unique
    private Vector3d gravitymod$preMoveWorldPos = Vector3d.ZERO;

    @Inject(
            method = "move",
            at = @At("HEAD")
    )
    private void gravitymod$capturePreMovePosition(MoverType type, Vector3d movement, CallbackInfo ci) {
        gravitymod$preMoveWorldPos = this.position();
    }

    @Inject(
            method = "updateAutoJump",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$inject_updateAutoJump(float dX, float dZ, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();
        Vector3d localDelta = RotationUtil.vecWorldToPlayer(
                this.position().subtract(gravitymod$preMoveWorldPos), gravityDirection);
        gravitymod$updateAutoJumpRotated((float) localDelta.x, (float) localDelta.z, gravityDirection);
    }

    @Unique
    private void gravitymod$updateAutoJumpRotated(float dX, float dZ, Direction gravityDirection) {
        boolean canAutoJump = this.isAutoJumpEnabled()
                && this.autoJumpTime <= 0
                && this.onGround
                && !this.isStayingOnGroundSurface()
                && !this.isPassenger()
                && gravitymod$isMovingByInput()
                && (double) this.getBlockJumpFactor() >= 1.0D;
        if (!canAutoJump) return;

        Vector3d localPos = RotationUtil.vecWorldToPlayer(this.position(), gravityDirection);
        Vector3d localDest = localPos.add(dX, 0.0D, dZ);
        Vector3d moveVec = new Vector3d(dX, 0.0D, dZ);
        float speed = this.getSpeed();
        float moveSqr = (float) moveVec.lengthSqr();
        if (moveSqr <= 0.001F) {
            Vector2f in = this.input.getMoveVector();
            float inputX = speed * in.x;
            float inputZ = speed * in.y;
            float sin = Mth.sin(this.yRot * 0.017453292F);
            float cos = Mth.cos(this.yRot * 0.017453292F);
            moveVec = new Vector3d(inputX * cos - inputZ * sin, moveVec.y, inputZ * cos + inputX * sin);
            moveSqr = (float) moveVec.lengthSqr();
            if (moveSqr <= 0.001F) return;
        }

        float invLen = (float) (1.0D / Math.sqrt(moveSqr));
        Vector3d moveDir = moveVec.scale(invLen);
        Vector3d localForward = RotationUtil.vecWorldToPlayer(this.getForward(), gravityDirection);
        float forwardDot = (float) (localForward.x * moveDir.x + localForward.z * moveDir.z);
        if (forwardDot < -0.15F) return;

        ISelectionContext context = ISelectionContext.of(this);
        Direction worldUp = RotationUtil.dirPlayerToWorld(Direction.UP, gravityDirection);
        BlockPos headPos = BlockPosUtil.containing(RotationUtil.vecPlayerToWorld(
                localPos.x, localPos.y + (double) this.getBbHeight(), localPos.z, gravityDirection));
        if (!this.level.getBlockState(headPos).getCollisionShape(this.level, headPos, context).isEmpty()) {
            return;
        }
        BlockPos aboveHeadPos = headPos.relative(worldUp);
        if (!this.level.getBlockState(aboveHeadPos).getCollisionShape(this.level, aboveHeadPos, context).isEmpty()) {
            return;
        }

        float maxJumpHeight = 1.2F;
        if (this.hasEffect(Effects.JUMP)) {
            maxJumpHeight += (float) (this.getEffect(Effects.JUMP).getAmplifier() + 1) * 0.75F;
        }

        float reach = Math.max(speed * 7.0F, 1.0F / invLen);
        Vector3d localFar = localDest.add(moveDir.scale(reach));
        float width = this.getBbWidth();
        float height = this.getBbHeight();
        AxisAlignedBB sweepLocal = new AxisAlignedBB(localPos, localFar.add(0.0D, height, 0.0D))
                .inflate(width, 0.0D, width);
        Vector3d rayFrom = localPos.add(0.0D, 0.51D, 0.0D);
        Vector3d rayTo = localFar.add(0.0D, 0.51D, 0.0D);
        Vector3d side = moveDir.cross(new Vector3d(0.0D, 1.0D, 0.0D));
        Vector3d sideOffset = side.scale(width * 0.5F);
        Vector3d rayFromLeft = rayFrom.subtract(sideOffset);
        Vector3d rayToLeft = rayTo.subtract(sideOffset);
        Vector3d rayFromRight = rayFrom.add(sideOffset);
        Vector3d rayToRight = rayTo.add(sideOffset);

        List<AxisAlignedBB> localObstacles = this.level
                .getCollisions(this, RotationUtil.boxPlayerToWorld(sweepLocal, gravityDirection), (e) -> true)
                .flatMap((shape) -> shape.toAabbs().stream())
                .map((worldBox) -> RotationUtil.boxWorldToPlayer(worldBox, gravityDirection))
                .collect(Collectors.toList());

        float obstacleTop = Float.MIN_VALUE;
        for (AxisAlignedBB obstacle : localObstacles) {
            if (obstacle.intersects(rayFromLeft, rayToLeft) || obstacle.intersects(rayFromRight, rayToRight)) {
                obstacleTop = (float) obstacle.maxY;
                BlockPos obstaclePos = BlockPosUtil.containing(
                        RotationUtil.vecPlayerToWorld(obstacle.getCenter(), gravityDirection));

                for (int i = 1; (float) i < maxJumpHeight; ++i) {
                    BlockPos checkPos = obstaclePos.relative(worldUp, i);
                    VoxelShape checkShape = this.level.getBlockState(checkPos).getCollisionShape(this.level, checkPos, context);
                    if (!checkShape.isEmpty()) {
                        AxisAlignedBB checkBox = checkShape.bounds()
                                .move(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                        obstacleTop = (float) RotationUtil.boxWorldToPlayer(checkBox, gravityDirection).maxY;
                        if ((double) obstacleTop - localPos.y > (double) maxJumpHeight) {
                            return;
                        }
                    }

                    if (i > 1) {
                        aboveHeadPos = aboveHeadPos.relative(worldUp);
                        if (!this.level.getBlockState(aboveHeadPos).getCollisionShape(this.level, aboveHeadPos, context).isEmpty()) {
                            return;
                        }
                    }
                }
                break;
            }
        }

        if (obstacleTop != Float.MIN_VALUE) {
            float rise = (float) ((double) obstacleTop - localPos.y);
            if (rise > 0.5F && rise <= maxJumpHeight) {
                this.autoJumpTime = 1;
            }
        }
    }

    @Unique
    private boolean gravitymod$isMovingByInput() {
        Vector2f moveVector = this.input.getMoveVector();
        return moveVector.x != 0.0F || moveVector.y != 0.0F;
    }
}