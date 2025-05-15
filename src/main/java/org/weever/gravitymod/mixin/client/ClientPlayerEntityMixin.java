package org.weever.gravitymod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;
import org.weever.gravitymod.mixin.PlayerEntityMixin;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntityMixin implements IGravityEntity {
    @Shadow
    public int sprintTime;
    @Shadow
    protected int sprintTriggerTime;

    @Shadow
    protected abstract void handleNetherPortalClient();

    @Shadow
    public MovementInput input;

    @Shadow
    protected abstract boolean hasEnoughImpulseToStartSprinting();

    @Shadow
    private boolean crouching;

    @Shadow
    public abstract boolean isMovingSlowly();

    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    private int autoJumpTime;

    @Shadow
    protected abstract void moveTowardsClosestSpace(double p_244389_1_, double p_244389_3_);

    @Shadow
    @Final
    public ClientPlayNetHandler connection;
    @Shadow
    private boolean wasFallFlying;
    @Shadow
    private int waterVisionTime;

    @Shadow
    protected abstract boolean isControlledCamera();

    @Shadow
    public abstract boolean isRidingJumpable();

    @Shadow
    private int jumpRidingTicks;
    @Shadow
    private float jumpRidingScale;

    @Shadow
    protected abstract void sendRidingJump();

    @Shadow
    public abstract float getJumpRidingScale();

    @Shadow
    public abstract void onUpdateAbilities();

    /**
     * @author Weever1337
     * @reason Fixes suffocation detection for gravity users.
     */
    @Overwrite
    private boolean suffocatesAt(BlockPos pos) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = pos.getX() + 1;
        float maxY = pos.getY() + 1;
        float maxZ = pos.getZ() + 1;

        switch (getGravityDirection()) {
            case EAST:
            case WEST:
                minY = (float) axisalignedbb.minY;
                maxY = (float) axisalignedbb.maxY;
                minZ = (float) axisalignedbb.minZ;
                maxZ = (float) axisalignedbb.maxZ;
                break;
            case UP:
            case DOWN:
            default:
                minX = (float) axisalignedbb.minX;
                maxX = (float) axisalignedbb.maxX;
                minZ = (float) axisalignedbb.minZ;
                maxZ = (float) axisalignedbb.maxZ;
                break;
        }
        AxisAlignedBB checkBB = (new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)).deflate(1.0E-7D);
        return !this.level.noBlockCollision((ClientPlayerEntity) (Object) this, checkBB, (blockstate, blockpos) -> blockstate.isSuffocating(this.level, blockpos));
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;setDeltaMovement(Lnet/minecraft/util/math/vector/Vector3d;)V", ordinal = 0))
    private void redirectFlyingMovement(ClientPlayerEntity player, Vector3d originalVelocity) {
        if (player.abilities.flying && this.isControlledCamera()) {
            int verticalInput = 0;
            if (this.input.shiftKeyDown) --verticalInput;
            if (this.input.jumping) ++verticalInput;

            if (verticalInput != 0) {
                GravityDirection gravityDirection = this.getGravityDirection();
                Vector3d currentVelocity = player.getDeltaMovement();
                double verticalThrust = (double) verticalInput * (double) player.abilities.getFlyingSpeed() * 3.0D;

                double finalX = currentVelocity.x;
                double finalY = currentVelocity.y;
                double finalZ = currentVelocity.z;

                switch (gravityDirection) {
                    case EAST:
                        finalX += verticalThrust;
                        break;
                    case WEST:
                        finalX -= verticalThrust;
                        break;
                    case UP:
                        finalY -= verticalThrust;
                        break;
                    case DOWN:
                    default:
                        finalY += verticalThrust;
                        break;
                }
                player.setDeltaMovement(finalX, finalY, finalZ);
                return;
            }
        }

        player.setDeltaMovement(originalVelocity);
    }
}