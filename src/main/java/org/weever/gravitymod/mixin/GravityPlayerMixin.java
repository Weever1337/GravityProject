package org.weever.gravitymod.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import org.weever.gravitymod.v1_20_1.util.Mth;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
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


@SuppressWarnings("resource")
@Mixin(value = PlayerEntity.class, priority = 1001)
public abstract class GravityPlayerMixin extends LivingEntity {
    @Shadow
    @Final
    public PlayerAbilities abilities;

    @Shadow
    public abstract EntitySize getDimensions(Pose pose);

    @Shadow
    protected abstract boolean isStayingOnGroundSurface();

    @Shadow
    protected abstract boolean isAboveGround();

    @Shadow public abstract void checkMovementStatistics(double d, double e, double f);

    protected GravityPlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    public PlayerEntity gravitymod$this(){
        return ((PlayerEntity)(Object)this);
    }

    @Inject(
            method = "drop(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/ItemEntity;",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$dropGravity(
            ItemStack $$0, boolean $$1, boolean $$2, CallbackInfoReturnable<ItemEntity> cir
    ) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN)
            return;

        if ($$0.isEmpty()) {
            cir.setReturnValue(null);
        } else {
            if (this.level.isClientSide) {
                this.swing(Hand.MAIN_HAND);
            }

            double $$3 = this.getEyeY() - 0.3F;

            Vector3d Vector3dd = this.getEyePosition(1.0F)
                    .subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.3D, 0.0D, gravityDirection));

            ItemEntity $$4 = new ItemEntity(this.level, Vector3dd.x, Vector3dd.y, Vector3dd.z, $$0);
            $$4.setPickUpDelay(40);
            if ($$2) {
                $$4.setThrower(this.getUUID());
            }

            if ($$1) {
                float $$5 = this.random.nextFloat() * 0.5F;
                float $$6 = this.random.nextFloat() * (float) (Math.PI * 2);

                Vector3d world = RotationUtil.vecPlayerToWorld((double)(-Mth.sin($$6) * $$5), 0.2F, (double)(Mth.cos($$6) * $$5), gravityDirection);
                GravityAPI.setWorldVelocity($$4, world);
            } else {
                float $$7 = 0.3F;
                float $$8 = Mth.sin(this.xRot * (float) (Math.PI / 180.0));
                float $$9 = Mth.cos(this.xRot * (float) (Math.PI / 180.0));
                float $$10 = Mth.sin(this.yRot * (float) (Math.PI / 180.0));
                float $$11 = Mth.cos(this.yRot * (float) (Math.PI / 180.0));
                float $$12 = this.random.nextFloat() * (float) (Math.PI * 2);
                float $$13 = 0.02F * this.random.nextFloat();
                Vector3d world = RotationUtil.vecPlayerToWorld((double)(-$$10 * $$9 * 0.3F) + Math.cos((double)$$12) * (double)$$13,
                        (double)(-$$8 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                        (double)($$11 * $$9 * 0.3F) + Math.sin((double)$$12) * (double)$$13, gravityDirection);
                GravityAPI.setWorldVelocity($$4, world);
            }

            cir.setReturnValue($$4);
        }
    }

    @Inject(
            method = "maybeBackOffFromEdge",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$inject_adjustMovementForSneaking(Vector3d movement, MoverType type, CallbackInfoReturnable<Vector3d> cir) {
        Entity this_ = (Entity) (Object) this;
        Direction gravityDirection = GravityAPI.getGravityDirection(this_);
        if (gravityDirection == Direction.DOWN) return;

        Vector3d playerMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);

        if (!this.abilities.flying && (type == MoverType.SELF || type == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround()) {
            double d = playerMovement.x;
            double e = playerMovement.z;
            double var7 = 0.05D;

            while (d != 0.0D && this_.level.noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(d, (double) (-this.maxUpStep), 0.0D, gravityDirection)))) {
                if (d < 0.05D && d >= -0.05D) {
                    d = 0.0D;
                }
                else if (d > 0.0D) {
                    d -= 0.05D;
                }
                else {
                    d += 0.05D;
                }
            }

            while (e != 0.0D && this_.level.noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(0.0D, (double) (-this.maxUpStep), e, gravityDirection)))) {
                if (e < 0.05D && e >= -0.05D) {
                    e = 0.0D;
                }
                else if (e > 0.0D) {
                    e -= 0.05D;
                }
                else {
                    e += 0.05D;
                }
            }

            while (d != 0.0D && e != 0.0D && this_.level.noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(d, (double) (-this.maxUpStep), e, gravityDirection)))) {
                if (d < 0.05D && d >= -0.05D) {
                    d = 0.0D;
                }
                else if (d > 0.0D) {
                    d -= 0.05D;
                }
                else {
                    d += 0.05D;
                }

                if (e < 0.05D && e >= -0.05D) {
                    e = 0.0D;
                }
                else if (e > 0.0D) {
                    e -= 0.05D;
                }
                else {
                    e += 0.05D;
                }
            }

            cir.setReturnValue(RotationUtil.vecPlayerToWorld(d, playerMovement.y, e, gravityDirection));
        }
        else {
            cir.setReturnValue(movement);
        }
    }

    @Inject(
            method = "isAboveGround",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$isAboveGround(CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN)
            return;


        Vector3d world = RotationUtil.vecPlayerToWorld(0.0, (double)(this.fallDistance - this.maxUpStep), 0.0, gravityDirection);

        cir.setReturnValue(this.isOnGround()
                || this.fallDistance < this.maxUpStep
                && !this.level.noCollision(this, this.getBoundingBox().move(world.x, world.y, world.z)));
    }

    @Unique
    private float gravitymod$tempStoreYRot = 0;
    @Unique
    private boolean gravitymod$tempStoreYRotShifted = false;

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"
            )
    )
    private void gravitymod$attackY1(Entity target, CallbackInfo ci) {
        Direction targetGravityDirection = GravityAPI.getGravityDirection(target);
        Direction attackerGravityDirection = GravityAPI.getGravityDirection(this);
        if (targetGravityDirection == attackerGravityDirection)
            return;

        gravitymod$tempStoreYRot = yRot;
        gravitymod$tempStoreYRotShifted = true;
        yRot = (RotationUtil.rotWorldToPlayer(RotationUtil.rotPlayerToWorld(yRot, xRot, attackerGravityDirection), targetGravityDirection).x);
    }
    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V",
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$attackY2(Entity target, CallbackInfo ci) {
        if (gravitymod$tempStoreYRotShifted){
            gravitymod$tempStoreYRotShifted = false;
            yRot = (gravitymod$tempStoreYRot);
        }
    }


    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;push(DDD)V"
            )
    )
    private void gravitymod$attackY1push(Entity target, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN)
            return;

        gravitymod$tempStoreYRot = yRot;
        gravitymod$tempStoreYRotShifted = true;
        yRot = (RotationUtil.rotPlayerToWorld(yRot, xRot, gravityDirection).x);
    }
    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;push(DDD)V",
                    shift = At.Shift.AFTER
            )
    )
    private void gravitymod$attackY2push(Entity target, CallbackInfo ci) {
        if (gravitymod$tempStoreYRotShifted){
            gravitymod$tempStoreYRotShifted = false;
            yRot = (gravitymod$tempStoreYRot);
        }
    }

    @Inject(
            method = "getRopeHoldPosition",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$getRopeHoldPosition(float partialTicks, CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) return;

        double armSide = 0.22D * (this.getMainArm() == HandSide.RIGHT ? -1.0D : 1.0D);
        float pitchRad = Mth.lerp(partialTicks * 0.5F, this.xRot, this.xRotO) * ((float) Math.PI / 180F);
        float bodyYawRad = Mth.lerp(partialTicks, this.yBodyRotO, this.yBodyRot) * ((float) Math.PI / 180F);
        if (this.isFallFlying() || this.isAutoSpinAttack()) {
            Vector3d localView = RotationUtil.vecWorldToPlayer(this.getViewVector(partialTicks), gravityDirection);
            Vector3d velocity = this.getDeltaMovement();
            double horizVelSqr = velocity.x * velocity.x + velocity.z * velocity.z;
            double horizViewSqr = localView.x * localView.x + localView.z * localView.z;
            float roll;
            if (horizVelSqr > 0.0D && horizViewSqr > 0.0D) {
                double dot = (velocity.x * localView.x + velocity.z * localView.z) / Math.sqrt(horizVelSqr * horizViewSqr);
                double cross = velocity.x * localView.z - velocity.z * localView.x;
                roll = (float) (Math.signum(cross) * Math.acos(dot));
            } else {
                roll = 0.0F;
            }

            Vector3d localOffset = new Vector3d(armSide, -0.11D, 0.85D).zRot(-roll).xRot(-pitchRad).yRot(-bodyYawRad);
            cir.setReturnValue(this.getEyePosition(partialTicks)
                    .add(RotationUtil.vecPlayerToWorld(localOffset, gravityDirection)));
        } else if (this.isVisuallySwimming()) {
            Vector3d localOffset = new Vector3d(armSide, 0.2D, -0.15D).xRot(-pitchRad).yRot(-bodyYawRad);
            cir.setReturnValue(this.getEyePosition(partialTicks)
                    .add(RotationUtil.vecPlayerToWorld(localOffset, gravityDirection)));
        } else {
            double height = this.getBbHeight() - 1.0D;
            double forward = this.isCrouching() ? -0.2D : 0.07D;
            Vector3d localOffset = new Vector3d(armSide, height, forward).yRot(-bodyYawRad);
            cir.setReturnValue(this.getPosition(partialTicks)
                    .add(RotationUtil.vecPlayerToWorld(localOffset, gravityDirection)));
        }
    }

    @Inject(
            method = "addParticlesAroundSelf",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$addParticlesAroundSelf(IParticleData $$0, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();
        for (int $$1 = 0; $$1 < 5; $$1++) {
            double $$2 = this.random.nextGaussian() * 0.02;
            double $$3 = this.random.nextGaussian() * 0.02;
            double $$4 = this.random.nextGaussian() * 0.02;
            Vector3d Vector3dd = RotationUtil.maskPlayerToWorld(this.getRandomX(1.0),
                    this.getRandomY() + 1.0,
                    this.getRandomZ(1.0),
                    gravityDirection);
            this.level.addParticle($$0, Vector3dd.x,Vector3dd.y,Vector3dd.z, $$2, $$3, $$4);
        }
    }
}
