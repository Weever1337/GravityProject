package org.weever.gravitymod.compat.presencefootsteps;

import org.weever.gravitymod.compat.ModCompat;

public final class PresenceFootstepsCompat {
    public static final String MOD_ID = "presencefootsteps";

    private PresenceFootstepsCompat() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModCompat.isLoaded(MOD_ID);
    }
}
