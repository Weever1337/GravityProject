package org.weever.gravitymod.v1_20_1.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class AABBUtil {
    public static AxisAlignedBB ofSize(Vector3d $$0, double $$1, double $$2, double $$3) {
        return new AxisAlignedBB($$0.x - $$1 / (double)2.0F, $$0.y - $$2 / (double)2.0F, $$0.z - $$3 / (double)2.0F, $$0.x + $$1 / (double)2.0F, $$0.y + $$2 / (double)2.0F, $$0.z + $$3 / (double)2.0F);
    }
}
