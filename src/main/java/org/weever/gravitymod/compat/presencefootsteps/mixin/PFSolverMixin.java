package org.weever.gravitymod.compat.presencefootsteps.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.weever.gravitymod.compat.ClassDependentMixin;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;
import org.weever.gravitymod.v1_20_1.util.Mth;

@Pseudo
@ClassDependentMixin("eu.ha3.presencefootsteps.world.PFSolver")
@Mixin(targets = "eu.ha3.presencefootsteps.world.PFSolver", remap = false)
public abstract class PFSolverMixin {
    private static final double GRAVITYMOD$TRAP_DOOR_OFFSET = 0.1D;

    @Redirect(
            method = "findAssociation(Lnet/minecraft/entity/Entity;DZ)Leu/ha3/presencefootsteps/world/Association;",
            at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/util/math/BlockPos;")
    )
    private BlockPos gravitymod$rotateFootPosToGravity(double origX, double origY, double origZ,
                                                       Entity ply, double verticalOffsetAsMinus, boolean isRightFoot) {
        Direction gravity = GravityAPI.getGravityDirection(ply);
        if (gravity == Direction.DOWN) {
            return new BlockPos(origX, origY, origZ);
        }

        double rot = Math.toRadians(Mth.wrapDegrees((double) ply.yRot));
        double feetDistanceToCenter = 0.2D * (isRightFoot ? -1 : 1);

        double localX = Math.cos(rot) * feetDistanceToCenter;
        double localY = -(GRAVITYMOD$TRAP_DOOR_OFFSET + verticalOffsetAsMinus);
        double localZ = Math.sin(rot) * feetDistanceToCenter;

        Vector3d worldOffset = RotationUtil.vecPlayerToWorld(localX, localY, localZ, gravity);
        Vector3d footPos = ply.position().add(worldOffset);

        return new BlockPos(footPos.x, footPos.y, footPos.z);
    }

    @Redirect(
            method = "findAssociation(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)Leu/ha3/presencefootsteps/world/Association;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;",
                    remap = true
            )
    )
    private AxisAlignedBB gravitymod$gravityAwareCollider(Entity player) {
        AxisAlignedBB box = player.getBoundingBox();
        if (GravityAPI.getGravityDirection(player) == Direction.DOWN) {
            return box;
        }
        return box.inflate(1.0D);
    }
}