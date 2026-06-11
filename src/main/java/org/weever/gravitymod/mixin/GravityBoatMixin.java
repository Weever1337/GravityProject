package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(BoatEntity.class)
public abstract class GravityBoatMixin extends Entity {
    @Shadow
    private float deltaRotation;

    @Shadow
    protected abstract void clampRotation(Entity entity);

    public GravityBoatMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "positionRider",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$positionRider(Entity passenger, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) return;

        ci.cancel();
        if (this.hasPassenger(passenger)) {
            float seatShift = 0.0F;
            float lift = (float) ((this.removed ? 0.01D : this.getPassengersRidingOffset()) + passenger.getMyRidingOffset());
            if (this.getPassengers().size() > 1) {
                int seatIndex = this.getPassengers().indexOf(passenger);
                seatShift = seatIndex == 0 ? 0.2F : -0.6F;
                if (passenger instanceof AnimalEntity) {
                    seatShift += 0.2F;
                }
            }

            Vector3d localOffset = new Vector3d(seatShift, 0.0D, 0.0D)
                    .yRot(-this.yRot * ((float) Math.PI / 180F) - ((float) Math.PI / 2F))
                    .add(0.0D, lift, 0.0D);
            Vector3d worldOffset = RotationUtil.vecPlayerToWorld(localOffset, gravityDirection);
            passenger.setPos(this.getX() + worldOffset.x, this.getY() + worldOffset.y, this.getZ() + worldOffset.z);

            passenger.yRot += this.deltaRotation;
            passenger.setYHeadRot(passenger.getYHeadRot() + this.deltaRotation);
            this.clampRotation(passenger);
            if (passenger instanceof AnimalEntity && this.getPassengers().size() > 1) {
                int bodyRotation = passenger.getId() % 2 == 0 ? 90 : 270;
                passenger.setYBodyRot(((AnimalEntity) passenger).yBodyRot + (float) bodyRotation);
                passenger.setYHeadRot(passenger.getYHeadRot() + (float) bodyRotation);
            }
        }
    }

    @Inject(
            method = "getDismountLocationForPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$getDismountLocation(LivingEntity passenger, CallbackInfoReturnable<Vector3d> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) return;

        AxisAlignedBB localBox = RotationUtil.boxWorldToPlayer(getBoundingBox(), gravityDirection);
        Vector3d localPos = RotationUtil.vecWorldToPlayer(position(), gravityDirection);
        cir.setReturnValue(RotationUtil.vecPlayerToWorld(localPos.x, localBox.maxY, localPos.z, gravityDirection));
    }
}
