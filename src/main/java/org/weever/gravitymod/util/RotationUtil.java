package org.weever.gravitymod.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import org.weever.gravitymod.v1_20_1.util.Mth;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.v1_20_1.util.Mth;

//https://github.com/qouteall/GravityChanger/tree/1.20.1-Fabric/src/main/java/gravity_changer/util
//credit to quoteall

public abstract class RotationUtil {
    private static final Direction[][] DIR_WORLD_TO_PLAYER = new Direction[6][];

    static {
        for (Direction gravityDirection : Direction.values()) {
            DIR_WORLD_TO_PLAYER[gravityDirection.get3DDataValue()] = new Direction[6];
            for (Direction direction : Direction.values()) {
                Vector3d directionVector = Vector3d.atLowerCornerOf(direction.getNormal());
                directionVector = RotationUtil.vecWorldToPlayer(directionVector, gravityDirection);
                DIR_WORLD_TO_PLAYER[gravityDirection.get3DDataValue()][direction.get3DDataValue()] =
                        Direction.getNearest(directionVector.x, directionVector.y, directionVector.z);
            }
        }
    }

    public static Direction dirWorldToPlayer(Direction direction, Direction gravityDirection) {
        return DIR_WORLD_TO_PLAYER[gravityDirection.get3DDataValue()][direction.get3DDataValue()];
    }

    private static final Direction[][] DIR_PLAYER_TO_WORLD = new Direction[6][];

    static {
        for (Direction gravityDirection : Direction.values()) {
            DIR_PLAYER_TO_WORLD[gravityDirection.get3DDataValue()] = new Direction[6];
            for (Direction direction : Direction.values()) {
                Vector3d directionVector = Vector3d.atLowerCornerOf(direction.getNormal());
                directionVector = RotationUtil.vecPlayerToWorld(directionVector, gravityDirection);
                DIR_PLAYER_TO_WORLD[gravityDirection.get3DDataValue()][direction.get3DDataValue()] =
                        Direction.getNearest(directionVector.x, directionVector.y, directionVector.z);
            }
        }
    }

    public static Direction dirPlayerToWorld(Direction direction, Direction gravityDirection) {
        return DIR_PLAYER_TO_WORLD[gravityDirection.get3DDataValue()][direction.get3DDataValue()];
    }

    public static Vector3d vecWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        switch (gravityDirection) {
            case DOWN:
                return new Vector3d(x, y, z);
            case UP:
                return new Vector3d(-x, -y, z);
            case NORTH:
                return new Vector3d(x, z, -y);
            case SOUTH:
                return new Vector3d(-x, -z, -y);
            case WEST:
                return new Vector3d(-z, x, -y);
            case EAST:
                return new Vector3d(z, -x, -y);

            default:
                throw new IllegalStateException("Unexpected value: " + gravityDirection);
        }
    }

    public static Vector3d vecWorldToPlayer(Vector3d Vector3dd, Direction gravityDirection) {
        return vecWorldToPlayer(Vector3dd.x, Vector3dd.y, Vector3dd.z, gravityDirection);
    }

    public static Vector3d vecPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        switch (gravityDirection) {
            case DOWN:
                return new Vector3d(x, y, z);
            case UP:
                return new Vector3d(-x, -y, z);
            case NORTH:
                return new Vector3d(x, -z, y);
            case SOUTH:
                return new Vector3d(-x, -z, -y);
            case WEST:
                return new Vector3d(y, -z, -x);
            case EAST:
                return new Vector3d(-y, -z, x);
            default:
                throw new IllegalStateException("Unexpected value: " + gravityDirection);
        }
    }

    public static Vector3d vecPlayerToWorld(Vector3d Vector3dd, Direction gravityDirection) {
        return vecPlayerToWorld(Vector3dd.x, Vector3dd.y, Vector3dd.z, gravityDirection);
    }

    public static Vector3f vecWorldToPlayer(float x, float y, float z, Direction gravityDirection) {
        switch (gravityDirection) {
            case DOWN:
                return new Vector3f(x, y, z);
            case UP:
                return new Vector3f(-x, -y, z);
            case NORTH:
                return new Vector3f(x, z, -y);
            case SOUTH:
                return new Vector3f(-x, -z, -y);
            case WEST:
                return new Vector3f(-z, x, -y);
            case EAST:
                return new Vector3f(z, -x, -y);
            default:
                throw new IllegalStateException("Unexpected value: " + gravityDirection);
        }
    }

    public static Vector3f vecWorldToPlayer(Vector3f vector3F, Direction gravityDirection) {
        return vecWorldToPlayer(vector3F.x(), vector3F.y(), vector3F.z(), gravityDirection);
    }

    public static Vector3f vecPlayerToWorld(float x, float y, float z, Direction gravityDirection) {
        switch (gravityDirection) {
            case DOWN:
                return new Vector3f(x, y, z);
            case UP:
                return new Vector3f(-x, -y, z);
            case NORTH:
                return new Vector3f(x, -z, y);
            case SOUTH:
                return new Vector3f(-x, -z, -y);
            case WEST:
                return new Vector3f(y, -z, -x);
            case EAST:
                return new Vector3f(-y, -z, x);
            default:
                throw new IllegalStateException("Unexpected value: " + gravityDirection);
        }
    }

    public static Vector3f vecPlayerToWorld(Vector3f vector3F, Direction gravityDirection) {
        return vecPlayerToWorld(vector3F.x(), vector3F.y(), vector3F.z(), gravityDirection);
    }

    public static Vector3d maskWorldToPlayer(double x, double y, double z, Direction gravityDirection) {
        switch (gravityDirection) {
            case DOWN:
            case UP:
                return new Vector3d(x, y, z);
            case NORTH:
            case SOUTH:
                return new Vector3d(x, z, y);
            case WEST:
            case EAST:
                return new Vector3d(z, x, y);
            default:
                throw new IllegalStateException("Unexpected value: " + gravityDirection);
        }
    }

    public static Vector3d maskWorldToPlayer(Vector3d Vector3dd, Direction gravityDirection) {
        return maskWorldToPlayer(Vector3dd.x, Vector3dd.y, Vector3dd.z, gravityDirection);
    }

    public static Vector3d maskPlayerToWorld(double x, double y, double z, Direction gravityDirection) {
        switch (gravityDirection) {
            case DOWN:
            case UP:
                return new Vector3d(x, y, z);
            case NORTH:
            case SOUTH:
                return new Vector3d(x, z, y);
            case WEST:
            case EAST:
                return new Vector3d(y, z, x);
            default:
                throw new IllegalStateException("Unexpected value: " + gravityDirection);
        }
    }

    public static Vector3d maskPlayerToWorld(Vector3d Vector3dd, Direction gravityDirection) {
        return maskPlayerToWorld(Vector3dd.x, Vector3dd.y, Vector3dd.z, gravityDirection);
    }

    public static AxisAlignedBB boxWorldToPlayer(AxisAlignedBB box, Direction gravityDirection) {
        return new AxisAlignedBB(
                RotationUtil.vecWorldToPlayer(box.minX, box.minY, box.minZ, gravityDirection),
                RotationUtil.vecWorldToPlayer(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static AxisAlignedBB boxPlayerToWorld(AxisAlignedBB box, Direction gravityDirection) {
        return new AxisAlignedBB(
                RotationUtil.vecPlayerToWorld(box.minX, box.minY, box.minZ, gravityDirection),
                RotationUtil.vecPlayerToWorld(box.maxX, box.maxY, box.maxZ, gravityDirection)
        );
    }

    public static Vector2f rotWorldToPlayer(float yaw, float pitch, Direction gravityDirection) {
        Vector3d Vector3dd = RotationUtil.vecWorldToPlayer(rotToVec(yaw, pitch), gravityDirection);
        return vecToRot(Vector3dd.x, Vector3dd.y, Vector3dd.z);
    }

    public static Vector2f rotWorldToPlayer(Vector2f vec2f, Direction gravityDirection) {
        return rotWorldToPlayer(vec2f.x, vec2f.y, gravityDirection);
    }

    public static Vector2f rotPlayerToWorld(float yaw, float pitch, Direction gravityDirection) {
        Vector3d Vector3dd = RotationUtil.vecPlayerToWorld(rotToVec(yaw, pitch), gravityDirection);
        return vecToRot(Vector3dd.x, Vector3dd.y, Vector3dd.z);
    }

    public static Vector2f rotPlayerToWorld(Vector2f vec2f, Direction gravityDirection) {
        return rotPlayerToWorld(vec2f.x, vec2f.y, gravityDirection);
    }

    public static Vector3d rotToVec(float yaw, float pitch) {
        double radPitch = pitch * 0.017453292;
        double radNegYaw = -yaw * 0.017453292;
        double cosNegYaw = Math.cos(radNegYaw);
        double sinNegYaw = Math.sin(radNegYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);
        return new Vector3d(sinNegYaw * cosPitch, -sinPitch, cosNegYaw * cosPitch);
    }

    public static Vector2f vecToRot(double x, double y, double z) {
        double sinPitch = -y;
        double radPitch = Math.asin(sinPitch);
        double cosPitch = Math.cos(radPitch);
        double sinNegYaw = x / cosPitch;
        double cosNegYaw = Mth.clamp(z / cosPitch, -1, 1);
        double radNegYaw = Math.acos(cosNegYaw);
        if (sinNegYaw < 0) radNegYaw = Math.PI * 2 - radNegYaw;

        return new Vector2f(Mth.wrapDegrees((float) (-radNegYaw) / 0.017453292F), (float) (radPitch) / 0.017453292F);
    }

    public static Vector2f vecToRot(Vector3d Vector3dd) {
        return vecToRot(Vector3dd.x, Vector3dd.y, Vector3dd.z);
    }

    private static final Quaternion[] WORLD_ROTATION_QUATERNIONS = new Quaternion[6];

    static {
        WORLD_ROTATION_QUATERNIONS[0] = new Quaternion(0, 0, 0, 1);

        WORLD_ROTATION_QUATERNIONS[1] = new Quaternion(Vector3f.ZP, -180, true);

        WORLD_ROTATION_QUATERNIONS[2] = new Quaternion(Vector3f.XP, -90, true);

        WORLD_ROTATION_QUATERNIONS[3] = new Quaternion(Vector3f.XP, -90, true);
        WORLD_ROTATION_QUATERNIONS[3].mul(new Quaternion(Vector3f.YP, -180, true));

        WORLD_ROTATION_QUATERNIONS[4] = new Quaternion(Vector3f.XP, -90, true);
        WORLD_ROTATION_QUATERNIONS[4].mul(new Quaternion(Vector3f.YP, -90, true));

        WORLD_ROTATION_QUATERNIONS[5] = new Quaternion(Vector3f.XP, -90, true);
        WORLD_ROTATION_QUATERNIONS[5].mul(new Quaternion(Vector3f.YP, -270, true));
    }

    /**
     * Note: don't modify the quaternion object in-place
     */
    public static Quaternion getWorldRotationQuaternion(Direction gravityDirection) {
        return WORLD_ROTATION_QUATERNIONS[gravityDirection.get3DDataValue()];
    }

    private static final Quaternion[] ENTITY_ROTATION_QUATERNIONS = new Quaternion[6];

    static {
        for (int i = 0; i < 6; i++) {
            Quaternion quaternion = WORLD_ROTATION_QUATERNIONS[i].copy();
            quaternion.conj();
            ENTITY_ROTATION_QUATERNIONS[i] = quaternion;
        }
    }

    /**
     * Note: don't modify the quaternion object in-place
     */
    public static Quaternion getCameraRotationQuaternion(Direction gravityDirection) {
        return ENTITY_ROTATION_QUATERNIONS[gravityDirection.get3DDataValue()];
    }

    public static Quaternion getRotationBetween(Direction d1, Direction d2) {
        Vector3d start = Vector3d.atLowerCornerOf(d1.getNormal());
        Vector3d end = Vector3d.atLowerCornerOf(d2.getNormal());
        if (d1.getOpposite() == d2) {
            GravityMod.LOGGER.info(new Quaternion(new Vector3f(0, 0, -1), 180.0f, true).toString());
            return new Quaternion(new Vector3f(0, 0, -1), 180.0f, true);
        } else {
            return QuaternionUtil.getRotationBetween(start, end);
        }
    }

    // slerp is not present in 1.16.5 Quaternion, so it's implemented manually
    public static Quaternion interpolate(Quaternion q1, Quaternion q2, float t) {
        Quaternion q2c = q2.copy();

        float dot = q1.i() * q2c.i() + q1.j() * q2c.j() + q1.k() * q2c.k() + q1.r() * q2c.r();

        if (dot < 0.0f) {
            q2c.mul(-1.0f);
            dot = -dot;
        }

        if (dot > 0.9995f) {
            Quaternion result = new Quaternion(
                    Mth.lerp(t, q1.i(), q2c.i()),
                    Mth.lerp(t, q1.j(), q2c.j()),
                    Mth.lerp(t, q1.k(), q2c.k()),
                    Mth.lerp(t, q1.r(), q2c.r())
            );
            result.normalize();
            return result;
        }

        float theta_0 = (float) Math.acos(dot);
        float theta = theta_0 * t;
        float sin_theta = (float) Math.sin(theta);
        float sin_theta_0 = (float) Math.sin(theta_0);

        float s0 = (float) Math.cos(theta) - dot * sin_theta / sin_theta_0;
        float s1 = sin_theta / sin_theta_0;

        return new Quaternion(
                (s0 * q1.i()) + (s1 * q2c.i()),
                (s0 * q1.j()) + (s1 * q2c.j()),
                (s0 * q1.k()) + (s1 * q2c.k()),
                (s0 * q1.r()) + (s1 * q2c.r())
        );
    }
}