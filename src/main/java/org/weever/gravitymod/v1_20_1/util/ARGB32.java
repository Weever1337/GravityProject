package org.weever.gravitymod.v1_20_1.util;

public class ARGB32 {
    public static int alpha(int $$0) {
        return $$0 >>> 24;
    }

    public static int red(int $$0) {
        return $$0 >> 16 & 255;
    }

    public static int green(int $$0) {
        return $$0 >> 8 & 255;
    }

    public static int blue(int $$0) {
        return $$0 & 255;
    }

    public static int color(int $$0, int $$1, int $$2, int $$3) {
        return $$0 << 24 | $$1 << 16 | $$2 << 8 | $$3;
    }

    public static int multiply(int $$0, int $$1) {
        return color(alpha($$0) * alpha($$1) / 255, red($$0) * red($$1) / 255, green($$0) * green($$1) / 255, blue($$0) * blue($$1) / 255);
    }

    public static int lerp(float $$0, int $$1, int $$2) {
        int $$3 = Mth.lerpInt($$0, alpha($$1), alpha($$2));
        int $$4 = Mth.lerpInt($$0, red($$1), red($$2));
        int $$5 = Mth.lerpInt($$0, green($$1), green($$2));
        int $$6 = Mth.lerpInt($$0, blue($$1), blue($$2));
        return color($$3, $$4, $$5, $$6);
    }
}
