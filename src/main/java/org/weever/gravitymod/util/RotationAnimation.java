package org.weever.gravitymod.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.Validate;

public class RotationAnimation {
    private boolean inAnimation = false;
    private Quaternion startGravityRotation;
    private Quaternion endGravityRotation;
    private Vector3d relativeRotationCenter = Vector3d.ZERO;

    private long startTimeMs;
    private long endTimeMs;

    public void startRotationAnimation(
            Direction newGravity, Direction prevGravity,
            long durationTimeMs, Entity entity, long timeMs,
            boolean rotateView, Vector3d relativeRotationCenter
    ) {
        if (durationTimeMs == 0) {
            inAnimation = false;
            return;
        }

        Validate.notNull(entity);

        Vector3d newLookingDirection = getNewLookingDirection(newGravity, prevGravity, entity, rotateView);

        Quaternion oldViewRotation = QuaternionUtil.getViewRotation(entity.xRot, entity.yRot);

        update(timeMs);
        Quaternion currentAnimatedGravityRotation = getCurrentGravityRotation(prevGravity, timeMs);

        // camera rotation = view rotation(pitch and yaw) * gravity rotation(animated)
        Quaternion currentAnimatedCameraRotation = oldViewRotation.copy();
        currentAnimatedCameraRotation.mul(currentAnimatedGravityRotation);

        Quaternion newEndGravityRotation = RotationUtil.getWorldRotationQuaternion(newGravity);

        Vector2f newYawAndPitch = RotationUtil.vecToRot(
                RotationUtil.vecWorldToPlayer(newLookingDirection, newGravity)
        );
        float newPitch = newYawAndPitch.y;
        float newYaw = newYawAndPitch.x;
        float deltaYaw = newYaw - entity.yRot;
        float deltaPitch = newPitch - entity.xRot;
        entity.yRot = entity.yRot + deltaYaw;
        entity.xRot = entity.xRot + deltaPitch;
        entity.yRotO += deltaYaw;
        entity.xRotO += deltaPitch;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.yBodyRot += deltaYaw;
            livingEntity.yBodyRotO += deltaYaw;
            livingEntity.yHeadRot += deltaYaw;
            livingEntity.yHeadRotO += deltaYaw;
        }

        Quaternion newViewRotation = QuaternionUtil.getViewRotation(entity.xRot, entity.yRot);

        // gravity rotation = (view rotation^-1) * camera rotation
        Quaternion animationStartGravityRotation = newViewRotation.copy();
        animationStartGravityRotation.conj();
        animationStartGravityRotation.mul(currentAnimatedCameraRotation);

        this.relativeRotationCenter = relativeRotationCenter;
        inAnimation = true;
        startGravityRotation = animationStartGravityRotation;
        endGravityRotation = newEndGravityRotation;
        startTimeMs = timeMs;
        endTimeMs = timeMs + durationTimeMs;
    }

    private Vector3d getNewLookingDirection(
            Direction newGravity, Direction prevGravity, Entity player,
            boolean rotateView
    ) {
        Vector3d oldLookingDirection = RotationUtil.vecPlayerToWorld(
                RotationUtil.rotToVec(player.yRot, player.xRot),
                prevGravity
        );

        if (!rotateView) {
            return oldLookingDirection;
        }

        if (newGravity == prevGravity.getOpposite()) {
            return oldLookingDirection.scale(-1.0D);
        }

        Quaternion deltaRotation = QuaternionUtil.getRotationBetween(
                Vector3d.atLowerCornerOf(prevGravity.getNormal()),
                Vector3d.atLowerCornerOf(newGravity.getNormal())
        );

        Vector3f lookingDirection = new Vector3f((float) oldLookingDirection.x, (float) oldLookingDirection.y, (float) oldLookingDirection.z);
        lookingDirection.transform(deltaRotation);
        Vector3d newLookingDirection = new Vector3d(lookingDirection.x(), lookingDirection.y(), lookingDirection.z());
        return newLookingDirection;
    }

    /**
     * It returns the rotation that applies to world for rendering.
     * To get the rotation that applies entity, conjugate it.
     */
    public Quaternion getCurrentGravityRotation(Direction currentGravity, long timeMs) {

        update(timeMs);

        if (!inAnimation) {
            return RotationUtil.getWorldRotationQuaternion(currentGravity);
        }

        double delta = (double) (timeMs - startTimeMs) / (endTimeMs - startTimeMs);

        return RotationUtil.interpolate(
                startGravityRotation, endGravityRotation,
                mapProgress((float) delta)
        );
    }

    public void update(long timeMs) {
        if (timeMs > endTimeMs) {
            inAnimation = false;
        }
    }

    /**
     * When doing gravity flipping, the rotation center is the player bounding box center.
     * But the player feet pos changes abruptly. So we need special calculation to eye offset.
     *
     * Note when rotateView is false, it will cause non-smooth eye offset change
     */
    public Vector3d getEyeOffset(
            Quaternion gravityRot, Vector3d localEyeOffset, Direction newGravity
    ) {
        Quaternion gravityRotForEntity = gravityRot.copy();
        gravityRotForEntity.conj();

        if (!inAnimation || relativeRotationCenter.equals(Vector3d.ZERO)) {
            return QuaternionUtil.rotate(localEyeOffset, gravityRotForEntity);
        }

        Vector3d rotationCenterOffset = RotationUtil.vecPlayerToWorld(relativeRotationCenter, newGravity);

        Vector3d eyeOffsetFromRotationCenter = localEyeOffset.subtract(relativeRotationCenter);
        Vector3d rotatedEyeOffsetFromRotationCenter =
                QuaternionUtil.rotate(eyeOffsetFromRotationCenter, gravityRotForEntity);

        return rotationCenterOffset.add(rotatedEyeOffsetFromRotationCenter);
    }

    private static float mapProgress(float delta) {
        return MathHelper.clamp((delta * delta * (3 - 2 * delta)), 0, 1);
    }

    public boolean isInAnimation() {
        return inAnimation;
    }
}