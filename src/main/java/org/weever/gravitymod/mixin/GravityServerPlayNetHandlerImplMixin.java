package org.weever.gravitymod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.access.IGravityEntity;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.Mth;

@Mixin(ServerPlayNetHandler.class)
public abstract class GravityServerPlayNetHandlerImplMixin {

    @Unique
    public ServerPlayNetHandler gravitymod$this(){
        return (ServerPlayNetHandler)(Object)this;
    }

    @Inject(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;move(Lnet/minecraft/entity/MoverType;Lnet/minecraft/util/math/vector/Vector3d;)V")
    )
    private void gravitymod$handleMovePlayer(CPlayerPacket $$0, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN)
            return;
        ((IGravityEntity)this.player).gravitymod$setTaggedForFlip(true);
    }


    @ModifyVariable(method = "handleMovePlayer", at = @At(value = "STORE"), ordinal = 0)
    private boolean gravitymod$handleMovePlayerResetFallDistance(boolean bool,CPlayerPacket $$0) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN)
            return bool;

        double $$2 =  gravityProjectFixing$clampHorizontal($$0.getX(this.player.getX()));
        double $$3 = gravityProjectFixing$clampVertical($$0.getY(this.player.getY()));
        double $$4 = gravityProjectFixing$clampHorizontal($$0.getZ(this.player.getZ()));
        Vector3d myPositionVec = RotationUtil.vecWorldToPlayer($$2,$$3,$$4,gravityDirection);
        Vector3d myLastPositionVec = RotationUtil.vecWorldToPlayer(lastGoodX,lastGoodY,lastGoodZ,gravityDirection);

        return (myPositionVec.y - myLastPositionVec.y > 0);
    }

    @Inject(
            method = "handleMoveVehicle",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MoverType;Lnet/minecraft/util/math/vector/Vector3d;)V"
            )
    )
    private void gravitymod$handleMoveVehicle(CMoveVehiclePacket pPacket, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN)
            return;
        Entity $$1 = this.player.getRootVehicle();
        ((IGravityEntity)$$1).gravitymod$setTaggedForFlip(true);
    }


    @Inject(
            method = "noBlocksAround",
            at = @At(
                    value = "HEAD",
                    target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/util/math/Vector3d;)V"
            ),
            cancellable = true
    )
    private void gravitymod$noBlocksAround(Entity $$0, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN)
            return;

        Vector3d argVec = new Vector3d(0.0, -0.55, 0.0);
        argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);

        cir.setReturnValue($$0.level.getBlockStates($$0.getBoundingBox().inflate(0.0625).expandTowards(argVec.x,argVec.y,argVec.z)).
                allMatch(BlockState::isAir));
    }


    @Shadow
    public ServerPlayerEntity player;

    @Unique
    private static double gravityProjectFixing$clampHorizontal(double p_143610_) {
        return Mth.clamp(p_143610_, -3.0E7D, 3.0E7D);
    }

    @Unique
    private static double gravityProjectFixing$clampVertical(double p_143654_) {
        return Mth.clamp(p_143654_, -2.0E7D, 2.0E7D);
    }

    @Shadow
    private double lastGoodX;

    @Shadow
    private double lastGoodY;

    @Shadow
    private double lastGoodZ;
}
