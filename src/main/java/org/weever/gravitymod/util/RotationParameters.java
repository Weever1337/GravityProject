package org.weever.gravitymod.util;

import net.minecraft.nbt.CompoundNBT;
import java.util.Objects;

public final class RotationParameters {
    private final boolean rotateVelocity;
    private final boolean rotateView; // currently ignores this
    private final int rotationTimeMS;

    public static RotationParameters defaultParam = new RotationParameters(
            true, true, 500
    );

    public RotationParameters(boolean rotateVelocity, boolean rotateView, int rotationTimeMS) {
        this.rotateVelocity = rotateVelocity;
        this.rotateView = rotateView;
        this.rotationTimeMS = rotationTimeMS;
    }

    public static void updateDefault() {
        defaultParam = new RotationParameters(
                true,
                true,
                400
        );
    }

    public static RotationParameters getDefault() {
        return defaultParam;
    }

    public RotationParameters withRotationTimeMs(int newRotationTimeMS) {
        return new RotationParameters(
                this.rotateVelocity,
                this.rotateView,
                newRotationTimeMS
        );
    }

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("RotateVelocity", this.rotateVelocity);
        tag.putBoolean("RotateView", this.rotateView);
        tag.putInt("RotationTimeMS", this.rotationTimeMS);
        return tag;
    }

    public static RotationParameters fromTag(CompoundNBT tag) {
        return new RotationParameters(
                tag.getBoolean("RotateVelocity"),
                tag.getBoolean("RotateView"),
                tag.getInt("RotationTimeMS")
        );
    }

    public boolean rotateVelocity() {
        return this.rotateVelocity;
    }

    public boolean rotateView() {
        return this.rotateView;
    }

    public int rotationTimeMS() {
        return this.rotationTimeMS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RotationParameters that = (RotationParameters) o;
        return rotateVelocity == that.rotateVelocity &&
                rotateView == that.rotateView &&
                rotationTimeMS == that.rotationTimeMS;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotateVelocity, rotateView, rotationTimeMS);
    }

    @Override
    public String toString() {
        return "RotationParameters{" +
                "rotateVelocity=" + rotateVelocity +
                ", rotateView=" + rotateView +
                ", rotationTimeMS=" + rotationTimeMS +
                '}';
    }
}