package com.hk47bit.gravitymod.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;

public abstract class QuaternionUtil {
    public static float magnitude(Quaternion quaternion) {
        return MathHelper.sqrt(quaternion.r() * quaternion.r() + quaternion.i() * quaternion.i() + quaternion.j() * quaternion.j() + quaternion.k() * quaternion.k());
    }

    public static float magnitudeSq(Quaternion quaternion) {
        return quaternion.r() * quaternion.r() + quaternion.i() * quaternion.i() + quaternion.j() * quaternion.j() + quaternion.k() * quaternion.k();
    }

    public static void inverse(Quaternion quaternion) {
        quaternion.conj();
        quaternion.mul(1.0F / magnitudeSq(quaternion));
    }
}
