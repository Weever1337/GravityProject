package org.weever.gravitymod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.access.IGravityLivingEntity;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.BlockPosUtil;
import org.weever.gravitymod.v1_20_1.util.Mth;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

@Mixin(value = LivingEntity.class)
public abstract class GravityLivingEntityMixin extends Entity implements IGravityLivingEntity {
    @Unique
    @Override
    public void gravitymod$augmentKB(Entity knockback) {
        gravitymod$knockbackGravityAugmentation = true;
        gravitymod$augmentSource = knockback;
    }

    public GravityLivingEntityMixin(EntityType<?> $$0, World $$1) {
        super($$0, $$1);
    }

    @Shadow
    public abstract void readAdditionalSaveData(CompoundNBT nbt);

    @Shadow
    public abstract EntitySize getDimensions(Pose pose);

    @Shadow
    public abstract float getViewYRot(float tickDelta);

    @Shadow
    public abstract boolean hasEffect(Effect mobEffect);

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract float getSpeed();

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract Vector3d getFluidFallingAdjustedMovement(double d, boolean bl, Vector3d Vector3d);

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    protected abstract SoundEvent getFallDamageSound(int i);

    @Shadow
    public abstract Vector3d handleRelativeFrictionAndCalculateMovement(Vector3d Vector3d, float f);

    @Shadow
    @Nullable
    public abstract EffectInstance getEffect(Effect mobEffect);

    @Shadow
    public abstract boolean isBlocking();

    @Shadow
    @Final
    private Map<Effect, EffectInstance> activeEffects;

    @Shadow
    protected abstract void onEffectRemoved(EffectInstance EffectInstance);

    @Shadow
    private boolean effectsDirty;

    @Shadow
    protected abstract void updateInvisibilityStatus();

    @Shadow
    @Final
    private static DataParameter<Integer> DATA_EFFECT_COLOR_ID;

    @Shadow
    @Final
    private static DataParameter<Boolean> DATA_EFFECT_AMBIENCE_ID;

    @Shadow
    protected boolean jumping;

    @Shadow
    public float animationSpeedOld;

    @Shadow
    public float animationSpeed;

    @Shadow
    public float animationPosition;

    @Shadow
    public abstract boolean canStandOnFluid(Fluid p_230285_1_);

    @Shadow
    private DamageSource lastDamageSource;

    @Shadow
    public abstract void calculateEntityAnimation(LivingEntity p_233629_1_, boolean p_233629_2_);

    @Shadow
    public abstract void knockback(float p_233627_1_, double p_233627_2_, double p_233627_4_);

    @Shadow
    protected abstract void onEffectUpdated(EffectInstance p_70695_1_, boolean p_70695_2_);

    @Shadow
    public abstract void travel(Vector3d pTravelVector);
    public LivingEntity gravitymod$this() {
        return ((LivingEntity) (Object) this);
    }

    /**
     * Fixed walking animation, base mod's was bugged
     */
    @Inject(
            method = "calculateEntityAnimation",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void gravitymod$calculateEntityAnimation(LivingEntity pLivingEntity, boolean pIsFlying, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(gravitymod$this());
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();

        Vector3d myPos = RotationUtil.vecPlayerToWorld(this.getX(), this.getY(), this.getZ(), gravityDirection);

        Vector3d myPoso = RotationUtil.vecPlayerToWorld(xo, yo, zo, gravityDirection);

        float $$1 = (float) Mth.length(myPos.x - myPoso.x, pIsFlying ? myPos.y - myPoso.y : 0.0, myPos.z - myPoso.z);
//        this.updateWalkAnimation($$1);

        float f = Math.min($$1 * 4.0F, 1.0F);
        this.animationSpeedOld = animationSpeed;
        this.animationSpeed += (f - animationSpeed) * 0.4F;
        this.animationPosition += animationSpeed;
    }

    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void gravitymod$travelWithGravity(Vector3d $$0, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(gravitymod$this());
        if (gravityDirection != Direction.DOWN){
            $$0 = RotationUtil.vecPlayerToWorld($$0, gravityDirection);
        }
    }


    @Inject(
            method = "playBlockFallSound",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$modify_playBlockFallSound_getBlockState_0(CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();
        if (!this.isSilent()) {
            BlockPos flipPos = BlockPosUtil.containing(this.position().add(RotationUtil.vecPlayerToWorld(0, -0.20000000298023224D, 0, gravityDirection)));
            BlockState $$3 = this.level.getBlockState(new BlockPos(flipPos.getX(), flipPos.getY(), flipPos.getZ()));
            if (!$$3.isAir()) {
                SoundType $$4 = $$3.getSoundType();
                this.playSound($$4.getFallSound(), $$4.getVolume() * 0.5F, $$4.getPitch() * 0.75F);
            }
        }
    }

    @Inject(
            method = "canSee",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/util/math/vector/Vector3d;",
                    ordinal = 0
            ),
            cancellable = true)
    private void gravitymod$redirect_canSee_new_0(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);

        Direction gravityDirectioEent = GravityAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN && gravityDirectioEent == Direction.DOWN)
            return;
        if (entity.level != this.level) {
            cir.setReturnValue(false);
        } else {
            Vector3d $$1 = this.getEyePosition(1.0F);
            Vector3d $$2 = entity.getEyePosition(1.0F);
            cir.setReturnValue($$2.distanceTo($$1) > 128.0D * 128.0D
                    ? false
                    : this.level.clip(new RayTraceContext($$1, $$2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)).getType() == RayTraceResult.Type.MISS);
        }
    }


    @Inject(
            method = "getLocalBoundsForPose",
            at = @At("RETURN"),
            cancellable = true
    )
    private void gravitymod$inject_getBoundingBox(Pose pose, CallbackInfoReturnable<AxisAlignedBB> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB box = cir.getReturnValue();
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0.0D, -1.0E-6D, 0.0D);
        }
        cir.setReturnValue(RotationUtil.boxWorldToPlayer(box, gravityDirection));
    }

    /**
     * Modifies the gravity influence
     */
    @ModifyVariable(method = "tick", at = @At(value = "STORE"), ordinal = 0)
    private double gravitymod$tickGravity(double $$1) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return $$1;

        return RotationUtil.vecWorldToPlayer(this.getX() - xo, this.getY() - this.yo, this.getZ() - this.zo, gravityDirection).x;
    }

    @ModifyVariable(method = "tick", at = @At(value = "STORE"), ordinal = 1)
    private double gravitymod$tickGravity2(double $$1) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return $$1;

        return RotationUtil.vecWorldToPlayer(getX() - xo, getY() - yo, getZ() - zo, gravityDirection).z;
    }


    @Unique
    public boolean gravitymod$knockbackGravityAugmentation = false;
    @Unique
    public Entity gravitymod$augmentSource = null;


    @Inject(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"
            )
    )
    private void gravitymod$augmentHurt(DamageSource $$0, float $$1, CallbackInfoReturnable<Boolean> cir) {
        gravitymod$knockbackGravityAugmentation = true;
        gravitymod$augmentSource = $$0.getEntity();
    }

    @Inject(
            method = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$augmentHurtKnockaback(float $$0, double $$1, double $$2, CallbackInfo ci) {
        if (gravitymod$knockbackGravityAugmentation) {
            gravitymod$knockbackGravityAugmentation = false;
            if (gravitymod$augmentSource != null) {
                Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
                if (gravityDirection == Direction.DOWN)
                    return;
                double $$13 = gravitymod$redirect_damage_getX_0(gravitymod$augmentSource) -
                        gravitymod$redirect_damage_getX_1(gravitymod$this());

                double $$14;
                for ($$14 = gravitymod$redirect_damage_getZ_0(gravitymod$augmentSource) -
                        gravitymod$redirect_damage_getZ_1(gravitymod$this()); $$13 * $$13 + $$14 * $$14 < 1.0E-4; $$14 = (Math.random() - Math.random()) * 0.01) {
                    $$13 = (Math.random() - Math.random()) * 0.01;
                }
                knockback(0.4F, $$13, $$14);
                ci.cancel();
            }
        }
    }

    @Unique
    private double gravitymod$redirect_damage_getX_0(Entity attacker) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getX();
            } else {
                return attacker.getEyePosition(1.0F).x;
            }
        }

        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(1.0F), gravityDirection).x;
    }

    @Unique
    private double gravitymod$redirect_damage_getZ_0(Entity attacker) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getZ();
            } else {
                return attacker.getEyePosition(1.0F).z;
            }
        }

        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(1.0F), gravityDirection).z;
    }


    @Unique
    private double gravitymod$redirect_damage_getX_1(LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getX();
        }

        return RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).x;
    }

    @Unique
    private double gravitymod$redirect_damage_getZ_1(LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            return target.getZ();
        }

        return RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).z;
    }

    @Inject(
            method = "blockedByShield",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void gravitymod$blocked(LivingEntity target, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        Direction gravityDirection2 = GravityAPI.getGravityDirection(gravitymod$this());
        if (gravityDirection == Direction.DOWN && gravityDirection2 == Direction.DOWN)
            return;
        ci.cancel();

        target.knockback(0.5F, RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).x
                        - gravitymod$redirect_knockback_getX_1(gravitymod$this(), target),
                RotationUtil.vecWorldToPlayer(target.position(), gravityDirection).z
                        - gravitymod$redirect_knockback_getZ_1(gravitymod$this(), target));
        return;
    }

    @Unique
    private double gravitymod$redirect_knockback_getX_1(LivingEntity attacker, LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getX();
            } else {
                return attacker.getEyePosition(1.0F).x;
            }
        }

        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(1.0F), gravityDirection).x;
    }

    @Unique
    private double gravitymod$redirect_knockback_getZ_1(LivingEntity attacker, LivingEntity target) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            if (GravityAPI.getGravityDirection(attacker) == Direction.DOWN) {
                return attacker.getZ();
            } else {
                return attacker.getEyePosition(1.0F).z;
            }
        }

        return RotationUtil.vecWorldToPlayer(attacker.getEyePosition(1.0F), gravityDirection).z;
    }

    @Inject(
            method = "spawnItemParticles",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void gravitymod$spawnItemParticles(ItemStack $$0, int $$1, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();
        for (int $$2 = 0; $$2 < $$1; $$2++) {
            Vector3d $$3 = new Vector3d(((double) this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            $$3 = $$3.xRot(-this.xRot * (float) (Math.PI / 180.0));
            $$3 = RotationUtil.vecPlayerToWorld($$3.yRot(-this.yRot * (float) (Math.PI / 180.0)), gravityDirection);
            double $$4 = (double) (-this.random.nextFloat()) * 0.6 - 0.3;
            Vector3d $$5 = new Vector3d(((double) this.random.nextFloat() - 0.5) * 0.3, $$4, 0.6);
            $$5 = $$5.xRot(-this.xRot * (float) (Math.PI / 180.0));
            $$5 = $$5.yRot(-this.yRot * (float) (Math.PI / 180.0));
            Vector3d rotated = RotationUtil.vecPlayerToWorld($$5, gravityDirection);
            $$5 = this.getEyePosition(1.0F).add(rotated.x, rotated.y, rotated.z);
            this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, $$0), $$5.x, $$5.y, $$5.z, $$3.x, $$3.y + 0.05, $$3.z);
        }
    }

    @Inject(
            method = "tickEffects",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$tickEffects(CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        ci.cancel();
        Iterator<Effect> $$0 = this.activeEffects.keySet().iterator();

        try {
            while ($$0.hasNext()) {
                Effect $$1 = $$0.next();
                EffectInstance $$2 = this.activeEffects.get($$1);
                if (!$$2.tick(gravitymod$this(), () -> this.onEffectUpdated($$2, true))) {
                    if (!this.level.isClientSide) {
                        $$0.remove();
                        this.onEffectRemoved($$2);
                    }
                } else if ($$2.getDuration() % 600 == 0) {
                    this.onEffectUpdated($$2, false);
                }
            }
        } catch (ConcurrentModificationException var11) {
        }

        if (this.effectsDirty) {
            if (!this.level.isClientSide) {
                this.updateInvisibilityStatus();
            }

            this.effectsDirty = false;
        }

        int $$3 = this.entityData.get(DATA_EFFECT_COLOR_ID);
        boolean $$4 = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
        if ($$3 > 0) {
            boolean $$5;
            if (this.isInvisible()) {
                $$5 = this.random.nextInt(15) == 0;
            } else {
                $$5 = this.random.nextBoolean();
            }

            if ($$4) {
                $$5 &= this.random.nextInt(5) == 0;
            }

            if ($$5 && $$3 > 0) {
                double $$7 = (double) ($$3 >> 16 & 0xFF) / 255.0;
                double $$8 = (double) ($$3 >> 8 & 0xFF) / 255.0;
                double $$9 = (double) ($$3 >> 0 & 0xFF) / 255.0;
                Vector3d Vector3dd = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(this.getRandomX(0.5),
                        this.getRandomY(),
                        this.getRandomZ(0.5)), gravityDirection));
                this.level
                        .addParticle(
                                $$4 ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT,
                                Vector3dd.x, Vector3dd.y, Vector3dd.z,
                                $$7,
                                $$8,
                                $$9
                        );
            }
        }
    }


//    @Inject(
//            method = "makePoofParticles",
//            at = @At(
//                    value = "HEAD"
//            ),
//            cancellable = true
//    )
//    private void gravitymod$modify_addDeathParticless_addParticle_0(CallbackInfo ci) {
//        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
//        if (gravityDirection == Direction.DOWN)
//            return;
//        for (int $$0 = 0; $$0 < 20; $$0++) {
//            double $$1 = this.random.nextGaussian() * 0.02;
//            double $$2 = this.random.nextGaussian() * 0.02;
//            double $$3 = this.random.nextGaussian() * 0.02;
//            Vector3d Vector3dd = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0)), gravityDirection));
//
//            this.level().addParticle(ParticleTypes.POOF, Vector3dd.x, Vector3dd.y, Vector3dd.z, $$1, $$2, $$3);
//        }
//        ci.cancel();
//    }

    @Inject(
            method = "isDamageSourceBlocked",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$modify_blockedByShield_Vector3dd_1(DamageSource $$0, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN)
            return;
        Entity $$1 = $$0.getDirectEntity();
        boolean $$2 = false;
        if ($$1 instanceof AbstractArrowEntity && ((AbstractArrowEntity) $$1).getPierceLevel() > 0) {
            $$2 = true;
        }

        if (!$$0.isBypassArmor() && this.isBlocking() && !$$2) {
            Vector3d $$4 = $$0.getSourcePosition();
            if ($$4 != null) {
                Vector3d $$5 = RotationUtil.vecWorldToPlayer(this.getViewVector(1.0F), gravityDirection);
                Vector3d $$6 = $$4.vectorTo(this.position()).normalize();
                $$6 = new Vector3d($$6.x, 0.0, $$6.z);
                if ($$6.dot($$5) < 0.0) {
                    cir.setReturnValue(true);
                }
            }
        }

        cir.setReturnValue(false);

        return;
    }


    @ModifyVariable(method = "calculateFallDamage(FF)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float gravitymod$diminishFallDamage(float value) {
        return value * (float) Math.sqrt(GravityAPI.getGravityStrength(this));
    }

}
