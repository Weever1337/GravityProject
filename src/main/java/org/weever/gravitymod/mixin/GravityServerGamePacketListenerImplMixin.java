//package org.weever.gravitymod.mixin;
//
//import net.minecraft.util.Direction;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.ModifyVariable;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import org.weever.gravitymod.access.IGravityEntity;
//import org.weever.gravitymod.util.GravityAPI;
//
//@Mixin(ServerGamePacketListenerImpl.class)
//public abstract class GravityServerGamePacketListenerImplMixin {
//    @Shadow public abstract void teleport(double d, double e, double f, float g, float h);
//
//
//    @Unique
//    public ServerGamePacketListenerImpl gravitymod$this(){
//        return (ServerGamePacketListenerImpl)(Object)this;
//    }
//
//    @Inject(
//            method = "handleMovePlayer",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vector3d;)V"
//            )
//    )
//    private void gravitymod$handleMovePlayer(ServerboundMovePlayerPacket $$0, CallbackInfo ci) {
//        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
//        if (gravityDirection == Direction.DOWN)
//            return;
//        ((IGravityEntity)this.player).gravitymod$setTaggedForFlip(true);
//    }
//
//
//    @ModifyVariable(method = "handleMovePlayer", at = @At(value = "STORE"), ordinal = 0)
//    private boolean gravitymod$handleMovePlayerResetFallDistance(boolean bool,ServerboundMovePlayerPacket $$0) {
//        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
//        if (gravityDirection == Direction.DOWN)
//            return bool;
//
//        double $$2 = clampHorizontal($$0.getX(this.player.getX()));
//        double $$3 = clampVertical($$0.getY(this.player.getY()));
//        double $$4 = clampHorizontal($$0.getZ(this.player.getZ()));
//        Vector3d myPositionVec = RotationUtil.vecPlayerToWorld($$2,$$3,$$4,gravityDirection);
//        Vector3d myLastPositionVec = RotationUtil.vecPlayerToWorld(lastGoodX,lastGoodY,lastGoodZ,gravityDirection);
//
//        return (myPositionVec.y - myLastPositionVec.y > 0);
//    }
//
//    @Inject(
//            method = "handleMoveVehicle",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vector3d;)V"
//            )
//    )
//    private void gravitymod$handleMoveVehicle(ServerboundMoveVehiclePacket $$0, CallbackInfo ci) {
//        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
//        if (gravityDirection == Direction.DOWN)
//            return;
//        Entity $$1 = this.player.getRootVehicle();
//        ((IGravityEntity)$$1).gravitymod$setTaggedForFlip(true);
//    }
//
//
//    @Inject(
//            method = "noBlocksAround",
//            at = @At(
//                    value = "HEAD",
//                    target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vector3d;)V"
//            ),
//            cancellable = true
//    )
//    private void gravitymod$noBlocksAround(Entity $$0, CallbackInfoReturnable<Boolean> cir) {
//        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
//        if (gravityDirection == Direction.DOWN)
//            return;
//
//        Vector3d argVec = new Vector3d(0.0, -0.55, 0.0);
//        argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);
//
//        cir.setReturnValue($$0.level().getBlockStates($$0.getBoundingBox().inflate(0.0625).expandTowards(argVec.x,argVec.y,argVec.z)).
//                allMatch(BlockBehaviour.BlockStateBase::isAir));
//    }
//
//
//    @Shadow
//    public ServerPlayer player;
//
//    @Shadow
//    private static double clampHorizontal(double d) {return 0;}
//
//    ;
//
//    @Shadow
//    private static double clampVertical(double d) {return 0;}
//
//    ;
//
//    @Shadow
//    private double lastGoodX;
//
//    @Shadow
//    private double lastGoodY;
//
//    @Shadow
//    private double lastGoodZ;
//}
