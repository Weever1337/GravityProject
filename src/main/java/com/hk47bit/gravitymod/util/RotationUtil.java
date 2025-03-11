package com.hk47bit.gravitymod.util;

import com.hk47bit.gravitymod.api.GravityDirection;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class RotationUtil {

    private static final Map<GravityDirection, Integer> directionToIndex = new HashMap<>();
    static {
        directionToIndex.put(GravityDirection.DOWN, 0);
        directionToIndex.put(GravityDirection.UP, 1);
        directionToIndex.put(GravityDirection.EAST, 2);
        directionToIndex.put(GravityDirection.WEST, 3);
    }

    private static int getDirectionIndex(GravityDirection direction) {
        return directionToIndex.get(direction);
    }

    private static final Direction[][] DIR_WORLD_TO_PLAYER = new Direction[4][];
    static {
        for(GravityDirection gravityDirection : GravityDirection.values()) {
            DIR_WORLD_TO_PLAYER[getDirectionIndex(gravityDirection)] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vector3d directionVector = new Vector3d(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
                directionVector = RotationUtil.vectorWorldToPlayer(directionVector, gravityDirection);
                DIR_WORLD_TO_PLAYER[getDirectionIndex(gravityDirection)][direction.ordinal()] = Direction.getNearest(directionVector.x, directionVector.y, directionVector.z);
            }
        }
    }

    public static Direction dirWorldToPlayer(Direction direction, GravityDirection gravityDirection) {
        return DIR_WORLD_TO_PLAYER[getDirectionIndex(gravityDirection)][direction.ordinal()];
    }

    private static final Direction[][] DIR_PLAYER_TO_WORLD = new Direction[4][];
    static {
        for(GravityDirection gravityDirection : GravityDirection.values()) {
            DIR_PLAYER_TO_WORLD[getDirectionIndex(gravityDirection)] = new Direction[6];
            for(Direction direction : Direction.values()) {
                Vector3d directionVector = new Vector3d(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
                directionVector = RotationUtil.vectorPlayerToWorld(directionVector, gravityDirection);
                DIR_PLAYER_TO_WORLD[getDirectionIndex(gravityDirection)][direction.ordinal()] = Direction.getNearest(directionVector.x, directionVector.y, directionVector.z);
            }
        }
    }

    public static Direction dirPlayerToWorld(Direction direction, GravityDirection gravityDirection) {
        return DIR_PLAYER_TO_WORLD[getDirectionIndex(gravityDirection)][direction.ordinal()];
    }

    public static Vector3d vectorWorldToPlayer(double x, double y, double z, GravityDirection gravityDirection) {
        switch(gravityDirection) {
            case UP:    return new Vector3d(-x, -y,  z);
            case WEST:  return new Vector3d(-z,  x, -y);
            case EAST:  return new Vector3d( z, -x, -y);
            default:    return new Vector3d( x,  y,  z);
        }
    }

    public static Vector3d vectorWorldToPlayer(Vector3d vector3d, GravityDirection gravityDirection) {
        return vectorWorldToPlayer(vector3d.x, vector3d.y, vector3d.z, gravityDirection);
    }

    public static Vector3d vectorPlayerToWorld(double x, double y, double z, GravityDirection gravityDirection) {
        switch(gravityDirection) {
            case UP:    return new Vector3d(-x, -y,  z);
            case WEST:  return new Vector3d( y, -z, -x);
            case EAST:  return new Vector3d(-y, -z,  x);
            default:    return new Vector3d( x,  y,  z);
        }
    }

    public static Vector3d vectorPlayerToWorld(Vector3d vector3d, GravityDirection gravityDirection) {
        return vectorPlayerToWorld(vector3d.x, vector3d.y, vector3d.z, gravityDirection);
    }

    public static Vector3f vectorWorldToPlayer(float x, float y, float z, GravityDirection gravityDirection) {
        switch(gravityDirection) {
            case UP:    return new Vector3f(-x, -y,  z);
            case WEST:  return new Vector3f(-z,  x, -y);
            case EAST:  return new Vector3f( z, -x, -y);
            default:    return new Vector3f( x,  y,  z);
        }
    }

    public static Vector3f vectorWorldToPlayer(Vector3f vector3f, GravityDirection gravityDirection) {
        return vectorWorldToPlayer(vector3f.x(), vector3f.y(), vector3f.z(), gravityDirection);
    }

    public static Vector3f vectorPlayerToWorld(float x, float y, float z, GravityDirection gravityDirection) {
        switch(gravityDirection) {
            case UP:    return new Vector3f(-x, -y,  z);
            case WEST:  return new Vector3f( y, -z, -x);
            case EAST:  return new Vector3f(-y, -z,  x);
            default:    return new Vector3f( x,  y,  z);
        }
    }

    public static Vector3f vectorPlayerToWorld(Vector3f vector3f, GravityDirection gravityDirection) {
        return vectorPlayerToWorld(vector3f.x(), vector3f.y(), vector3f.z(), gravityDirection);
    }

    public static Vector3d maskWorldToPlayer(double x, double y, double z, GravityDirection gravityDirection) {
        switch(gravityDirection) {
            case WEST: case EAST:  return new Vector3d(z, x, y);
            default:    return new Vector3d(x, y, z);
        }
    }

    public static Vector3d maskWorldToPlayer(Vector3d vector3d, GravityDirection gravityDirection) {
        return maskWorldToPlayer(vector3d.x, vector3d.y, vector3d.z, gravityDirection);
    }

    public static Vector3d maskPlayerToWorld(double x, double y, double z, GravityDirection gravityDirection) {
        switch(gravityDirection) {
            case WEST: case EAST:  return new Vector3d(y, z, x);
            default:    return new Vector3d(x, y, z);
        }
    }

    public static Vector3d maskPlayerToWorld(Vector3d vector3d, GravityDirection gravityDirection) {
        return maskPlayerToWorld(vector3d.x, vector3d.y, vector3d.z, gravityDirection);
    }

    public static AxisAlignedBB boxWorldToPlayer(AxisAlignedBB box, GravityDirection gravityDirection) {
        return new AxisAlignedBB(
                RotationUtil.vectorWorldToPlayer(box.minX, box.minY, box.minZ, gravityDirection),
                RotationUtil.vectorWorldToPlayer(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static AxisAlignedBB boxPlayerToWorld(AxisAlignedBB box, GravityDirection gravityDirection) {
        return new AxisAlignedBB(
                RotationUtil.vectorPlayerToWorld(box.minX, box.minY, box.minZ, gravityDirection),
                RotationUtil.vectorPlayerToWorld(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static Vector2f rotWorldToPlayer(float yaw, float pitch, GravityDirection gravityDirection) {
        Vector3d vector3d = RotationUtil.vectorWorldToPlayer(rotToVector(yaw, pitch), gravityDirection);
        return vectorToRot(vector3d.x, vector3d.y, vector3d.z);
    }

    public static Vector2f rotWorldToPlayer(Vector2f vector2f, GravityDirection gravityDirection) {
        return rotWorldToPlayer(vector2f.x, vector2f.y, gravityDirection);
    }

    public static Vector2f rotPlayerToWorld(float yaw, float pitch, GravityDirection gravityDirection) {
        Vector3d vector3d = RotationUtil.vectorPlayerToWorld(rotToVector(yaw, pitch), gravityDirection);
        return vectorToRot(vector3d.x, vector3d.y, vector3d.z);
    }

    public static Vector2f rotPlayerToWorld(Vector2f vector2f, GravityDirection gravityDirection) {
        return rotPlayerToWorld(vector2f.x, vector2f.y, gravityDirection);
    }

    private static Vector3d rotToVector(float yaw, float pitch) {
        double radPitch = pitch * 0.017453292;
        double radNegYaw = -yaw * 0.017453292;
        double cosNegYaw = Math.cos(radNegYaw);
        double sinNegYaw = Math.sin(radNegYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);
        return new Vector3d(sinNegYaw * cosPitch, -sinPitch, cosNegYaw * cosPitch);
    }

    private static Vector2f vectorToRot(double x, double y, double z) {
        double sinPitch = -y;
        double radPitch = Math.asin(sinPitch);
        double cosPitch = Math.cos(radPitch);
        double sinNegYaw = x / cosPitch;
        double cosNegYaw = MathHelper.clamp(z / cosPitch, -1, 1);
        double radNegYaw = Math.acos(cosNegYaw);
        if(sinNegYaw < 0) radNegYaw = Math.PI * 2 - radNegYaw;

        return new Vector2f(MathHelper.wrapDegrees((float)(-radNegYaw) / 0.017453292F), (float)(radPitch) / 0.017453292F);
    }

    private static final Quaternion[] WORLD_ROTATION_QUATERNIONS = new Quaternion[4];
    static {
        WORLD_ROTATION_QUATERNIONS[0] = Quaternion.ONE.copy();
        WORLD_ROTATION_QUATERNIONS[1] = Vector3f.ZP.rotationDegrees(-180);
        WORLD_ROTATION_QUATERNIONS[2] = Vector3f.XP.rotationDegrees(-90);
        WORLD_ROTATION_QUATERNIONS[3] = Vector3f.XP.rotationDegrees(90);
    }

    public static Quaternion getWorldRotationQuaternion(GravityDirection gravityDirection) {
        return WORLD_ROTATION_QUATERNIONS[getDirectionIndex(gravityDirection)];
    }

    private static final Quaternion[] ENTITY_ROTATION_QUATERNIONS = new Quaternion[4];
    static {
        ENTITY_ROTATION_QUATERNIONS[0] = Quaternion.ONE;
        ENTITY_ROTATION_QUATERNIONS[1] = Vector3f.ZP.rotationDegrees(180);
        ENTITY_ROTATION_QUATERNIONS[2] = Vector3f.XP.rotationDegrees(90);
        ENTITY_ROTATION_QUATERNIONS[3] = Vector3f.XP.rotationDegrees(-90);
    }

    public static Quaternion getCameraRotationQuaternion(GravityDirection gravityDirection) {
        return ENTITY_ROTATION_QUATERNIONS[getDirectionIndex(gravityDirection)];
    }
}