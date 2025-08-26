package org.weever.gravitymod.util;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.weever.gravitymod.GravityMod;

import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

//https://github.com/qouteall/GravityChanger/tree/1.20.1-Fabric/src/main/java/gravity_changer/util
//credit to quoteall

public abstract class QuaternionUtil {
    public static Quaternion getViewRotation(float pitch, float yaw) {
        Quaternion r1 = new Quaternion(Vector3f.XP, pitch, true);
        Quaternion r2 = new Quaternion(Vector3f.YP, yaw + 180, true);
        r1.mul(r2);
        return r1;
    }

    // NOTE the "from" and "to" cannot be opposite
    public static Quaternion getRotationBetween(Vector3d from, Vector3d to) {
        from = from.normalize();
        to = to.normalize();
        Vector3d axis = from.cross(to).normalize();
        double cos = from.dot(to);
        double angle = Math.acos(cos);
        GravityMod.LOGGER.info(new Quaternion(
                new Vector3f((float) axis.x, (float) axis.y, (float) axis.z),
                (float) angle,
                false
        ).toString());
        return new Quaternion(
                new Vector3f((float) axis.x, (float) axis.y, (float) axis.z),
                (float) angle,
                false
        );
    }

    public static Vector3d rotate(Vector3d vec, Quaternion quaternion) {
        Vector3f vector3f = new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);

        vector3f.transform(quaternion);

        return new Vector3d(vector3f.x(), vector3f.y(), vector3f.z());
    }
}