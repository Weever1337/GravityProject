package org.weever.gravitymod.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import org.weever.gravitymod.api.GravityDirection;

public final class GravityUtil {
    public static Quaternion getWorldRotation(GravityDirection direction) {
        switch (direction) {
            case UP:
                return Vector3f.ZP.rotationDegrees(180.0F);
            case EAST:
                return Vector3f.ZP.rotationDegrees(90.0F);
            case WEST:
                return Vector3f.ZP.rotationDegrees(-90.0F);
            case DOWN:
            default:
                return Quaternion.ONE.copy();
        }
    }

    public static BlockPos getBlockPosRotation(GravityDirection direction, BlockPos originalBlockPos) {
        switch (direction) {
            case UP:
                return new BlockPos(originalBlockPos.getX(), originalBlockPos.getY() + 1, originalBlockPos.getZ());
            case WEST:
                return new BlockPos(originalBlockPos.getX() - 1, originalBlockPos.getY(), originalBlockPos.getZ());
            case EAST:
                return new BlockPos(originalBlockPos.getX() + 1, originalBlockPos.getY(), originalBlockPos.getZ());
            default:
                return originalBlockPos;
        }
    }
}