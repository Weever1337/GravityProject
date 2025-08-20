package org.weever.gravitymod.v1_20_1.util;

import net.minecraft.dispenser.IPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BlockPosUtil {
    public static BlockPos containing(double $$0, double $$1, double $$2) {
        return new BlockPos(MathHelper.floor($$0), MathHelper.floor($$1), MathHelper.floor($$2));
    }

    public static BlockPos containing(IPosition $$0) {
        return containing($$0.x(), $$0.y(), $$0.z());
    }
}
