package org.weever.gravitymod.compat;

import net.minecraftforge.fml.ModList;

public final class ModCompat {
    private ModCompat() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded(String modId) {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(modId);
    }
}
