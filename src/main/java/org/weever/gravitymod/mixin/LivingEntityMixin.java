package org.weever.gravitymod.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements IGravityEntity {
    @Shadow
    @Final
    private static AttributeModifier SLOW_FALLING;
    @Shadow
    protected boolean jumping;

    @Shadow
    public abstract boolean isEffectiveAi();

    @Shadow
    public abstract @Nullable ModifiableAttributeInstance getAttribute(Attribute attribute);

    @Shadow
    public abstract boolean hasEffect(Effect effect);

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Shadow
    public abstract boolean canStandOnFluid(Fluid fluid);

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract float getSpeed();

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract Vector3d getFluidFallingAdjustedMovement(double gravity, boolean falling, Vector3d movement);

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    protected abstract SoundEvent getFallDamageSound(int height);

    @Shadow
    public abstract @Nullable EffectInstance getEffect(Effect effect);

    @Shadow
    public abstract void calculateEntityAnimation(LivingEntity entity, boolean flying);

    @Shadow
    protected abstract float getFrictionInfluencedSpeed(float speed);

    @Shadow
    protected abstract Vector3d handleOnClimbable(Vector3d movement);

    @Shadow
    public abstract boolean hurt(DamageSource source, float amount);

    @Shadow
    protected abstract float getJumpPower();

    @Shadow public float flyingSpeed;

    @Shadow public abstract boolean isSleeping();

    @Shadow protected abstract void goDownInWater();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public Vector3d handleRelativeFrictionAndCalculateMovement(Vector3d input, float speedMultiplier) {
        this.moveRelative(this.getFrictionInfluencedSpeed(speedMultiplier), input);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vector3d movement = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && this.onClimbable()) {
            movement = new Vector3d(movement.x, 0.2D, movement.z);
        }
        return movement;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void travel(Vector3d input) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            double gravityAcceleration;
            ModifiableAttributeInstance gravityAttr = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
            boolean falling = this.getDeltaMovement().y <= 0.0D;

            if (falling && this.hasEffect(Effects.SLOW_FALLING)) {
                if (!gravityAttr.hasModifier(SLOW_FALLING)) {
                    gravityAttr.addTransientModifier(SLOW_FALLING);
                }
                this.fallDistance = 0.0F;
            } else if (gravityAttr.hasModifier(SLOW_FALLING)) {
                gravityAttr.removeModifier(SLOW_FALLING);
            }
            gravityAcceleration = gravityAttr.getValue();

            FluidState fluidstate = this.level.getFluidState(this.blockPosition());
            if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType())) {
                double prevY = this.getY();
                float waterSlowdown = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float waterAcceleration = 0.02F;
                float depthStrider = (float) EnchantmentHelper.getDepthStrider((LivingEntity) (Object) this);

                if (depthStrider > 3.0F) depthStrider = 3.0F;
                if (!this.onGround) depthStrider *= 0.5F;

                if (depthStrider > 0.0F) {
                    waterSlowdown += (0.54600006F - waterSlowdown) * depthStrider / 3.0F;
                    waterAcceleration += (this.getSpeed() - waterAcceleration) * depthStrider / 3.0F;
                }
                if (this.hasEffect(Effects.DOLPHINS_GRACE)) {
                    waterSlowdown = 0.96F;
                }

                waterAcceleration *= (float) this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
                this.moveRelative(waterAcceleration, input);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vector3d movement = this.getDeltaMovement();

                if (this.horizontalCollision && this.onClimbable()) {
                    movement = new Vector3d(movement.x, 0.2D, movement.z);
                }

                this.setDeltaMovement(movement.multiply(waterSlowdown, 0.8F, waterSlowdown));
                Vector3d fluidMovement = this.getFluidFallingAdjustedMovement(gravityAcceleration, falling, this.getDeltaMovement());
                this.setDeltaMovement(fluidMovement);

                if (this.horizontalCollision && this.isFree(fluidMovement.x, fluidMovement.y + 0.6F - this.getY() + prevY, fluidMovement.z)) {
                    this.setDeltaMovement(fluidMovement.x, 0.3F, fluidMovement.z);
                }
            } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidstate.getType())) {
                double prevY = this.getY();
                this.moveRelative(0.02F, input);
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.8F, 0.5D));
                    Vector3d lavaMovement = this.getFluidFallingAdjustedMovement(gravityAcceleration, falling, this.getDeltaMovement());
                    this.setDeltaMovement(lavaMovement);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
                }

                if (!this.isNoGravity()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -gravityAcceleration / 4.0D, 0.0D));
                }

                Vector3d movement = this.getDeltaMovement();
                if (this.horizontalCollision && this.isFree(movement.x, movement.y + 0.6F - this.getY() + prevY, movement.z)) {
                    this.setDeltaMovement(movement.x, 0.3F, movement.z);
                }
            } else if (this.isFallFlying()) {
                Vector3d movement = this.getDeltaMovement();
                if (movement.y > -0.5D) {
                    this.fallDistance = 1.0F;
                }

                Vector3d lookAngle = this.getLookAngle();
                float pitch = this.xRot * ((float) Math.PI / 180F);
                double horizontalLook = Math.sqrt(lookAngle.x * lookAngle.x + lookAngle.z * lookAngle.z);
                double horizontalMovement = Math.sqrt(this.getHorizontalDistanceSquared(movement));
                double lookLength = lookAngle.length();
                float pitchCos = MathHelper.cos(pitch);
                pitchCos = (float) ((double) pitchCos * (double) pitchCos * Math.min(1.0D, lookLength / 0.4D));

                movement = this.getDeltaMovement().add(0.0D, gravityAcceleration * (-1.0D + (double) pitchCos * 0.75D), 0.0D);
                if (movement.y < 0.0D && horizontalLook > 0.0D) {
                    double lift = movement.y * -0.1D * (double) pitchCos;
                    movement = movement.add(lookAngle.x * lift / horizontalLook, lift, lookAngle.z * lift / horizontalLook);
                }

                if (pitch < 0.0F && horizontalLook > 0.0D) {
                    double thrust = horizontalMovement * (double) (-MathHelper.sin(pitch)) * 0.04D;
                    movement = movement.add(-lookAngle.x * thrust / horizontalLook, thrust * 3.2D, -lookAngle.z * thrust / horizontalLook);
                }

                if (horizontalLook > 0.0D) {
                    movement = movement.add(
                            (lookAngle.x / horizontalLook * horizontalMovement - movement.x) * 0.1D,
                            0.0D,
                            (lookAngle.z / horizontalLook * horizontalMovement - movement.z) * 0.1D
                    );
                }

                this.setDeltaMovement(movement.multiply(0.99F, 0.98F, 0.99F));
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.horizontalCollision && !this.level.isClientSide) {
                    double deltaMovement = Math.sqrt(getHorizontalDistanceSquared(this.getDeltaMovement()));
                    double collisionForce = horizontalMovement - deltaMovement;
                    float damage = (float) (collisionForce * 10.0D - 3.0D);

                    if (damage > 0.0F) {
                        this.playSound(this.getFallDamageSound((int) damage), 1.0F, 1.0F);
                        this.hurt(DamageSource.FLY_INTO_WALL, damage);
                    }
                }

                if (this.onGround && !this.level.isClientSide) {
                    this.setSharedFlag(7, false);
                }
            } else {
                BlockPos groundPos = this.getBlockPosBelowThatAffectsMyMovement();
                float slipperiness = this.level.getBlockState(groundPos).getSlipperiness(level, groundPos, (Entity) (Object) this);
                float friction = this.onGround ? slipperiness * 0.91F : 0.91F;
                Vector3d movement = this.handleRelativeFrictionAndCalculateMovement(input, slipperiness);
                double verticalMovement = this.verticalDelta(movement);

                if (this.hasEffect(Effects.LEVITATION)) {
                    verticalMovement += (0.05D * (this.getEffect(Effects.LEVITATION).getAmplifier() + 1) - verticalMovement) * 0.2D;
                    this.fallDistance = 0.0F;
                } else if (this.level.isClientSide && !this.level.hasChunkAt(groundPos)) {
                    verticalMovement = this.getVerticalCoordinate() > 0.0D ? -0.1D : 0.0D;
                } else if (!this.isNoGravity()) {
                    GravityDirection gravity = getGravityDirection();
                    if (gravity == GravityDirection.WEST || gravity == GravityDirection.UP) {
                        verticalMovement += gravityAcceleration;
                    } else {
                        verticalMovement -= gravityAcceleration;
                    }
                }

                GravityDirection gravity = getGravityDirection();
                if (gravity == GravityDirection.EAST || gravity == GravityDirection.WEST) {
                    this.setDeltaMovement(verticalMovement * 0.98F, movement.y * friction, movement.z * friction);
                } else if (gravity == GravityDirection.UP) {
                    this.setDeltaMovement(movement.x * friction, -verticalMovement * 0.98F, movement.z * friction);
                } else {
                    this.setDeltaMovement(movement.x * friction, verticalMovement * 0.98F, movement.z * friction);
                }
            }
        }
        this.calculateEntityAnimation((LivingEntity) (Object) this, this instanceof IFlyingAnimal);
    }

    /**
     * @author Weever1337
     * @reason Fixes the jump behavior for gravity users.
     */
    @Overwrite
    protected void jumpFromGround() {
        float f = this.getJumpPower();
        if (this.hasEffect(Effects.JUMP)) {
            f += 0.1F * (float) (this.getEffect(Effects.JUMP).getAmplifier() + 1);
        }

        Vector3d vector3d = this.getDeltaMovement();
        GravityDirection direction = this.getGravityDirection();
        switch (direction) {
            case EAST:
                this.setDeltaMovement(f, vector3d.y, vector3d.z);
                break;
            case WEST:
                this.setDeltaMovement(-f, vector3d.y, vector3d.z);
                break;
            case UP:
                this.setDeltaMovement(vector3d.x, -f, vector3d.z);
                break;
            default:
                this.setDeltaMovement(vector3d.x, f, vector3d.z);
                break;
        }

        if (this.isSprinting()) {
            float f1 = this.yRot * ((float) Math.PI / 180F);
            switch (direction) {
                case EAST:
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -MathHelper.cos(f1) * 0.2F, MathHelper.sin(f1) * 0.2F));
                    break;
                case WEST:
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -MathHelper.cos(f1) * 0.2F, -MathHelper.sin(f1) * 0.2F));
                    break;
                case UP:
                    this.setDeltaMovement(this.getDeltaMovement().add(-MathHelper.sin(f1) * 0.2F, 0.0D, MathHelper.cos(f1) * 0.2F)); // todo: fix, cuz it's just copy of default
                    break;
                default:
                    this.setDeltaMovement(this.getDeltaMovement().add(-MathHelper.sin(f1) * 0.2F, 0.0D, MathHelper.cos(f1) * 0.2F));
                    break;
            }
        }

        this.hasImpulse = true;
        net.minecraftforge.common.ForgeHooks.onLivingJump((LivingEntity) (Object) this);
    }
}