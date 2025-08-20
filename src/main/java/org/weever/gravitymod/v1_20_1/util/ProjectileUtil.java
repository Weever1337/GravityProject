package org.weever.gravitymod.v1_20_1.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

public final class ProjectileUtil {
    public static Hand getWeaponHoldingHand(LivingEntity $$0, Item $$1) {
        return $$0.getMainHandItem().getItem() == $$1 ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }
}
