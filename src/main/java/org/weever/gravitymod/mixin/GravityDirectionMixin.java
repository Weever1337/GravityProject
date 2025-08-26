package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import org.weever.gravitymod.v1_20_1.util.Mth;
import net.minecraft.util.math.vector.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(value = Direction.class, priority = 1001)
public abstract class GravityDirectionMixin {
    @Inject(method = "orderedByNearest", at = @At("HEAD"), cancellable = true)
    private static void gravity_orderedByNearest(Entity pEntity, CallbackInfoReturnable<Direction[]> cir) {
        Direction gravityDirection = GravityAPI.getGravityDirection(pEntity);
        if (gravityDirection == Direction.DOWN) {
            return;
        }

        Vector2f newRots = RotationUtil.rotPlayerToWorld(pEntity.yRot, pEntity.xRot, gravityDirection);
        float newPitch = newRots.y;
        float newYaw = newRots.x;

        float f = newPitch * ((float)Math.PI / 180F);
        float f1 = -newYaw * ((float)Math.PI / 180F);
        float f2 = Mth.sin(f);
        float f3 = Mth.cos(f);
        float f4 = Mth.sin(f1);
        float f5 = Mth.cos(f1);
        boolean flag = f4 > 0.0F;
        boolean flag1 = f2 < 0.0F;
        boolean flag2 = f5 > 0.0F;
        float f6 = flag ? f4 : -f4;
        float f7 = flag1 ? -f2 : f2;
        float f8 = flag2 ? f5 : -f5;
        float f9 = f6 * f3;
        float f10 = f8 * f3;
        Direction direction = flag ? Direction.EAST : Direction.WEST;
        Direction direction1 = flag1 ? Direction.UP : Direction.DOWN;
        Direction direction2 = flag2 ? Direction.SOUTH : Direction.NORTH;

        Direction[] result;
        if (f6 > f8) {
            if (f7 > f9) {
                result = makeDirectionArray(direction1, direction, direction2);
            } else {
                result = f10 > f7 ? makeDirectionArray(direction, direction2, direction1) : makeDirectionArray(direction, direction1, direction2);
            }
        } else if (f7 > f10) {
            result = makeDirectionArray(direction1, direction2, direction);
        } else {
            result = f9 > f7 ? makeDirectionArray(direction2, direction, direction1) : makeDirectionArray(direction2, direction1, direction);
        }

        cir.setReturnValue(result);
    }

    private static Direction[] makeDirectionArray(Direction pFirst, Direction pSecond, Direction pThird) {
        return new Direction[]{pFirst, pSecond, pThird, pThird.getOpposite(), pSecond.getOpposite(), pFirst.getOpposite()};
    }
}