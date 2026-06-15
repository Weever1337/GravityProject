package org.weever.gravitymod.compat.presencefootsteps.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.weever.gravitymod.compat.ClassDependentMixin;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

@Pseudo
@ClassDependentMixin("eu.ha3.presencefootsteps.sound.generator.BipedalStepSoundGenerator")
@Mixin(targets = "eu.ha3.presencefootsteps.sound.generator.BipedalStepSoundGenerator", remap = false)
public abstract class PFBipedalStepSoundGeneratorMixin {
    @Redirect(
            method = "simulateBrushes(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/util/math/BlockPos;")
    )
    private BlockPos gravitymod$rotateBrushPosToGravity(double origX, double origY, double origZ, LivingEntity ply) {
        Direction gravity = GravityAPI.getGravityDirection(ply);
        if (gravity == Direction.DOWN) {
            return new BlockPos(origX, origY, origZ);
        }

        Vector3d feet = ply.position();
        double downDistance = feet.y - origY;

        Vector3d worldOffset = RotationUtil.vecPlayerToWorld(0.0D, -downDistance, 0.0D, gravity);
        Vector3d brushPos = feet.add(worldOffset);

        return new BlockPos(brushPos.x, brushPos.y, brushPos.z);
    }
}