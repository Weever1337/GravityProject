package org.weever.gravitymod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin implements IGravityEntity {
    @Shadow
    public float yRot;
    @Shadow
    public float xRot;
    @Shadow
    public boolean noPhysics;
    @Shadow
    public World level;
    @Shadow
    public boolean horizontalCollision;
    @Shadow
    public boolean verticalCollision;
    @Shadow
    public float fallDistance;
    @Shadow
    public float walkDist;
    @Shadow
    public float moveDist;
    @Shadow
    public double xo;
    @Shadow
    public double yo;
    @Shadow
    public double zo;
    @Shadow
    public boolean hasImpulse;
    @Shadow
    public float maxUpStep;
    @Shadow
    protected Vector3d stuckSpeedMultiplier;
    @Shadow
    protected boolean onGround;
    @Shadow
    protected boolean firstTick;
    @Shadow
    @Final
    protected Random random;
    @Shadow
    private Vector3d position;
    @Shadow
    private float nextStep;
    @Shadow
    private float nextFlap;
    @Shadow
    private int remainingFireTicks;
    @Shadow
    private EntitySize dimensions;
    @Shadow
    private float eyeHeight;

    @Shadow
    public abstract Vector3d getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(Vector3d p_213317_1_);

    @Shadow
    public abstract AxisAlignedBB getBoundingBox();

    @Shadow
    public abstract void setBoundingBox(AxisAlignedBB p_174826_1_);

    @Shadow
    public abstract EntitySize getDimensions(Pose p_213305_1_);

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    protected abstract Vector3d limitPistonMovement(Vector3d p_213308_1_);

    @Shadow
    public abstract void setDeltaMovement(double p_213293_1_, double p_213293_3_, double p_213293_5_);

    @Shadow
    protected abstract boolean isMovementNoisy();

    @Shadow
    public abstract boolean isPassenger();

    @Shadow
    public abstract boolean isSteppingCarefully();

    @Shadow
    protected abstract float nextStep();

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    public abstract boolean isVehicle();

    @Shadow
    public abstract @Nullable Entity getControllingPassenger();

    @Shadow
    protected abstract void playSwimSound(float p_203006_1_);

    @Shadow
    protected abstract void playStepSound(BlockPos p_180429_1_, BlockState p_180429_2_);

    @Shadow
    protected abstract boolean makeFlySound();

    @Shadow
    protected abstract float playFlySound(float p_191954_1_);

    @Shadow
    protected abstract void checkInsideBlocks();

    @Shadow
    public abstract void fillCrashReportCategory(CrashReportCategory p_85029_1_);

    @Shadow
    protected abstract float getBlockSpeedFactor();

    @Shadow
    public abstract void setRemainingFireTicks(int p_241209_1_);

    @Shadow
    protected abstract int getFireImmuneTicks();

    @Shadow
    public abstract boolean isOnFire();

    @Shadow
    public abstract boolean isInWaterRainOrBubble();

    @Shadow
    public abstract void playSound(SoundEvent p_184185_1_, float p_184185_2_, float p_184185_3_);

    @Shadow
    public abstract boolean isControlledByLocalInstance();

    @Shadow
    public abstract BlockPos blockPosition();

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public abstract boolean isFree(double p_70038_1_, double p_70038_3_, double p_70038_5_);

    @Shadow
    public abstract boolean isInLava();

    @Shadow
    public abstract Vector3d getLookAngle();

    @Shadow
    protected abstract void setSharedFlag(int p_70052_1_, boolean p_70052_2_);

    @Shadow
    public abstract double getFluidHeight(ITag<Fluid> p_233571_1_);

    @Shadow
    public abstract double getFluidJumpThreshold();

    @Shadow
    public abstract boolean isNoGravity();

    @Shadow
    protected abstract void checkFallDamage(double p_184231_1_, boolean p_184231_3_, BlockState p_184231_4_, BlockPos p_184231_5_);

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract void setPosRaw(double p_226288_1_, double p_226288_3_, double p_226288_5_);

    @Shadow
    public abstract float getEyeHeight();

    @Shadow
    public abstract boolean isAddedToWorld();

    @Shadow
    public abstract void setSwimming(boolean p_204711_1_);

    @Shadow
    public abstract void updateSwimming();

    @Shadow
    public abstract float getBbWidth();

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Shadow public abstract boolean isSwimming();

    @Shadow protected abstract boolean canEnterPose(Pose p_213298_1_);

    @Shadow public abstract boolean isEyeInFluid(ITag<Fluid> p_208600_1_);

    @Shadow @Nullable public abstract Entity getVehicle();

    /**
     * @author Weever1337
     * @reason Fixes the method for gravity users.
     */
    @Overwrite
    public boolean isInWall() {
        if (this.noPhysics) {
            return false;
        } else {
            float size = this.dimensions.width * 0.8F;
            float sizeX = size;
            float sizeY = size;
            float sizeZ = size;
            float eyeX = (float) this.getEyeX();
            float eyeY = (float) this.getEyeY();
            float eyeZ = (float) this.getEyeZ();
            if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
                sizeX = 0.1F;
            } else {
                sizeY = 0.1F;
            }
            AxisAlignedBB axisalignedbb = AxisAlignedBB.ofSize(sizeX, sizeY, sizeZ).move(eyeX, eyeY, eyeZ);
            return this.level.getBlockCollisions((Entity) (Object) this, axisalignedbb, (blockstate, pos) -> blockstate.isSuffocating(this.level, pos)).findAny().isPresent();
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes the method for gravity users.
     */
    @Overwrite
    public void setLocationFromBoundingbox() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        GravityDirection direction = getGravityDirection();
        if (direction == GravityDirection.EAST) {
            this.setPosRaw(axisalignedbb.minX, (axisalignedbb.minY + axisalignedbb.maxY) / 2, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D);
        } else if (direction == GravityDirection.WEST) {
            this.setPosRaw(axisalignedbb.maxX, (axisalignedbb.minY + axisalignedbb.maxY) / 2, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D);
        } else if (getGravityDirection() == GravityDirection.UP) {
            this.setPosRaw((axisalignedbb.minX + axisalignedbb.maxX) / 2.0D, axisalignedbb.maxY, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D);
        } else {
            this.setPosRaw((axisalignedbb.minX + axisalignedbb.maxX) / 2.0D, axisalignedbb.minY, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D);
        }
        if (this.isAddedToWorld() && !this.level.isClientSide && level instanceof ServerWorld) {
            ((ServerWorld) this.level).updateChunkPos((Entity) (Object) this); // Forge - Process chunk registration after moving.
        }
    }

    @Override
    public Vector3d getVerticalVectorNullifier() {
        if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
            return new Vector3d(0, 1, 1);
        } else {
            return new Vector3d(1, 0, 1);
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes the method for gravity users.
     */
    @Overwrite
    protected float getEyeHeight(Pose p_213316_1_, EntitySize p_213316_2_) {
        if (getGravityDirection() == GravityDirection.WEST) {
            return p_213316_2_.height * 0.15F;
        }
        if (getGravityDirection() == GravityDirection.UP) {
            return p_213316_2_.height * 0.85F;
        }
        return p_213316_2_.height * 0.85F;
    }

    /**
     * @author Weever1337
     * @reason Fixes the method for gravity users.
     */
    @Overwrite
    public void moveRelative(float speed, Vector3d input) {
        Vector3d vector3d = this.transformInputToWorldCoordinates(input, speed, this.yRot);
        this.setDeltaMovement(this.getDeltaMovement().add(vector3d));
    }

    /**
     * @author Weever1337
     * @reason Fixes the method for gravity users.
     */
    @Overwrite
    public void move(MoverType moverType, Vector3d requestedMove) {
        if (this.noPhysics) {
            this.setBoundingBox(this.getBoundingBox().move(requestedMove));
            this.setLocationFromBoundingbox();
        } else {
            if (moverType == MoverType.PISTON) {
                requestedMove = this.limitPistonMovement(requestedMove);
                if (requestedMove.equals(Vector3d.ZERO)) {
                    return;
                }
            }

            this.level.getProfiler().push("move");
            if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7D) {
                requestedMove = requestedMove.multiply(this.stuckSpeedMultiplier);
                this.stuckSpeedMultiplier = Vector3d.ZERO;
                this.setDeltaMovement(Vector3d.ZERO);
            }

            requestedMove = this.maybeBackOffFromEdge(requestedMove, moverType);
            Vector3d actualMove = this.collide(requestedMove);
            if (actualMove.lengthSqr() > 1.0E-7D) {
                this.setBoundingBox(this.getBoundingBox().move(actualMove));
                this.setLocationFromBoundingbox();
            }

            this.level.getProfiler().pop();
            this.level.getProfiler().push("rest");
            this.horizontalCollision = this.horizontalCollision(requestedMove, actualMove);
            this.verticalCollision = this.verticalCollision(requestedMove, actualMove);
            this.onGround = this.isOnGroundBasedOnMove(this.verticalCollision, requestedMove);
            BlockPos blockpos = this.getOnPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            this.checkFallDamage(this.verticalDelta(actualMove), this.onGround, blockstate, blockpos);
            Vector3d requestedDeltaMovement = this.getDeltaMovement();
            this.handleHorizontalCollide(requestedDeltaMovement, requestedMove, actualMove);

            Block block = blockstate.getBlock();
            if (this.verticalCollision) {
                this.setDeltaMovement(getDeltaMovement().multiply(this.getVerticalVectorNullifier()));
            }

            if (this.onGround && !this.isSteppingCarefully()) {
                block.stepOn(this.level, blockpos, (Entity) (Object) this);
            }

            if (this.isMovementNoisy() && !this.isPassenger()) {
                double deltaX = actualMove.x;
                double deltaY = actualMove.y;
                double deltaZ = actualMove.z;
                if (!block.is(BlockTags.CLIMBABLE)) {
                    if (this.getGravityDirection() == GravityDirection.DOWN) {
                        deltaY = 0.0D;
                    } else if (this.getGravityDirection() == GravityDirection.EAST || this.getGravityDirection() == GravityDirection.WEST) {
                        deltaX = 0.0D;
                    }
                }

                this.walkDist = (float) ((double) this.walkDist + (double) MathHelper.sqrt(getHorizontalDistanceSquared(actualMove)) * 0.6D);
                this.moveDist = (float) ((double) this.moveDist + (double) MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 0.6D);
                if (this.moveDist > this.nextStep && !blockstate.isAir(this.level, blockpos)) {
                    this.nextStep = this.nextStep();
                    if (this.isInWater()) {
                        Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : (Entity) (Object) this;
                        float f = entity == (Object) this ? 0.35F : 0.4F;
                        Vector3d vector3d2 = entity.getDeltaMovement();
                        float f1 = MathHelper.sqrt(getSwimProbability(vector3d2)) * f;
                        if (f1 > 1.0F) {
                            f1 = 1.0F;
                        }

                        this.playSwimSound(f1);
                    } else {
                        this.playStepSound(blockpos, blockstate);
                    }
                } else if (this.moveDist > this.nextFlap && this.makeFlySound() && blockstate.isAir(this.level, blockpos)) {
                    this.nextFlap = this.playFlySound(this.moveDist);
                }
            }

            try {
                this.checkInsideBlocks();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being checked for collision");
                this.fillCrashReportCategory(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            float speedFactor = this.getBlockSpeedFactor();
            this.setDeltaMovement(multiplyDeltaMovementBySpeedFactor(this.getDeltaMovement(), speedFactor));
            if (BlockPos.betweenClosedStream(this.getBoundingBox().deflate(0.001D)).noneMatch((blockPos) ->
            {
                BlockState state = level.getBlockState(blockPos);
                return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.isBurning(level, blockPos);
            }) && this.remainingFireTicks <= 0) {
                this.setRemainingFireTicks(-this.getFireImmuneTicks());
            }

            if (this.isInWaterRainOrBubble() && this.isOnFire()) {
                this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                this.setRemainingFireTicks(-this.getFireImmuneTicks());
            }

            this.level.getProfiler().pop();
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes the method for gravity users.
     */
    @Overwrite
    protected BlockPos getOnPos() {
        int i;
        int j;
        int k;
        if (this.getGravityDirection() == GravityDirection.EAST) {
            i = MathHelper.floor(this.position.x - 0.2);
            j = MathHelper.floor(this.position.y);
            k = MathHelper.floor(this.position.z);
        } else if (this.getGravityDirection() == GravityDirection.WEST) {
            i = MathHelper.ceil(this.position.x + 0.2);
            j = MathHelper.floor(this.position.y);
            k = MathHelper.floor(this.position.z);
        } else {
            i = MathHelper.floor(this.position.x);
            j = MathHelper.ceil(this.position.y - 0.2F);
            k = MathHelper.floor(this.position.z);
        }
        BlockPos blockpos = new BlockPos(i, j, k);
        if (this.level.isEmptyBlock(blockpos)) {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = this.level.getBlockState(blockpos1);
            if (blockstate.collisionExtendsVertically(this.level, blockpos1, (Entity) (Object) this)) {
                return blockpos1;
            }
        }

        return blockpos;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected Vector3d maybeBackOffFromEdge(Vector3d movement, MoverType moverType) {
        if (moverType == MoverType.SELF || moverType == MoverType.PLAYER) {
            if (this.isShiftKeyDown() && this.onGround) {
                GravityDirection gravity = getGravityDirection();
                BlockPos blockpos = this.getOnPos();

                if (this.level.getBlockState(blockpos).isSuffocating(this.level, blockpos)) {
                    return movement;
                }

                double centerX = this.getX();
                double centerZ = this.getZ();
                float halfWidth = this.getBbWidth() * 0.5F;

                boolean hasSupport = false;
                for (int i = 0; i < 4; ++i) {
                    double dx = centerX + (double) ((i % 2) * 2 - 1) * halfWidth;
                    double dz = centerZ + (double) ((i / 2) * 2 - 1) * halfWidth;
                    BlockPos checkPos;

                    switch (gravity) {
                        case EAST:
                            checkPos = new BlockPos(this.getX() - 1.0D, this.getY() + (double) (((i % 2) * 2 - 1) * halfWidth), dz);
                            break;
                        case WEST:
                            checkPos = new BlockPos(this.getX() + 1.0D, this.getY() + (double) (((i % 2) * 2 - 1) * halfWidth), dz);
                            break;
                        case UP:
                            checkPos = new BlockPos(dx, this.getY() - 1.0D, this.getZ() + (double) (((i / 2) * 2 - 1) * halfWidth));
                            break;
                        default:
                            checkPos = new BlockPos(dx, this.getY() - 1.0D, dz);
                            break;
                    }

                    if (!this.level.getBlockState(checkPos).isAir()) {
                        hasSupport = true;
                        break;
                    }
                }

                if (!hasSupport) {
                    switch (gravity) {
                        case EAST:
                            return new Vector3d(-0.05D, movement.y, movement.z);
                        case WEST:
                            return new Vector3d(0.05D, movement.y, movement.z);
                        case UP:
                            return new Vector3d(movement.x, 0.05D, movement.z);
                        default:
                            return new Vector3d(movement.x, -0.05D, movement.z);
                    }
                }
            }
        }
        return movement;
    }

    /**
     * @author Weever1337
     * @reason Fixes bounding box for gravity users.
     */
    @Overwrite
    protected AxisAlignedBB getBoundingBoxForPose(Pose pose) {
        EntitySize entitysize = this.getDimensions(pose);
        float halfWidth = entitysize.width / 2.0F;
        if (this.getGravityDirection() == GravityDirection.EAST) {
            Vector3d vector3d = new Vector3d(this.getX(), this.getY() - halfWidth, this.getZ() - halfWidth);
            Vector3d vector3d1 = new Vector3d(this.getX() + entitysize.height, this.getY() + halfWidth, this.getZ() + halfWidth);
            return new AxisAlignedBB(vector3d, vector3d1);
        } else if (this.getGravityDirection() == GravityDirection.WEST) {
            Vector3d vector3d = new Vector3d(this.getX() - entitysize.height, this.getY() - halfWidth, this.getZ() - halfWidth);
            Vector3d vector3d1 = new Vector3d(this.getX(), this.getY() + halfWidth, this.getZ() + halfWidth);
            return new AxisAlignedBB(vector3d, vector3d1);
        } else {
            Vector3d vector3d = new Vector3d(this.getX() - halfWidth, this.getY(), this.getZ() - halfWidth);
            Vector3d vector3d1 = new Vector3d(this.getX() + halfWidth, this.getY() + entitysize.height, this.getZ() + halfWidth);
            return new AxisAlignedBB(vector3d, vector3d1);
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes this method for gravity users
     */
    @Overwrite
    protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
        switch (this.getGravityDirection()) {
            case EAST:
                return new BlockPos(this.getBoundingBox().minX - 0.5000001D, this.position.y, this.position.z);
            case WEST:
                return new BlockPos(this.getBoundingBox().maxX + 0.5000001D, this.position.y, this.position.z);
            case UP:
                return new BlockPos(this.position.x, this.getBoundingBox().maxY + 0.5000001D, this.position.z);
            default:
                return new BlockPos(this.position.x, this.getBoundingBox().minY - 0.5000001D, this.position.z);
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes the refreshDimensions method for gravity users (boundingBox and move method).
     */
    @Overwrite
    public void refreshDimensions() {
        EntitySize entitysize = this.dimensions;
        Pose pose = this.getPose();
        EntitySize entitysize1 = this.getDimensions(pose);
        net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory.getEntitySizeForge((Entity) (Object) this, pose, entitysize, entitysize1, this.getEyeHeight(pose, entitysize1));
        entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        this.eyeHeight = sizeEvent.getNewEyeHeight();
        if (entitysize1.width < entitysize.width) {
            AxisAlignedBB bb = getBoundingBoxForPose(pose);
            this.setBoundingBox(bb);
        } else {
            AxisAlignedBB bb = getBoundingBoxForPose(pose);
            this.setBoundingBox(bb);
            if (entitysize1.width > entitysize.width && !this.firstTick && !this.level.isClientSide) {
                float f = entitysize.width - entitysize1.width;
                if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
                    this.move(MoverType.SELF, new Vector3d(0.0D, f, f));
                } else {
                    this.move(MoverType.SELF, new Vector3d(f, 0.0D, f));
                }
            }
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes boundingBox in setPos
     */
    @Overwrite
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        try {
            AxisAlignedBB boundingBoxForPose = this.getBoundingBoxForPose(getPose());
            this.setBoundingBox(boundingBoxForPose);
        } catch (NullPointerException e) {
            AxisAlignedBB bb = this.dimensions.makeBoundingBox(x, y, z);
            this.setBoundingBox(bb);
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes eye position for gravity users
     */
    @Overwrite
    public final Vector3d getEyePosition(float ticks) {
        if (ticks == 1.0F) {
            Vector3d vector3d;
            if (getGravityDirection() == GravityDirection.EAST || getGravityDirection() == GravityDirection.WEST) {
                vector3d = new Vector3d(getEyeVerticalCoordinate(), this.getY(), this.getZ());
            } else {
                vector3d = new Vector3d(this.getX(), this.getEyeVerticalCoordinate(), this.getZ());
            }
            return vector3d;
        } else {
            double x = MathHelper.lerp(ticks, this.xo, this.getX());
            double y = MathHelper.lerp(ticks, this.yo, this.getY());
            double z = MathHelper.lerp(ticks, this.zo, this.getZ());
            if (getGravityDirection() == GravityDirection.EAST) {
                x += this.getEyeHeight();
            } else if (getGravityDirection() == GravityDirection.WEST) {
                x -= this.getEyeHeight();
            } else {
                y += this.getEyeHeight();
            }
            Vector3d vector3d = new Vector3d(x, y, z);
            return vector3d;
        }
    }

    /**
     * @author Weever1337
     * @reason Recalculates view vector for gravity users
     */
    @Overwrite
    protected final Vector3d calculateViewVector(float xRot, float yRot) {
        float f = xRot * ((float) Math.PI / 180F);
        float f1 = -yRot * ((float) Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        switch (this.getGravityDirection()) {
            case EAST:
                return new Vector3d(-f5, -f2 * f4, -f3 * f4);
            case WEST:
                return new Vector3d(f5, -f2 * f4, f3 * f4);
            case UP:
                return new Vector3d(f3 * f4, f5, f2 * f4);
            default:
                return new Vector3d(f3 * f4, -f5, f2 * f4);
        }
    }

    /**
     * @author Weever1337
     * @reason Fixes collide "system"
     */
    @Overwrite
    private Vector3d collide(Vector3d requestedMove) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        ISelectionContext iselectioncontext = ISelectionContext.of((Entity) (Object) this);
        VoxelShape voxelshape = this.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> stream = VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(axisalignedbb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
        Stream<VoxelShape> stream1 = this.level.getEntityCollisions((Entity) (Object) this, axisalignedbb.expandTowards(requestedMove), (p_233561_0_) -> true);
        ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
        Vector3d vector3d = requestedMove.lengthSqr() == 0.0D ? requestedMove : Entity.collideBoundingBoxHeuristically((Entity) (Object) this, requestedMove, axisalignedbb, this.level, iselectioncontext, reuseablestream);
        boolean verticalCollision = this.verticalCollision(requestedMove, vector3d);
        boolean horizontalCollision = this.horizontalCollision(requestedMove, vector3d);
        boolean onGround = this.onGround || this.isOnGroundBasedOnMove(verticalCollision, requestedMove);
        if (this.maxUpStep > 0.0F && onGround && horizontalCollision) {
            Vector3d vector3d1 = Entity.collideBoundingBoxHeuristically((Entity) (Object) this, setVerticalCoordinate(this.maxUpStep, requestedMove), axisalignedbb, this.level, iselectioncontext, reuseablestream);
            Vector3d vector3d2 = Entity.collideBoundingBoxHeuristically((Entity) (Object) this, setVerticalCoordinate(this.maxUpStep, Vector3d.ZERO), axisalignedbb.expandTowards(setVerticalCoordinate(0, requestedMove)), this.level, iselectioncontext, reuseablestream);
            if (vector3d2.y < (double) this.maxUpStep) {
                Vector3d vector3d3 = Entity.collideBoundingBoxHeuristically((Entity) (Object) this, setVerticalCoordinate(0, requestedMove), axisalignedbb.move(vector3d2), this.level, iselectioncontext, reuseablestream).add(vector3d2);
                if (getHorizontalDistanceSquared(vector3d3) > getHorizontalDistanceSquared(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }

            if (getHorizontalDistanceSquared(vector3d1) > getHorizontalDistanceSquared(vector3d)) {
                return vector3d1.add(Entity.collideBoundingBoxHeuristically((Entity) (Object) this, setVerticalCoordinate((float) (-this.getVerticalDelta(vector3d1) + getVerticalDelta(requestedMove)), Vector3d.ZERO), axisalignedbb.move(vector3d1), this.level, iselectioncontext, reuseablestream));
            }
        }

        return vector3d;
    }


    /**
     * @author Weever1337
     * @reason Fixes eye position for Y
     */
    @Overwrite
    public double getEyeY() {
        if (getGravityDirection() == GravityDirection.DOWN) { // TODO: Fix it for other directions
            return this.position.y + (double) this.eyeHeight;
        } else {
            return this.position.y;
        }
    }

//    @Inject(method = "tick", at = @At("TAIL"))
//    public void tick(CallbackInfo ci) {
//        if (this.getGravityDirection() != GravityDirection.DOWN) {
//            this.refreshDimensions();
//        }
//    }
}
