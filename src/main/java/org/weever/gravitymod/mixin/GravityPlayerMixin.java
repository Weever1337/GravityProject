package org.weever.gravitymod.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
            method = "travel",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$gravityTravel(Vector3d $$0, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(gravitymod$this());
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();

        double $$1 = this.getX();
        double $$2 = this.getY();
        double $$3 = this.getZ();
        if (this.isSwimming() && !this.isPassenger()) {
            double $$4 = RotationUtil.vecWorldToPlayer(this.getLookAngle(), gravityDirection).y;
            double $$5 = $$4 < -0.2 ? 0.085 : 0.06;
            Vector3d rotate = new Vector3d(0.0D, 1.0D - 0.1D, 0.0D);
            rotate = RotationUtil.vecPlayerToWorld(rotate, GravityAPI.getGravityDirection(this));
            if ($$4 <= 0.0
                    || this.jumping
                    || !this.level.getBlockState(BlockPosUtil.containing(
                    (double) this.getX() - rotate.x,
                    (double) (this.getY() + 1.0 - 0.1) - rotate.y + (1.0D - 0.1D),
                    (double) this.getZ() - rotate.z)
            ).getFluidState().isEmpty()) {
                Vector3d $$6 = this.getDeltaMovement();
                this.setDeltaMovement($$6.add(0.0, ($$4 - $$6.y) * $$5, 0.0));
            }
        }

        if (this.abilities.flying && !this.isPassenger()) {
            double $$7 = this.getDeltaMovement().y;
            super.travel($$0);
            Vector3d $$8 = this.getDeltaMovement();
            this.setDeltaMovement($$8.x, $$7 * 0.6, $$8.z);
            this.fallDistance = 0.0F;
            this.setSharedFlag(7, false);
        } else {
            super.travel($$0);
        }

        this.checkMovementStatistics(this.getX() - $$1, this.getY() - $$2, this.getZ() - $$3);

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
