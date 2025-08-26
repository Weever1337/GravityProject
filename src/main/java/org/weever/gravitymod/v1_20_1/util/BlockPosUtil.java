package org.weever.gravitymod.v1_20_1.util;

import net.minecraft.dispenser.IPosition;
import net.minecraft.util.math.BlockPos;
import org.weever.gravitymod.v1_20_1.util.Mth;

public class BlockPosUtil {
    public static BlockPos containing(double $$0, double $$1, double $$2) {
        return new BlockPos(Mth.floor($$0), Mth.floor($$1), Mth.floor($$2));
    }

    public static BlockPos containing(IPosition $$0) {
        return containing($$0.x(), $$0.y(), $$0.z());
    }
}
