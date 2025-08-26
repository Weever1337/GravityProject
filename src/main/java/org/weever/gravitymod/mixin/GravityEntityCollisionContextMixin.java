package org.weever.gravitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Mixin(EntitySelectionContext.class)
public abstract class GravityEntityCollisionContextMixin {
    @Inject(
            method = "isAbove",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gravitymod$isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue, CallbackInfoReturnable<Boolean> cir) {
        if (this.entity == null) return;

        Direction gravityDirection = GravityAPI.getGravityDirection(this.entity);
        if (gravityDirection == Direction.DOWN) return;

        double realBottom = RotationUtil.boxWorldToPlayer(entity.getBoundingBox(), gravityDirection).minY;

        if (shape.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        AxisAlignedBB shapeBox = RotationUtil.boxWorldToPlayer(
                shape.bounds().inflate(-9.999999747378752E-6D), gravityDirection
        );
        AxisAlignedBB posBox = RotationUtil.boxWorldToPlayer(new AxisAlignedBB(pos), gravityDirection);
        cir.setReturnValue(
                realBottom > posBox.minY + shapeBox.maxX
        );
    }


    /**Shadows, ignore
     * -------------------------------------------------------------------------------------------------------------
     * */
    @Shadow @Final
    private Entity entity;

    @Shadow @Final
    private double entityBottom;
}
