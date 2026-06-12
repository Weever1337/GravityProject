package org.weever.gravitymod.mixin.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.access.IGravityEntity;
import org.weever.gravitymod.util.GravityAPI;

@Mixin(BrainUtil.class)
public class GravityBrainUtilMixin {
    @Inject(
            method = "throwItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/vector/Vector3d;)V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void gravitymod$throwItem(LivingEntity pLivingEntity, ItemStack pStack, Vector3d pOffset, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(pLivingEntity);
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();

        double yOffset = pOffset.y;
        Vector3d eyeOffset = GravityAPI.getEyeOffset(pLivingEntity);
        Vector3d offset = eyeOffset.normalize().scale(yOffset);
        Vector3d itemPos = pLivingEntity.position().add(eyeOffset).subtract(offset);
        ItemEntity itemEntity = new ItemEntity(
                pLivingEntity.level, itemPos.x(), itemPos.y(), itemPos.z(), pStack
        );

        ((IGravityEntity) itemEntity).gravitymod$setBaseGravityDirection(
                GravityAPI.getGravityDirection(pLivingEntity)
        );

        itemEntity.setThrower(pLivingEntity.getUUID());
        Vector3d throwVec = pOffset.subtract(pLivingEntity.position());
        throwVec = throwVec.normalize().multiply(pOffset.x, pOffset.y, pOffset.z);
        GravityAPI.setWorldVelocity(itemEntity, throwVec);
        itemEntity.setNoPickUpDelay();
        pLivingEntity.level.addFreshEntity(itemEntity);
    }
}
