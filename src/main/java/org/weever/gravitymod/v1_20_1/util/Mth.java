package org.weever.gravitymod.v1_20_1.util;

import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class Mth {
    private static final long UUID_VERSION = 61440L;
    private static final long UUID_VERSION_TYPE_4 = 16384L;
    private static final long UUID_VARIANT = -4611686018427387904L;
    private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
    public static final float PI = (float)Math.PI;
    public static final float HALF_PI = ((float)Math.PI / 2F);
    public static final float TWO_PI = ((float)Math.PI * 2F);
    public static final float DEG_TO_RAD = ((float)Math.PI / 180F);
    public static final float RAD_TO_DEG = (180F / (float)Math.PI);
    public static final float EPSILON = 1.0E-5F;
    public static final float SQRT_OF_TWO = sqrt(2.0F);
    private static final float SIN_SCALE = 10430.378F;
    private static final float[] SIN = (float[]) Util.make(new float[65536], ($$0x) -> {
        for(int $$1 = 0; $$1 < $$0x.length; ++$$1) {
            $$0x[$$1] = (float)Math.sin((double)$$1 * Math.PI * (double)2.0F / (double)65536.0F);
        }

    });
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double ONE_SIXTH = 0.16666666666666666;
    private static final int FRAC_EXP = 8;
    private static final int LUT_SIZE = 257;
    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];

    public static float sin(float $$0) {
        return SIN[(int)($$0 * 10430.378F) & '\uffff'];
    }

    public static float cos(float $$0) {
        return SIN[(int)($$0 * 10430.378F + 16384.0F) & '\uffff'];
    }

    public static float sqrt(float $$0) {
        return (float)Math.sqrt((double)$$0);
    }

    public static int floor(float $$0) {
        int $$1 = (int)$$0;
        return $$0 < (float)$$1 ? $$1 - 1 : $$1;
    }

    public static int floor(double $$0) {
        int $$1 = (int)$$0;
        return $$0 < (double)$$1 ? $$1 - 1 : $$1;
    }

    public static long lfloor(double $$0) {
        long $$1 = (long)$$0;
        return $$0 < (double)$$1 ? $$1 - 1L : $$1;
    }

    public static float abs(float $$0) {
        return Math.abs($$0);
    }

    public static int abs(int $$0) {
        return Math.abs($$0);
    }

    public static int ceil(float $$0) {
        int $$1 = (int)$$0;
        return $$0 > (float)$$1 ? $$1 + 1 : $$1;
    }

    public static int ceil(double $$0) {
        int $$1 = (int)$$0;
        return $$0 > (double)$$1 ? $$1 + 1 : $$1;
    }

    public static int clamp(int $$0, int $$1, int $$2) {
        return Math.min(Math.max($$0, $$1), $$2);
    }

    public static float clamp(float $$0, float $$1, float $$2) {
        return $$0 < $$1 ? $$1 : Math.min($$0, $$2);
    }

    public static double clamp(double $$0, double $$1, double $$2) {
        return $$0 < $$1 ? $$1 : Math.min($$0, $$2);
    }

    public static double clampedLerp(double $$0, double $$1, double $$2) {
        if ($$2 < (double)0.0F) {
            return $$0;
        } else {
            return $$2 > (double)1.0F ? $$1 : lerp($$2, $$0, $$1);
        }
    }

    public static float clampedLerp(float $$0, float $$1, float $$2) {
        if ($$2 < 0.0F) {
            return $$0;
        } else {
            return $$2 > 1.0F ? $$1 : lerp($$2, $$0, $$1);
        }
    }

    public static double absMax(double $$0, double $$1) {
        if ($$0 < (double)0.0F) {
            $$0 = -$$0;
        }

        if ($$1 < (double)0.0F) {
            $$1 = -$$1;
        }

        return Math.max($$0, $$1);
    }

    public static int floorDiv(int $$0, int $$1) {
        return Math.floorDiv($$0, $$1);
    }

    public static int nextInt(Random $$0, int $$1, int $$2) {
        return $$1 >= $$2 ? $$1 : $$0.nextInt($$2 - $$1 + 1) + $$1;
    }

    public static float nextFloat(Random $$0, float $$1, float $$2) {
        return $$1 >= $$2 ? $$1 : $$0.nextFloat() * ($$2 - $$1) + $$1;
    }

    public static double nextDouble(Random $$0, double $$1, double $$2) {
        return $$1 >= $$2 ? $$1 : $$0.nextDouble() * ($$2 - $$1) + $$1;
    }

    public static boolean equal(float $$0, float $$1) {
        return Math.abs($$1 - $$0) < 1.0E-5F;
    }

    public static boolean equal(double $$0, double $$1) {
        return Math.abs($$1 - $$0) < (double)1.0E-5F;
    }

    public static int positiveModulo(int $$0, int $$1) {
        return Math.floorMod($$0, $$1);
    }

    public static float positiveModulo(float $$0, float $$1) {
        return ($$0 % $$1 + $$1) % $$1;
    }

    public static double positiveModulo(double $$0, double $$1) {
        return ($$0 % $$1 + $$1) % $$1;
    }

    public static boolean isMultipleOf(int $$0, int $$1) {
        return $$0 % $$1 == 0;
    }

    public static int wrapDegrees(int $$0) {
        int $$1 = $$0 % 360;
        if ($$1 >= 180) {
            $$1 -= 360;
        }

        if ($$1 < -180) {
            $$1 += 360;
        }

        return $$1;
    }

    public static float wrapDegrees(float $$0) {
        float $$1 = $$0 % 360.0F;
        if ($$1 >= 180.0F) {
            $$1 -= 360.0F;
        }

        if ($$1 < -180.0F) {
            $$1 += 360.0F;
        }

        return $$1;
    }

    public static double wrapDegrees(double $$0) {
        double $$1 = $$0 % (double)360.0F;
        if ($$1 >= (double)180.0F) {
            $$1 -= (double)360.0F;
        }

        if ($$1 < (double)-180.0F) {
            $$1 += (double)360.0F;
        }

        return $$1;
    }

    public static float degreesDifference(float $$0, float $$1) {
        return wrapDegrees($$1 - $$0);
    }

    public static float degreesDifferenceAbs(float $$0, float $$1) {
        return abs(degreesDifference($$0, $$1));
    }

    public static float rotateIfNecessary(float $$0, float $$1, float $$2) {
        float $$3 = degreesDifference($$0, $$1);
        float $$4 = clamp($$3, -$$2, $$2);
        return $$1 - $$4;
    }

    public static float approach(float $$0, float $$1, float $$2) {
        $$2 = abs($$2);
        return $$0 < $$1 ? clamp($$0 + $$2, $$0, $$1) : clamp($$0 - $$2, $$1, $$0);
    }

    public static float approachDegrees(float $$0, float $$1, float $$2) {
        float $$3 = degreesDifference($$0, $$1);
        return approach($$0, $$0 + $$3, $$2);
    }

    public static int getInt(String $$0, int $$1) {
        return NumberUtils.toInt($$0, $$1);
    }

    public static int smallestEncompassingPowerOfTwo(int $$0) {
        int $$1 = $$0 - 1;
        $$1 |= $$1 >> 1;
        $$1 |= $$1 >> 2;
        $$1 |= $$1 >> 4;
        $$1 |= $$1 >> 8;
        $$1 |= $$1 >> 16;
        return $$1 + 1;
    }

    public static boolean isPowerOfTwo(int $$0) {
        return $$0 != 0 && ($$0 & $$0 - 1) == 0;
    }

    public static int ceillog2(int $$0) {
        $$0 = isPowerOfTwo($$0) ? $$0 : smallestEncompassingPowerOfTwo($$0);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)$$0 * 125613361L >> 27) & 31];
    }

    public static int log2(int $$0) {
        return ceillog2($$0) - (isPowerOfTwo($$0) ? 0 : 1);
    }

    public static int color(float $$0, float $$1, float $$2) {
        return ARGB32.color(0, floor($$0 * 255.0F), floor($$1 * 255.0F), floor($$2 * 255.0F));
    }

    public static float frac(float $$0) {
        return $$0 - (float)floor($$0);
    }

    public static double frac(double $$0) {
        return $$0 - (double)lfloor($$0);
    }

    /** @deprecated */
    @Deprecated
    public static long getSeed(Vector3i $$0) {
        return getSeed($$0.getX(), $$0.getY(), $$0.getZ());
    }

    /** @deprecated */
    @Deprecated
    public static long getSeed(int $$0, int $$1, int $$2) {
        long $$3 = (long)($$0 * 3129871) ^ (long)$$2 * 116129781L ^ (long)$$1;
        $$3 = $$3 * $$3 * 42317861L + $$3 * 11L;
        return $$3 >> 16;
    }

    public static UUID createInsecureUUID(Random $$0) {
        long $$1 = $$0.nextLong() & -61441L | 16384L;
        long $$2 = $$0.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID($$1, $$2);
    }

    public static UUID createInsecureUUID() {
        return createInsecureUUID(RANDOM);
    }

    public static double inverseLerp(double $$0, double $$1, double $$2) {
        return ($$0 - $$1) / ($$2 - $$1);
    }

    public static float inverseLerp(float $$0, float $$1, float $$2) {
        return ($$0 - $$1) / ($$2 - $$1);
    }

    public static boolean rayIntersectsAABB(Vector3d $$0, Vector3d $$1, AxisAlignedBB $$2) {
        double $$3 = ($$2.minX + $$2.maxX) * (double)0.5F;
        double $$4 = ($$2.maxX - $$2.minX) * (double)0.5F;
        double $$5 = $$0.x - $$3;
        if (Math.abs($$5) > $$4 && $$5 * $$1.x >= (double)0.0F) {
            return false;
        } else {
            double $$6 = ($$2.minY + $$2.maxY) * (double)0.5F;
            double $$7 = ($$2.maxY - $$2.minY) * (double)0.5F;
            double $$8 = $$0.y - $$6;
            if (Math.abs($$8) > $$7 && $$8 * $$1.y >= (double)0.0F) {
                return false;
            } else {
                double $$9 = ($$2.minZ + $$2.maxZ) * (double)0.5F;
                double $$10 = ($$2.maxZ - $$2.minZ) * (double)0.5F;
                double $$11 = $$0.z - $$9;
                if (Math.abs($$11) > $$10 && $$11 * $$1.z >= (double)0.0F) {
                    return false;
                } else {
                    double $$12 = Math.abs($$1.x);
                    double $$13 = Math.abs($$1.y);
                    double $$14 = Math.abs($$1.z);
                    double $$15 = $$1.y * $$11 - $$1.z * $$8;
                    if (Math.abs($$15) > $$7 * $$14 + $$10 * $$13) {
                        return false;
                    } else {
                        $$15 = $$1.z * $$5 - $$1.x * $$11;
                        if (Math.abs($$15) > $$4 * $$14 + $$10 * $$12) {
                            return false;
                        } else {
                            $$15 = $$1.x * $$8 - $$1.y * $$5;
                            return Math.abs($$15) < $$4 * $$13 + $$7 * $$12;
                        }
                    }
                }
            }
        }
    }

    public static double atan2(double $$0, double $$1) {
        double $$2 = $$1 * $$1 + $$0 * $$0;
        if (Double.isNaN($$2)) {
            return Double.NaN;
        } else {
            boolean $$3 = $$0 < (double)0.0F;
            if ($$3) {
                $$0 = -$$0;
            }

            boolean $$4 = $$1 < (double)0.0F;
            if ($$4) {
                $$1 = -$$1;
            }

            boolean $$5 = $$0 > $$1;
            if ($$5) {
                double $$6 = $$1;
                $$1 = $$0;
                $$0 = $$6;
            }

            double $$7 = fastInvSqrt($$2);
            $$1 *= $$7;
            $$0 *= $$7;
            double $$8 = FRAC_BIAS + $$0;
            int $$9 = (int)Double.doubleToRawLongBits($$8);
            double $$10 = ASIN_TAB[$$9];
            double $$11 = COS_TAB[$$9];
            double $$12 = $$8 - FRAC_BIAS;
            double $$13 = $$0 * $$11 - $$1 * $$12;
            double $$14 = ((double)6.0F + $$13 * $$13) * $$13 * 0.16666666666666666;
            double $$15 = $$10 + $$14;
            if ($$5) {
                $$15 = (Math.PI / 2D) - $$15;
            }

            if ($$4) {
                $$15 = Math.PI - $$15;
            }

            if ($$3) {
                $$15 = -$$15;
            }

            return $$15;
        }
    }

    public static float invSqrt(float $$0) {
        return org.joml.Math.invsqrt($$0);
    }

    public static double invSqrt(double $$0) {
        return org.joml.Math.invsqrt($$0);
    }

    /** @deprecated */
    @Deprecated
    public static double fastInvSqrt(double $$0) {
        double $$1 = (double)0.5F * $$0;
        long $$2 = Double.doubleToRawLongBits($$0);
        $$2 = 6910469410427058090L - ($$2 >> 1);
        $$0 = Double.longBitsToDouble($$2);
        $$0 *= (double)1.5F - $$1 * $$0 * $$0;
        return $$0;
    }

    public static float fastInvCubeRoot(float $$0) {
        int $$1 = Float.floatToIntBits($$0);
        $$1 = 1419967116 - $$1 / 3;
        float $$2 = Float.intBitsToFloat($$1);
        $$2 = 0.6666667F * $$2 + 1.0F / (3.0F * $$2 * $$2 * $$0);
        $$2 = 0.6666667F * $$2 + 1.0F / (3.0F * $$2 * $$2 * $$0);
        return $$2;
    }

    public static int hsvToRgb(float $$0, float $$1, float $$2) {
        int $$3 = (int)($$0 * 6.0F) % 6;
        float $$4 = $$0 * 6.0F - (float)$$3;
        float $$5 = $$2 * (1.0F - $$1);
        float $$6 = $$2 * (1.0F - $$4 * $$1);
        float $$7 = $$2 * (1.0F - (1.0F - $$4) * $$1);
        float $$8;
        float $$9;
        float $$10;
        switch ($$3) {
            case 0:
                $$8 = $$2;
                $$9 = $$7;
                $$10 = $$5;
                break;
            case 1:
                $$8 = $$6;
                $$9 = $$2;
                $$10 = $$5;
                break;
            case 2:
                $$8 = $$5;
                $$9 = $$2;
                $$10 = $$7;
                break;
            case 3:
                $$8 = $$5;
                $$9 = $$6;
                $$10 = $$2;
                break;
            case 4:
                $$8 = $$7;
                $$9 = $$5;
                $$10 = $$2;
                break;
            case 5:
                $$8 = $$2;
                $$9 = $$5;
                $$10 = $$6;
                break;
            default:
                throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + $$0 + ", " + $$1 + ", " + $$2);
        }

        return ARGB32.color(0, clamp((int)($$8 * 255.0F), 0, 255), clamp((int)($$9 * 255.0F), 0, 255), clamp((int)($$10 * 255.0F), 0, 255));
    }

    public static int murmurHash3Mixer(int $$0) {
        $$0 ^= $$0 >>> 16;
        $$0 *= -2048144789;
        $$0 ^= $$0 >>> 13;
        $$0 *= -1028477387;
        $$0 ^= $$0 >>> 16;
        return $$0;
    }

    public static int binarySearch(int $$0, int $$1, IntPredicate $$2) {
        int $$3 = $$1 - $$0;

        while($$3 > 0) {
            int $$4 = $$3 / 2;
            int $$5 = $$0 + $$4;
            if ($$2.test($$5)) {
                $$3 = $$4;
            } else {
                $$0 = $$5 + 1;
                $$3 -= $$4 + 1;
            }
        }

        return $$0;
    }

    public static int lerpInt(float $$0, int $$1, int $$2) {
        return $$1 + floor($$0 * (float)($$2 - $$1));
    }

    public static float lerp(float $$0, float $$1, float $$2) {
        return $$1 + $$0 * ($$2 - $$1);
    }

    public static double lerp(double $$0, double $$1, double $$2) {
        return $$1 + $$0 * ($$2 - $$1);
    }

    public static double lerp2(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
        return lerp($$1, lerp($$0, $$2, $$3), lerp($$0, $$4, $$5));
    }

    public static double lerp3(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5, double $$6, double $$7, double $$8, double $$9, double $$10) {
        return lerp($$2, lerp2($$0, $$1, $$3, $$4, $$5, $$6), lerp2($$0, $$1, $$7, $$8, $$9, $$10));
    }

    public static float catmullrom(float $$0, float $$1, float $$2, float $$3, float $$4) {
        return 0.5F * (2.0F * $$2 + ($$3 - $$1) * $$0 + (2.0F * $$1 - 5.0F * $$2 + 4.0F * $$3 - $$4) * $$0 * $$0 + (3.0F * $$2 - $$1 - 3.0F * $$3 + $$4) * $$0 * $$0 * $$0);
    }

    public static double smoothstep(double $$0) {
        return $$0 * $$0 * $$0 * ($$0 * ($$0 * (double)6.0F - (double)15.0F) + (double)10.0F);
    }

    public static double smoothstepDerivative(double $$0) {
        return (double)30.0F * $$0 * $$0 * ($$0 - (double)1.0F) * ($$0 - (double)1.0F);
    }

    public static int sign(double $$0) {
        if ($$0 == (double)0.0F) {
            return 0;
        } else {
            return $$0 > (double)0.0F ? 1 : -1;
        }
    }

    public static float rotLerp(float $$0, float $$1, float $$2) {
        return $$1 + $$0 * wrapDegrees($$2 - $$1);
    }

    public static float triangleWave(float $$0, float $$1) {
        return (Math.abs($$0 % $$1 - $$1 * 0.5F) - $$1 * 0.25F) / ($$1 * 0.25F);
    }

    public static float square(float $$0) {
        return $$0 * $$0;
    }

    public static double square(double $$0) {
        return $$0 * $$0;
    }

    public static int square(int $$0) {
        return $$0 * $$0;
    }

    public static long square(long $$0) {
        return $$0 * $$0;
    }

    public static double clampedMap(double $$0, double $$1, double $$2, double $$3, double $$4) {
        return clampedLerp($$3, $$4, inverseLerp($$0, $$1, $$2));
    }

    public static float clampedMap(float $$0, float $$1, float $$2, float $$3, float $$4) {
        return clampedLerp($$3, $$4, inverseLerp($$0, $$1, $$2));
    }

    public static double map(double $$0, double $$1, double $$2, double $$3, double $$4) {
        return lerp(inverseLerp($$0, $$1, $$2), $$3, $$4);
    }

    public static float map(float $$0, float $$1, float $$2, float $$3, float $$4) {
        return lerp(inverseLerp($$0, $$1, $$2), $$3, $$4);
    }

    public static double wobble(double $$0) {
        return $$0 + ((double)2.0F * new Random((long)floor($$0 * (double)3000.0F)).nextDouble() - (double)1.0F) * 1.0E-7 / (double)2.0F;
    }

    public static int roundToward(int $$0, int $$1) {
        return positiveCeilDiv($$0, $$1) * $$1;
    }

    public static int positiveCeilDiv(int $$0, int $$1) {
        return -Math.floorDiv(-$$0, $$1);
    }

    public static int randomBetweenInclusive(Random $$0, int $$1, int $$2) {
        return $$0.nextInt($$2 - $$1 + 1) + $$1;
    }

    public static float randomBetween(Random $$0, float $$1, float $$2) {
        return $$0.nextFloat() * ($$2 - $$1) + $$1;
    }

    public static float normal(Random $$0, float $$1, float $$2) {
        return $$1 + (float)$$0.nextGaussian() * $$2;
    }

    public static double lengthSquared(double $$0, double $$1) {
        return $$0 * $$0 + $$1 * $$1;
    }

    public static double length(double $$0, double $$1) {
        return Math.sqrt(lengthSquared($$0, $$1));
    }

    public static double lengthSquared(double $$0, double $$1, double $$2) {
        return $$0 * $$0 + $$1 * $$1 + $$2 * $$2;
    }

    public static double length(double $$0, double $$1, double $$2) {
        return Math.sqrt(lengthSquared($$0, $$1, $$2));
    }

    public static int quantize(double $$0, int $$1) {
        return floor($$0 / (double)$$1) * $$1;
    }

    static {
        for(int $$0 = 0; $$0 < 257; ++$$0) {
            double $$1 = (double)$$0 / (double)256.0F;
            double $$2 = Math.asin($$1);
            COS_TAB[$$0] = Math.cos($$2);
            ASIN_TAB[$$0] = $$2;
        }

    }
}
