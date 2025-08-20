package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.access.IGravityLivingEntity;
import org.weever.gravitymod.util.GravityAPI;

@Mixin(MobEntity.class)
public abstract class GravityMobMixin extends LivingEntity {

    @Shadow protected abstract float rotlerp(float f, float g, float h);

    @Inject(
            method = "doHurtTarget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"
            )
    )
    private void gravitymod$doHurtTarget(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN)
            return;

        if (target instanceof LivingEntity) {
            ((IGravityLivingEntity)target).gravitymod$augmentKB(this);
        }
    }

    @Inject(
            method = "lookAt",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void gravitymod$lookat(Entity $$0, float $$1, float $$2, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection($$0);
        Direction gravityDirection2 = GravityAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN && gravityDirection2 == Direction.DOWN)
            return;
        ci.cancel();

        double $$3 = $$0.getEyePosition(1.0F).x - this.getEyePosition(1.0F).x;
        double $$4 = $$0.getEyePosition(1.0F).z - this.getEyePosition(1.0F).z;
        double $$6;
        if ($$0 instanceof LivingEntity) {
            $$6 = $$0.getEyePosition(1.0F).y - this.getEyePosition(1.0F).y;
        } else {
            $$6 = ($$0.getBoundingBox().minY + $$0.getBoundingBox().maxY) / 2.0 - getEyeY();
        }

        double $$8 = Math.sqrt($$3 * $$3 + $$4 * $$4);
        float $$9 = (float)(MathHelper.atan2($$4, $$3) * 180.0F / (float)Math.PI) - 90.0F;
        float $$10 = (float)(-(MathHelper.atan2($$6, $$8) * 180.0F / (float)Math.PI));
        this.xRot = (this.rotlerp(this.xRot, $$10, $$2));
        this.yRot = (this.rotlerp(this.yRot, $$9, $$1));
    }

    protected GravityMobMixin(EntityType<? extends LivingEntity> $$0, World $$1) {
        super($$0, $$1);
    }
}
