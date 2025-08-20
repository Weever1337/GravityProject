package org.weever.gravitymod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.ProjectileUtil;

@Mixin(AbstractSkeletonEntity.class)
public abstract class GravityAbstractSkeletonMixin extends MobEntity implements IRangedAttackMob {
    @Shadow protected abstract AbstractArrowEntity getArrow(ItemStack itemStack, float f);

    protected GravityAbstractSkeletonMixin(EntityType<? extends MobEntity> $$0, World $$1) {
        super($$0, $$1);
    }

    @Inject(
            method = "performRangedAttack",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$performRangedAttack(LivingEntity target, float $$1, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();

        ItemStack $$2 = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrowEntity $$3 = this.getArrow($$2, $$1);
        double $$4 = target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection)).x - this.getX();
        double $$5 = target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection)).y - $$3.getY();
        double $$6 = target.position().add(RotationUtil.vecPlayerToWorld(0.0D, target.getBbHeight() * 0.3333333333333333D, 0.0D, gravityDirection)).z - this.getZ();
        double $$7 = Math.sqrt(Math.sqrt($$4 * $$4 + $$6 * $$6));
        $$3.shoot($$4, $$5 + $$7 * 0.2F, $$6, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity($$3);
    }
}
