package org.weever.gravitymod.mixin;

import net.minecraft.client.audio.SoundSource;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(CrossbowItem.class)
public abstract class GravityCrossbowItemMixin extends ShootableItem implements IVanishable {
    @Shadow
    private static AbstractArrowEntity getArrow(World level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
        return null;
    }

    public GravityCrossbowItemMixin(Properties $$0) {
        super($$0);
    }

    @Inject(
            method = "shootProjectile",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void redirect_shoot_getX_0(World $$0, LivingEntity $$1, Hand $$2, ItemStack $$3, ItemStack $$4, float $$5, boolean $$6, float $$7, float $$8, float $$9, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection($$1);
        if (gravityDirection == Direction.DOWN)
            return;

        ci.cancel();

        if (!$$0.isClientSide) {
            boolean $$10 = $$4.getItem() == Items.FIREWORK_ROCKET;
            ProjectileEntity $$11;
            if ($$10) {
                $$11 = new FireworkRocketEntity($$0, $$4, $$1,
                        $$1.getEyePosition(1).subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.15000000596046448D, 0.0D, gravityDirection)).x,
                        $$1.getEyePosition(1).subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.15000000596046448D, 0.0D, gravityDirection)).y + 0.15000000596046448D - 0.15F,
                        $$1.getEyePosition(1).subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.15000000596046448D, 0.0D, gravityDirection)).z,
                        true);
            } else {
                $$11 = getArrow($$0, $$1, $$3, $$4);
                if ($$6 || $$9 != 0.0F) {
                    ((AbstractArrowEntity)$$11).pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                }
            }

            if ($$1 instanceof ICrossbowUser) {
                ICrossbowUser crossbowUser = (ICrossbowUser) $$1;
                crossbowUser.shootCrossbowProjectile(crossbowUser.getTarget(), $$3, $$11, $$9);
            } else {
                Vector3d $$14 = $$1.getUpVector(1.0F);
                Quaternion $$15 = new Quaternion(new Vector3f((float)$$14.x, (float)$$14.y, (float)$$14.z), $$9, true);
                Vector3d $$16 = $$1.getViewVector(1.0F);
                Vector3f $$17 = new Vector3f((float)$$16.x, (float)$$16.y, (float)$$16.z);
                $$17.transform($$15);
                $$11.shoot($$17.x(), $$17.y(), $$17.z(), $$7, $$8);
            }

            $$3.hurtAndBreak($$10 ? 3 : 1, $$1, $$1x -> $$1x.broadcastBreakEvent($$2));
            $$0.addFreshEntity($$11);
            $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, $$5);
        }
    }
}
