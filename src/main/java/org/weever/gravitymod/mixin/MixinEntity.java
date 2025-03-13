package org.weever.gravitymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity {
//    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
//    public void move(MoverType type, Vector3d p_213315_2_, CallbackInfo ci) {
//        Entity entity = (Entity) (Object) this;
//        GravityDirection gravityDirection = GravityAPI.getGravityDirection(entity);
//
//        if (gravityDirection != GravityDirection.DOWN) {
//            System.out.println(gravityDirection + " " + entity.getName().getString());
//            Vector3d transformedMovement = RotationUtil.vectorWorldToPlayer(p_213315_2_, gravityDirection);
//            ((Entity) (Object) this).setDeltaMovement(transformedMovement);
////            ((Entity) (Object) this).move(type, transformedMovement);
//            ci.cancel();
//        }
//    }
}