package org.weever.gravitymod.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.*;
import org.weever.gravitymod.network.ModPackets;
import org.weever.gravitymod.v1_20_1.util.Mth;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.access.IClientEntity;
import org.weever.gravitymod.access.IGravityEntity;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationAnimation;
import org.weever.gravitymod.util.RotationParameters;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.AABBUtil;
import org.weever.gravitymod.v1_20_1.util.BlockPosUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.entity.Entity.collideBoundingBoxLegacy;

@Mixin(Entity.class)
public abstract class GravityEntityMixin implements IGravityEntity {
    @Shadow
    private int id;
    @Shadow
    public abstract int getId();
    @Shadow
    public static double getHorizontalDistanceSqr(Vector3d p_213296_0_) {
        return p_213296_0_.x * p_213296_0_.x + p_213296_0_.z * p_213296_0_.z;
    };

    @Shadow
    public float yRot;
    @Shadow
    public float xRot;

    @Shadow
    public abstract float getViewXRot(float p_195050_1_);

    @Shadow
    private BlockPos blockPosition;

    @Shadow
    protected abstract BlockPos getOnPos();

    @Shadow public float moveDist;

    @Shadow protected abstract Vector3d collide(Vector3d Vector3d);

    @Shadow public float walkDistO;
    @Shadow public float walkDist;

    @Shadow public abstract void setPos(double pX, double pY, double pZ);

    @Shadow public double zOld;
    @Shadow public double yOld;
    @Shadow public double xOld;

    @Shadow public abstract void setBoundingBox(AxisAlignedBB aABB);

    @Shadow public int tickCount;

    @Shadow @Nullable public abstract Entity getVehicle();

    /***
     * Gravity Direction for Entities. Note that only Living Entities use tracked/synched entitydata,
     * so regular entities use a function in IEntityAndData instead.
     */
    @Unique
    private static final DataParameter<Direction> gravitymod$GRAVITY_DIRECTION = EntityDataManager.defineId(Entity.class,
            DataSerializers.DIRECTION);

    @Unique
    private static final DataParameter<Float> gravitymod$GRAVITY_STRENGTH = EntityDataManager.defineId(Entity.class,
            DataSerializers.FLOAT);

    @Unique
    @Override
    public Direction gravitymod$getGravityDirection(){
        return this.getEntityData().get(gravitymod$GRAVITY_DIRECTION);
    }

    @Unique
    @Override
    public void gravitymod$setGravityDirection(Direction direction){
        this.getEntityData().set(gravitymod$GRAVITY_DIRECTION, direction);
    }

    @Unique
    @Override
    public void gravitymod$setBaseGravityDirection(Direction gravityDirection) {
        if (gravitymod$baseGravityDirection != gravityDirection) {
            gravitymod$baseGravityDirection = gravityDirection;
            gravitymod$updateGravityStatus(); // will this cause issue?
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void gravitymod$init(EntityType $$0, World $$1, CallbackInfo ci){
        ((Entity) (Object) this).getEntityData().define(gravitymod$GRAVITY_DIRECTION, Direction.DOWN);
        ((Entity) (Object) this).getEntityData().define(gravitymod$GRAVITY_STRENGTH, 1.0F);
    }

    @Unique
    private boolean gravitymod$taggedForFlip;
    @Unique
    @Override
    public void gravitymod$setTaggedForFlip(boolean flip){
        gravitymod$taggedForFlip = flip;
    }

    @Unique
    private boolean gravitymod$initialized = false;

    // not synchronized
    @Unique
    private Direction gravitymod$prevGravityDirection = Direction.DOWN;
    @Unique
    private double gravitymod$prevGravityStrength = 1.0;


    @Unique
    @Override
    public double gravitymod$getGravityStrength(){
        return this.getEntityData().get(gravitymod$GRAVITY_STRENGTH);
    }

    @Unique
    @Override
    public void gravitymod$setGravityStrength(double str){
        this.getEntityData().set(gravitymod$GRAVITY_STRENGTH, (float) str);
    }

    // the base gravity direction
    @Unique
    Direction gravitymod$baseGravityDirection = Direction.DOWN;

    // the base gravity strength
    @Unique
    private double gravitymod$baseGravityStrength = 1.0;

    @Nullable RotationParameters gravitymod$currentRotationParameters = RotationParameters.getDefault();


    @Unique
    private double gravitymod$currentEffectPriority = Double.MIN_VALUE;

    // if it equals entity.tickCount,
    // it means that the gravity update event has already fired in this tick
    @Unique
    private long gravitymod$lastUpdateTickCount = 0;

    @Inject(
            method = "tick",
            at = @At("TAIL"))
    private void gravitymod$tick(CallbackInfo ci) {
        if (!gravitymod$canChangeGravity()) {
            return;
        }
        if (!level.isClientSide()) {
            gravitymod$updateGravityStatus();
        }
        gravitymod$applyGravityChange();
    }
    @Unique
    public void gravitymod$applyGravityChange() {


        if (!gravitymod$canChangeGravity()) {
            return;
        }

        if (gravitymod$currentRotationParameters == null) {
            gravitymod$currentRotationParameters = RotationParameters.getDefault();
        }

        if (gravitymod$prevGravityDirection != gravitymod$getGravityDirection()) {
            gravitymod$applyGravityDirectionChange(
                    gravitymod$prevGravityDirection, gravitymod$getGravityDirection(),
                    gravitymod$currentRotationParameters, false
            );
            gravitymod$prevGravityDirection = gravitymod$getGravityDirection();
        }

        if (Math.abs(gravitymod$getGravityStrength() - gravitymod$prevGravityStrength) > 0.0001) {
            gravitymod$prevGravityStrength = gravitymod$getGravityStrength();
        }
    }

    @Unique
    private boolean gravitymod$canChangeGravity() {
        return true;
    }

    @Unique
    public void gravitymod$applyGravityDirectionChange(
            Direction oldGravity, Direction newGravity,
            RotationParameters rotationParameters, boolean isInitialization
    ) {

        // update bounding box
        setBoundingBox(gravityProject$makeBoundingBox(position().x, position().y, position().z));

        // A weird thing is that,
        // using `entity.setPos(entity.position())` to a painting on client side
        // make the painting move wrongly, because Painting overrides `trackingPosition()`.
        // No entity other than Painting overrides that method.
        // It seems to be legacy code from early versions of Minecraft.

        if (isInitialization) {
            return;
        }

        fallDistance = 0;

        long timeMs = level.getGameTime() * 50;

        Vector3d relativeRotationCenter = gravitymod$getLocalRotationCenter(
                ((Entity)(Object)this), oldGravity, newGravity, rotationParameters
        );
        Vector3d oldPos = position();
        Vector3d oldLastTickPos = new Vector3d(xOld, yOld, zOld);
        Vector3d rotationCenter = oldPos.add(RotationUtil.vecPlayerToWorld(relativeRotationCenter, oldGravity));
        Vector3d newPos = rotationCenter.subtract(RotationUtil.vecPlayerToWorld(relativeRotationCenter, newGravity));
        Vector3d posTranslation = newPos.subtract(oldPos);
        Vector3d newLastTickPos = oldLastTickPos.add(posTranslation);

        this.setPos(newPos.x, newPos.y, newPos.z);
        this.xo = newLastTickPos.x;
        this.yo = newLastTickPos.y;
        this.zo = newLastTickPos.z;
        this.xOld = newLastTickPos.x;
        this.yOld = newLastTickPos.y;
        this.zOld = newLastTickPos.z;

        gravitymod$adjustEntityPosition(oldGravity, newGravity, getBoundingBox());

        if (level.isClientSide()) {
            RotationAnimation ani = ((IClientEntity)this).gravitymod$getGravityAnimation();
            Validate.notNull(ani, "gravity animation is null");

            int rotationTimeMS = rotationParameters.rotationTimeMS();

            ani.startRotationAnimation(
                    newGravity, oldGravity,
                    rotationTimeMS,
                    ((Entity)(Object)this), timeMs, rotationParameters.rotateView(),
                    relativeRotationCenter
            );
        }

        Vector3d realWorldVelocity = getDeltaMovement();
//        GravityMod.LOGGER.info("rWV {}", realWorldVelocity);
//        GravityMod.LOGGER.info("rotated rWV {}", RotationUtil.vecWorldToPlayer(realWorldVelocity, newGravity));
//        setDeltaMovement(RotationUtil.vecWorldToPlayer(realWorldVelocity, newGravity));
        if (rotationParameters.rotateVelocity()) {
            // Rotate velocity with gravity, this will cause things to appear to take a sharp turn
            Vector3f worldSpaceVec = new Vector3f((float) getDeltaMovement().x, (float) getDeltaMovement().y, (float) getDeltaMovement().z);
            worldSpaceVec.transform(RotationUtil.getRotationBetween(oldGravity, newGravity)); // TODO: CAN BE A PROBLEM SO BE CAREFUL
            GravityMod.LOGGER.info("1 {}", worldSpaceVec);
//            setDeltaMovement(RotationUtil.vecWorldToPlayer(new Vector3d(worldSpaceVec), newGravity));
        }
        else {
            // Velocity will be conserved relative to the world, will result in more natural motion
//            GravityMod.LOGGER.info("2 {}",  RotationUtil.vecWorldToPlayer(realWorldVelocity, newGravity));
            setDeltaMovement(RotationUtil.vecWorldToPlayer(realWorldVelocity, newGravity));
        }
    }

    // Adjust position to avoid suffocation in blocks when changing gravity
    @Unique
    private void gravitymod$adjustEntityPosition(Direction oldGravity, Direction newGravity, AxisAlignedBB entityBoundingBox) {
        Entity ent = ((Entity)(Object)this);
        if (ent instanceof AbstractArrowEntity || ent instanceof EnderCrystalEntity) {
            return;
        }

        // for example, if gravity changed from down to north, move up
        // if gravity changed from down to up, also move up
        Direction movingDirection = oldGravity.getOpposite();

        Stream<VoxelShape> collisionStream = ent.level.getCollisions(
                ent,
                entityBoundingBox.inflate(-0.01), // shrink to avoid floating point error
                entity -> true
        );

        List<VoxelShape> collisions = collisionStream.collect(Collectors.toList());

        AxisAlignedBB totalCollisionBox = null;
        for (VoxelShape collision : collisions) {
            if (!collision.isEmpty()) {
                AxisAlignedBB boundingBox = collision.bounds();
                if (totalCollisionBox == null) {
                    totalCollisionBox = boundingBox;
                }
                else {
                    totalCollisionBox = totalCollisionBox.minmax(boundingBox);
                }
            }
        }

        if (totalCollisionBox != null) {
            Vector3d positionAdjustmentOffset = gravitymod$getPositionAdjustmentOffset(
                    entityBoundingBox, totalCollisionBox, movingDirection
            );
            Vector3d newPos = ent.position().add(positionAdjustmentOffset);
            ent.setPos(newPos.x, newPos.y, newPos.z);
        }
    }

    @Unique
    private static Vector3d gravitymod$getPositionAdjustmentOffset(
            AxisAlignedBB entityBoundingBox, AxisAlignedBB nearbyCollisionUnion, Direction movingDirection
    ) {
        Direction.Axis axis = movingDirection.getAxis();
        double offset = 0;
        if (movingDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            double pushing = nearbyCollisionUnion.max(axis);
            double pushed = entityBoundingBox.min(axis);
            if (pushing > pushed) {
                offset = pushing - pushed;
            }
        }
        else {
            double pushing = nearbyCollisionUnion.min(axis);
            double pushed = entityBoundingBox.max(axis);
            if (pushing < pushed) {
                offset = pushed - pushing;
            }
        }

        return new Vector3d(new Vector3f(
                (float)movingDirection.getStepX(), 
                (float)movingDirection.getStepY(), 
                (float)movingDirection.getStepZ())).scale(offset);
    }

    @Unique
    @NotNull
    private static Vector3d gravitymod$getLocalRotationCenter(
            Entity entity,
            Direction oldGravity, Direction newGravity, RotationParameters rotationParameters
    ) {
        if (entity instanceof EnderCrystalEntity) {
            //In the middle of the block below
            return new Vector3d(0, -0.5, 0);
        }

        EntitySize dimensions = entity.getDimensions(entity.getPose());
        if (newGravity.getOpposite() == oldGravity) {
            // In the center of the hit-box
            return new Vector3d(0, dimensions.height / 2, 0);
        }
        else {
            return Vector3d.ZERO;
        }
    }

    // getVelocity() does not return the actual velocity. It returns the velocity plus acceleration.
    // Even if the entity is standing still, getVelocity() will still give a downwards vector.
    // The real velocity is this tick position subtract last tick position
    @Unique
    private static Vector3d gravitymod$getRealWorldVelocity(Entity entity, Direction prevGravityDirection) {
        if (entity.isControlledByLocalInstance()) {
            GravityMod.LOGGER.info("vel {}", new Vector3d(
                    entity.getX() - entity.xo,
                    entity.getY() - entity.yo,
                    entity.getZ() - entity.zo
            ));
            return new Vector3d(
                    entity.getX() - entity.xo,
                    entity.getY() - entity.yo,
                    entity.getZ() - entity.zo
            );
        }

        return RotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), prevGravityDirection);
    }

    @Unique
    public void gravitymod$updateGravityStatus() {
        Entity vehicle = getVehicle();
        if (vehicle != null) {
            gravitymod$setGravityDirection(GravityAPI.getGravityDirection(vehicle));
            gravitymod$setGravityStrength(GravityAPI.getGravityStrength(vehicle));
        }
        else if (gravitymod$isReadyToResetGravity()) {
            gravitymod$setGravityDirection(gravitymod$baseGravityDirection);
            gravitymod$setGravityStrength(gravitymod$baseGravityStrength);

            if (gravitymod$currentEffectPriority == Double.MIN_VALUE) {
                // if no effect is applied, reset the rotation parameters
                gravitymod$currentRotationParameters = RotationParameters.getDefault();
            }
        }
    }


    @Unique
    public boolean gravitymod$isReadyToResetGravity(){
        return gravitymod$lastUpdateTickCount != tickCount;
    }

    @Unique
    @Override
    public void gravitymod$applyGravityEffect(Direction direction) {
        gravitymod$setGravityDirection(direction);
        gravitymod$lastUpdateTickCount = tickCount;
    }




    // THE GENERAL MIXIN STUFF

    @Redirect(
            method = "setPos",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntitySize;makeBoundingBox(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
    private AxisAlignedBB inject_calculateBoundingBox(EntitySize instance, double pX, double pY, double pZ) {
        return gravityProject$makeBoundingBox(pX, pY, pZ);
    }

    @Unique
    private AxisAlignedBB gravityProject$makeBoundingBox(double pX, double pY, double pZ){
        AxisAlignedBB originalBb = dimensions.makeBoundingBox(pX, pY, pZ);
        if (this.getEntityData() != null){
            Entity entity = ((Entity) (Object) this);
            if (entity instanceof ProjectileEntity) return originalBb;
            Direction gravityDirection = ((IGravityEntity)entity).gravitymod$getGravityDirection();
            if (gravityDirection == Direction.DOWN) return originalBb;

            AxisAlignedBB box = originalBb.move(this.position.reverse());
            return RotationUtil.boxPlayerToWorld(box, gravityDirection).move(this.position);
        }
        return originalBb;
    }

    @Inject(
            method = "calculateViewVector",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_getRotationVector(CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
    }

    @Inject(
            method = "getBlockPosBelowThatAffectsMyMovement",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getVelocityAffectingPos(CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        cir.setReturnValue(BlockPosUtil.containing(this.position.add(Vector3d.atCenterOf(gravityDirection.getNormal()))));
    }

    @Inject(
            method = "getEyePosition",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getEyePos(CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        cir.setReturnValue(RotationUtil.vecPlayerToWorld(0.0D, this.eyeHeight, 0.0D, gravityDirection).add(this.position));
    }

    @Inject(
            method = "getEyePosition",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getCamovemeraPosVec(float tickDelta, CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        Vector3d Vector3dd = RotationUtil.vecPlayerToWorld(0.0D, this.eyeHeight, 0.0D, gravityDirection);

        double d = Mth.lerp(tickDelta, this.xo, this.getX()) + Vector3dd.x;
        double e = Mth.lerp(tickDelta, this.yo, this.getY()) + Vector3dd.y;
        double f = Mth.lerp(tickDelta, this.zo, this.getZ()) + Vector3dd.z;
        cir.setReturnValue(new Vector3d(d, e, f));
    }

//    @Inject( // this is not what should be fixed
//            method = "getLightProbePosition",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void inject_getBrightnessAtFEyes(CallbackInfoReturnable<Float> cir) {
//        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
//        if (gravityDirection == Direction.DOWN) return;
//        BlockPos eyePos = new BlockPos(Math.floor(this.getEyePosition(1).x), Math.floor(this.getEyePosition(1).y), Math.floor(this.getEyePosition(1).z));
//        cir.setReturnValue(this.level.hasChunkAt(this.blockPosition) ? this.level.getBrightness(eyePos) : 0.0F);
//    }

    // transform move vector from local to world (the velocity is local)
    @ModifyVariable(
            method = "move",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private Vector3d modify_move_Vector3dd_0_0(Vector3d Vector3dd, MoverType moverType, Vector3d rawArg) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravitymod$taggedForFlip){
            gravitymod$taggedForFlip = false;
            Vector3dd = RotationUtil.vecWorldToPlayer(Vector3dd, gravityDirection);
        }
        if (gravityDirection == Direction.DOWN) {
            return Vector3dd;
        }
        if (moverType == MoverType.PISTON || moverType == MoverType.SHULKER_BOX) {
            Vector3dd = RotationUtil.vecWorldToPlayer(Vector3dd, gravityDirection);
        }

        return RotationUtil.vecPlayerToWorld(Vector3dd, gravityDirection);
    }

    // transform the argument vector back to local coordinate
    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/IProfiler;pop()V",
                    ordinal = 0
            ),
            ordinal = 0,
            argsOnly = true
    )
    private Vector3d modify_move_Vector3dd_0_1(Vector3d Vector3dd) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return Vector3dd;
        }

        return RotationUtil.vecWorldToPlayer(Vector3dd, gravityDirection);
    }

    // transform the local variable (result from collide()) to local coordinate
    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/IProfiler;pop()V",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private Vector3d modify_move_Vector3dd_1(Vector3d Vector3dd) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return Vector3dd;
        }

        return RotationUtil.vecWorldToPlayer(Vector3dd, gravityDirection);
    }

    @Inject(
            method = "getOnPos",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_getLandingPos(CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        BlockPos blockPos = BlockPosUtil.containing(RotationUtil.vecPlayerToWorld(0.0D, -0.20000000298023224D, 0.0D, gravityDirection).add(this.position));
        cir.setReturnValue(blockPos);
    }

    @Inject(
            method = "collide",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$collide(Vector3d movement, CallbackInfoReturnable<Vector3d> cir) {
        Entity self = (Entity) (Object) this;
        Direction gravityDirection = GravityAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB box = this.getBoundingBox();
        ISelectionContext context = ISelectionContext.of(self);
        VoxelShape worldBorderShape = this.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> worldBorderStream = VoxelShapes.joinIsNotEmpty(worldBorderShape, VoxelShapes.create(box.deflate(1.0E-7D)), IBooleanFunction.AND)
                ? Stream.empty() : Stream.of(worldBorderShape);
        Stream<VoxelShape> entityCollisions = this.level.getEntityCollisions(self, box.expandTowards(movement), (e) -> true);
        ReuseableStream<VoxelShape> potentialHits = new ReuseableStream<>(Stream.concat(entityCollisions, worldBorderStream));
        Vector3d collided = movement.lengthSqr() == 0.0D
                ? movement
                : Entity.collideBoundingBoxHeuristically(self, movement, box, this.level, context, potentialHits);

        Vector3d playerMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        Vector3d playerCollided = RotationUtil.vecWorldToPlayer(collided, gravityDirection);

        boolean blockedX = playerMovement.x != playerCollided.x;
        boolean blockedY = playerMovement.y != playerCollided.y;
        boolean blockedZ = playerMovement.z != playerCollided.z;
        boolean onGroundOrLanding = this.onGround || blockedY && playerMovement.y < 0.0D;
        if (this.maxUpStep > 0.0F && onGroundOrLanding && (blockedX || blockedZ)) {
            Vector3d stepAndMove = Entity.collideBoundingBoxHeuristically(self,
                    RotationUtil.vecPlayerToWorld(playerMovement.x, this.maxUpStep, playerMovement.z, gravityDirection),
                    box, this.level, context, potentialHits);
            Vector3d horizontalMovement = RotationUtil.vecPlayerToWorld(playerMovement.x, 0.0D, playerMovement.z, gravityDirection);
            Vector3d stepUp = Entity.collideBoundingBoxHeuristically(self,
                    RotationUtil.vecPlayerToWorld(0.0D, this.maxUpStep, 0.0D, gravityDirection),
                    box.expandTowards(horizontalMovement), this.level, context, potentialHits);
            if (RotationUtil.vecWorldToPlayer(stepUp, gravityDirection).y < (double) this.maxUpStep) {
                Vector3d stepUpThenMove = Entity.collideBoundingBoxHeuristically(self,
                        horizontalMovement, box.move(stepUp), this.level, context, potentialHits).add(stepUp);
                if (gravitymod$playerHorizontalLengthSqr(stepUpThenMove, gravityDirection) > gravitymod$playerHorizontalLengthSqr(stepAndMove, gravityDirection)) {
                    stepAndMove = stepUpThenMove;
                }
            }

            if (gravitymod$playerHorizontalLengthSqr(stepAndMove, gravityDirection) > gravitymod$playerHorizontalLengthSqr(collided, gravityDirection)) {
                double stepAndMovePlayerY = RotationUtil.vecWorldToPlayer(stepAndMove, gravityDirection).y;
                Vector3d settleDown = RotationUtil.vecPlayerToWorld(0.0D, -stepAndMovePlayerY + playerMovement.y, 0.0D, gravityDirection);
                cir.setReturnValue(stepAndMove.add(
                        Entity.collideBoundingBoxHeuristically(self, settleDown, box.move(stepAndMove), this.level, context, potentialHits)));
                return;
            }
        }

        cir.setReturnValue(collided);
    }

    @Unique
    private static double gravitymod$playerHorizontalLengthSqr(Vector3d worldVec, Direction gravityDirection) {
        Vector3d playerVec = RotationUtil.vecWorldToPlayer(worldVec, gravityDirection);
        return playerVec.x * playerVec.x + playerVec.z * playerVec.z;
    }


    @Inject(
            method = "collideBoundingBoxHeuristically",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gravitymod$collideBoundingBox(Entity pEntity, Vector3d pVec, AxisAlignedBB pCollisionBox, World pLevel, ISelectionContext pContext, ReuseableStream<VoxelShape> pPotentialHits, CallbackInfoReturnable<Vector3d> cir) {
        if (pEntity == null)
            return;
        Direction gravityDirection = GravityAPI.getGravityDirection(pEntity);
        if (gravityDirection == Direction.DOWN)
            return;

        List<VoxelShape> $$5 = pPotentialHits.getStream().collect(Collectors.toList());

        WorldBorder $$6 = pLevel.getWorldBorder();
        boolean $$7 = pEntity != null && $$6.isWithinBounds(pCollisionBox.expandTowards(pVec));
        if ($$7) {
            $$5.add($$6.getCollisionShape());
        }

        $$5.addAll(pLevel.getBlockCollisions(pEntity, pCollisionBox.expandTowards(pVec)).collect(Collectors.toList()));

        cir.setReturnValue(gravitymod$collideWithShapesGrav(pVec, pCollisionBox, new ReuseableStream<>($$5.stream()), pEntity));
    }

    @Unique
    private static Vector3d gravitymod$collideWithShapesGrav(Vector3d movement, AxisAlignedBB entityBoundingBox, ReuseableStream<VoxelShape> collisions, Entity entity) {
        Direction gravityDirection;
        if (entity == null || (gravityDirection = GravityAPI.getGravityDirection(entity)) == Direction.DOWN) {
            return collideBoundingBoxLegacy(movement, entityBoundingBox, collisions);
        }

        Vector3d playerMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        double playerMovementX = playerMovement.x;
        double playerMovementY = playerMovement.y;
        double playerMovementZ = playerMovement.z;
        Direction directionX = RotationUtil.dirPlayerToWorld(Direction.EAST, gravityDirection);
        Direction directionY = RotationUtil.dirPlayerToWorld(Direction.UP, gravityDirection);
        Direction directionZ = RotationUtil.dirPlayerToWorld(Direction.SOUTH, gravityDirection);
        if (playerMovementY != 0.0D) {
            playerMovementY = VoxelShapes.collide(directionY.getAxis(), entityBoundingBox, collisions.getStream(), playerMovementY * directionY.getAxisDirection().getStep()) * directionY.getAxisDirection().getStep();
            if (playerMovementY != 0.0D) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(0.0D, playerMovementY, 0.0D, gravityDirection));
            }
        }

        boolean isZLargerThanX = Math.abs(playerMovementX) < Math.abs(playerMovementZ);
        if (isZLargerThanX && playerMovementZ != 0.0D) {
            playerMovementZ = VoxelShapes.collide(directionZ.getAxis(), entityBoundingBox, collisions.getStream(), playerMovementZ * directionZ.getAxisDirection().getStep()) * directionZ.getAxisDirection().getStep();
            if (playerMovementZ != 0.0D) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(0.0D, 0.0D, playerMovementZ, gravityDirection));
            }
        }

        if (playerMovementX != 0.0D) {
            playerMovementX = VoxelShapes.collide(directionX.getAxis(), entityBoundingBox, collisions.getStream(), playerMovementX * directionX.getAxisDirection().getStep()) * directionX.getAxisDirection().getStep();
            if (!isZLargerThanX && playerMovementX != 0.0D) {
                entityBoundingBox = entityBoundingBox.move(RotationUtil.vecPlayerToWorld(playerMovementX, 0.0D, 0.0D, gravityDirection));
            }
        }

        if (!isZLargerThanX && playerMovementZ != 0.0D) {
            playerMovementZ = VoxelShapes.collide(directionZ.getAxis(), entityBoundingBox, collisions.getStream(), playerMovementZ * directionZ.getAxisDirection().getStep()) * directionZ.getAxisDirection().getStep();
        }

        return RotationUtil.vecPlayerToWorld(playerMovementX, playerMovementY, playerMovementZ, gravityDirection);
    }

    @Inject(
            method = "setLocationFromBoundingbox",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$setLocationFromBoundingbox(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof ProjectileEntity) return;
        Direction gravityDirection = GravityAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();
        AxisAlignedBB playerBox = RotationUtil.boxWorldToPlayer(getBoundingBox(), gravityDirection);
        Vector3d worldPos = RotationUtil.vecPlayerToWorld(
                (playerBox.minX + playerBox.maxX) / 2.0D,
                playerBox.minY,
                (playerBox.minZ + playerBox.maxZ) / 2.0D,
                gravityDirection);
        this.setPosRaw(worldPos.x, worldPos.y, worldPos.z);
    }

    @Inject(
            method = "refreshDimensions",
            at = @At("TAIL")
    )
    private void gravitymod$refreshDimensions(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof ProjectileEntity) return;
        Direction gravityDirection = GravityAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;

        setBoundingBox(gravityProject$makeBoundingBox(position().x, position().y, position().z));
    }

    @Inject(
            method = "isInWall",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$isInWall(CallbackInfoReturnable<Boolean> cir) {

        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        if (this.noPhysics) {
            cir.setReturnValue(false);
        } else {
            float $$0 = this.dimensions.width * 0.8F;

            Vector3d rotate = new Vector3d((double)$$0, 1.0E-6, (double)$$0);
            rotate = RotationUtil.vecPlayerToWorld(rotate, GravityAPI.getGravityDirection((Entity) (Object) this));

            AxisAlignedBB $$1 = AABBUtil.ofSize(this.getEyePosition(1), rotate.x,rotate.y,rotate.z);
            cir.setReturnValue(BlockPos.betweenClosedStream($$1)
                    .anyMatch(
                            $$1x -> {
                                BlockState $$2 = this.level.getBlockState($$1x);
                                return !$$2.isAir()
                                        && $$2.isSuffocating(this.level, $$1x)
                                        && VoxelShapes.joinIsNotEmpty(
                                        $$2.getCollisionShape(this.level, $$1x).move((double)$$1x.getX(), (double)$$1x.getY(), (double)$$1x.getZ()),
                                        VoxelShapes.create($$1),
                                        IBooleanFunction.AND
                                );
                            }
                    )
            );
        }
    }

    @Inject(
            method = "getDirection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$getDirection(CallbackInfoReturnable<Direction> cir) {

        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        cir.setReturnValue(Direction.fromYRot(RotationUtil.rotPlayerToWorld((float) this.yRot, this.xRot, gravityDirection).x));
    }

    @Inject(
            method = "getBoundingBoxForPose",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_calculateBoundsForPose(Pose pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB box = cir.getReturnValue().move(this.position.reverse());
        box = box.inflate(-0.01); // avoid entering crouching because of floating point inaccuracy
//        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
//
//        }
        cir.setReturnValue(RotationUtil.boxPlayerToWorld(box, gravityDirection).move(this.position));
    }

    @Inject(
            method = "spawnSprintParticle()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_spawnSprintingParticles(CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();

        Vector3d floorPos = this.position().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.20000000298023224D, 0.0D, gravityDirection));

        BlockPos blockPos = BlockPosUtil.containing(floorPos);
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.getRenderShape() != BlockRenderType.INVISIBLE) {
            Vector3d particlePos = this.position().add(RotationUtil.vecPlayerToWorld((this.random.nextDouble() - 0.5D) * (double) this.dimensions.width, 0.1D, (this.random.nextDouble() - 0.5D) * (double) this.dimensions.width, gravityDirection));
            Vector3d playerVelocity = this.getDeltaMovement();
            Vector3d particleVelocity = RotationUtil.vecPlayerToWorld(playerVelocity.x * -4.0D, 1.5D, playerVelocity.z * -4.0D, gravityDirection);
            this.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState), particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y, particleVelocity.z);
        }
    }


    @Inject(
            method = "updateFluidHeightAndDoFluidPushing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$updateFluidHeightAndDoFluidPushing(ITag<Fluid> $$0, double $$1, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB axisalignedbb = this.getBoundingBox().deflate(0.001D);
        int i = Mth.floor(axisalignedbb.minX);
        int j = Mth.ceil(axisalignedbb.maxX);
        int k = Mth.floor(axisalignedbb.minY);
        int l = Mth.ceil(axisalignedbb.maxY);
        int i1 = Mth.floor(axisalignedbb.minZ);
        int j1 = Mth.ceil(axisalignedbb.maxZ);
        
        if (!this.level.hasChunksAt(i, k, i1, j, l, j1)) {
            cir.setReturnValue(false);
        } else {
            AxisAlignedBB $$2 = this.getBoundingBox().deflate(0.001);
            int $$3 = Mth.floor($$2.minX);
            int $$4 = Mth.ceil($$2.maxX);
            int $$5 = Mth.floor($$2.minY);
            int $$6 = Mth.ceil($$2.maxY);
            int $$7 = Mth.floor($$2.minZ);
            int $$8 = Mth.ceil($$2.maxZ);
            double $$9 = 0.0;
            boolean $$10 = this.isPushedByFluid();
            boolean $$11 = false;
            Vector3d $$12 = Vector3d.ZERO;
            int $$13 = 0;
            BlockPos.Mutable $$14 = new BlockPos.Mutable();

            double localBottom = RotationUtil.boxWorldToPlayer($$2, gravityDirection).minY;
            for (int $$15 = $$3; $$15 < $$4; $$15++) {
                for (int $$16 = $$5; $$16 < $$6; $$16++) {
                    for (int $$17 = $$7; $$17 < $$8; $$17++) {
                        $$14.set($$15, $$16, $$17);
                        FluidState $$18 = this.level.getFluidState($$14);
                        if ($$18.is($$0)) {
                            double $$19 = (double)((float)$$16 + $$18.getHeight(this.level, $$14));
                            AxisAlignedBB fluidBox = new AxisAlignedBB(
                                    (double) $$15, (double) $$16, (double) $$17,
                                    (double) $$15 + 1.0D, $$19, (double) $$17 + 1.0D);
                            double localFluidTop = RotationUtil.boxWorldToPlayer(fluidBox, gravityDirection).maxY;
                            if (localFluidTop >= localBottom) {
                                $$11 = true;
                                $$9 = Math.max(localFluidTop - localBottom, $$9);
                                if ($$10) {
                                    Vector3d $$20 = $$18.getFlow(this.level, $$14);
                                    if ($$9 < 0.4) {
                                        $$20 = $$20.scale($$9);
                                    }

                                    $$12 = $$12.add($$20);
                                    $$13++;
                                }
                            }
                        }
                    }
                }
            }

            if ($$12.length() > 0.0) {
                if ($$13 > 0) {
                    $$12 = $$12.scale(1.0 / (double)$$13);
                }

                if (!(((Entity)(Object)this) instanceof PlayerEntity)) {
                    $$12 = $$12.normalize();
                }

                Vector3d $$21 = this.getDeltaMovement();
                $$12 = $$12.scale($$1 * 1.0);
                double $$22 = 0.003;
                if (Math.abs($$21.x) < 0.003 && Math.abs($$21.z) < 0.003 && $$12.length() < 0.0045000000000000005) {
                    $$12 = $$12.normalize().scale(0.0045000000000000005);
                }

                //This is the main change made to the function
                this.setDeltaMovement(this.getDeltaMovement().add(RotationUtil.vecWorldToPlayer($$12, gravityDirection)));
            }

            if ($$0 instanceof Tag) {
                this.fluidHeight.put((Tag<Fluid>) $$0, $$9);
            }
            cir.setReturnValue($$11);
        }
    }


    @Inject(
            method = "positionRider(Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$positionRider(Entity passenger, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Direction gravityDirection = GravityAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();
        if (self.hasPassenger(passenger)) {
            double offset = self.getPassengersRidingOffset() + passenger.getMyRidingOffset();
            Vector3d worldOffset = RotationUtil.vecPlayerToWorld(0.0D, offset, 0.0D, gravityDirection);
            passenger.setPos(getX() + worldOffset.x, getY() + worldOffset.y, getZ() + worldOffset.z);
        }
    }

    @Inject(
            method = "getDismountLocationForPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$getDismountLocation(LivingEntity passenger, CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB localBox = RotationUtil.boxWorldToPlayer(getBoundingBox(), gravityDirection);
        Vector3d localPos = RotationUtil.vecWorldToPlayer(position(), gravityDirection);
        cir.setReturnValue(RotationUtil.vecPlayerToWorld(localPos.x, localBox.maxY, localPos.z, gravityDirection));
    }

    @Inject(
            method = "push(Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_pushAwayFrom(Entity entity, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        Direction otherGravityDirection = GravityAPI.getGravityDirection(entity);

        if (gravityDirection == Direction.DOWN && otherGravityDirection == Direction.DOWN) return;

        ci.cancel();

        if (!this.isPassengerOfSameVehicle(entity)) {
            if (!entity.noPhysics && !this.noPhysics) {
                Vector3d entityOffset = entity.getBoundingBox().getCenter().subtract(this.getBoundingBox().getCenter());

                {
                    Vector3d playerEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, gravityDirection);
                    double dx = playerEntityOffset.x;
                    double dz = playerEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582D) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1.0D / f;
                        if (g > 1.0D) {
                            g = 1.0D;
                        }

                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806D;
                        dz *= 0.05000000074505806D;
                        if (!this.isVehicle()) {
                            this.push(-dx, 0.0D, -dz);
                        }
                    }
                }

                {
                    Vector3d entityEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, otherGravityDirection);
                    double dx = entityEntityOffset.x;
                    double dz = entityEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582D) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1.0D / f;
                        if (g > 1.0D) {
                            g = 1.0D;
                        }

                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806D;
                        dz *= 0.05000000074505806D;
                        if (!entity.isVehicle()) {
                            entity.push(dx, 0.0D, dz);
                        }
                    }
                }
            }
        }
    }

    @Inject(
            method = "isFree(DDD)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$isFree(double $$0, double $$1, double $$2, CallbackInfoReturnable<Boolean> cir) {

        Direction gravityDirection = GravityAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        Vector3d rotate = new Vector3d($$0, $$1, $$2);
        rotate = RotationUtil.vecWorldToPlayer(rotate, GravityAPI.getGravityDirection((Entity) (Object) this));
        cir.setReturnValue(this.isFree(this.getBoundingBox().move(rotate.x, rotate.y, rotate.z)));
    }


    @ModifyVariable(
            method = "updateFluidOnEyes()V",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private double submergedInWaterEyeFix(double d) {
        d = this.getEyePosition(1).y();
        return d;
    }

    @ModifyVariable(
            method = "updateFluidOnEyes()V",
            at = @At(
                    value = "STORE"
            ),
            ordinal = 0
    )
    private BlockPos submergedInWaterPosFix(BlockPos blockpos) {
        blockpos = BlockPosUtil.containing(this.getEyePosition(1));
        return blockpos;
    }

    /**Shadows, ignore
     * -------------------------------------------------------------------------------------------------------------
     * */

    @Shadow
    private Vector3d position;

    @Shadow
    private EntitySize dimensions;

    @Shadow
    private float eyeHeight;

    @Shadow
    public double xo;

    @Shadow
    public double yo;

    @Shadow
    public double zo;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract Vector3d getEyePosition(float partialTicks);

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public World level;

    @Shadow
    public boolean noPhysics;

    @Shadow
    public abstract Vector3d getDeltaMovement();

    @Shadow
    public abstract boolean isVehicle();

    @Shadow
    public abstract AxisAlignedBB getBoundingBox();

    @Shadow
    public abstract Vector3d position();
    
    @Shadow
    public abstract boolean isPassengerOfSameVehicle(Entity entity);

    @Shadow
    public abstract void push(double deltaX, double deltaY, double deltaZ);

    @Shadow
    public abstract double getEyeY();

    @Shadow
    public abstract float getViewYRot(float tickDelta);

    @Shadow
    @Final
    protected Random random;

    @Shadow
    public float fallDistance;

    @Shadow public abstract EntityDataManager getEntityData();

    @Shadow @Final protected EntityDataManager entityData;

    @Shadow protected boolean onGround;
    @Shadow public float maxUpStep;

    @Shadow
    public static Vector3d collideBoundingBox(Vector3d pVec, AxisAlignedBB pCollisionBox, IWorldReader pLevel, ISelectionContext pSelectionContext, ReuseableStream<VoxelShape> pPotentialHits) {
        return null;
    }

    @Shadow public abstract void setPosRaw(double x, double y, double z);

    @Shadow public abstract boolean isAddedToWorld();

    @Shadow public abstract boolean isPushedByFluid();

    @Shadow protected Object2DoubleMap<Tag<Fluid>> fluidHeight;

    @Shadow public abstract void setDeltaMovement(Vector3d Vector3d);

    @Shadow protected abstract boolean isFree(AxisAlignedBB aABB);
}
