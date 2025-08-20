package org.weever.gravitymod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(value = ClientPlayNetHandler.class, priority = 1001)
public abstract class GravityClientPacketHandlerListener {
    @Shadow
    @Final
    private Minecraft minecraft;


    @Shadow private ClientWorld level;

    @Inject(
            method = "handleGameEvent",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void redirect_onGameStateChange_getEyeY_0(SChangeGameStatePacket packet, CallbackInfo ci) {
        PacketThreadUtil.ensureRunningOnSameThread(packet, (ClientPlayNetHandler)(Object)this, this.minecraft);

        ClientPlayerEntity playerEntity = this.minecraft.player;
        if (playerEntity == null) return;

        Direction gravityDirection = GravityAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        if (packet.getEvent() == SChangeGameStatePacket.ARROW_HIT_PLAYER) {
            ci.cancel();
            Vector3d eyePos = playerEntity.getEyePosition(1.0F);
            this.level.playSound(playerEntity, eyePos.x, eyePos.y, eyePos.z, SoundEvents.ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18F, 0.45F);
        }
    }

    @Inject(
            method = "handleExplosion(Lnet/minecraft/network/play/server/SExplosionPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void wrapOperation_onExplosion_add_0(SExplosionPacket packet, CallbackInfo ci) {
        PacketThreadUtil.ensureRunningOnSameThread(packet, (ClientPlayNetHandler)(Object)this, this.minecraft);

        ClientPlayerEntity playerEntity = this.minecraft.player;
        if (playerEntity == null) return;

        Direction gravityDirection = GravityAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        ci.cancel();

        Vector3d knockback = RotationUtil.vecWorldToPlayer(
                packet.getKnockbackX(),
                packet.getKnockbackY(),
                (double)packet.getKnockbackZ(),
                gravityDirection
        );

        Explosion explosion = new Explosion(this.minecraft.level, null, packet.getX(), packet.getY(), packet.getZ(), packet.getPower(), packet.getToBlow());
        explosion.finalizeExplosion(true);

        this.minecraft.player.setDeltaMovement(
                this.minecraft.player.getDeltaMovement().add(knockback.x, knockback.y, knockback.z)
        );
    }
}