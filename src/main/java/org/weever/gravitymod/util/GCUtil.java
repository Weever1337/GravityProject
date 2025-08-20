package org.weever.gravitymod.util;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.Style;

//https://github.com/qouteall/GravityChanger/tree/1.20.1-Fabric/src/main/java/gravity_changer/util
//credit to quoteall

public class GCUtil {
    public static ITextComponent getLinkText(String link) {
        return new StringTextComponent(link).setStyle(
                Style.EMPTY.withClickEvent(new ClickEvent(
                        ClickEvent.Action.OPEN_URL, link
                )).setUnderlined(true)
        );
    }

    public static ITextComponent getDirectionText(Direction gravityDirection) {
        return new TranslationTextComponent("direction." + gravityDirection.getName());
    }

    public static double distanceToRange(double value, double rangeStart, double rangeEnd) {
        if (value < rangeStart) {
            return rangeStart - value;
        }

        if (value > rangeEnd) {
            return value - rangeEnd;
        }

        return 0;
    }

    public static boolean isClientPlayer(Entity entity) {
        if (entity.level.isClientSide()) {
            return entity instanceof ClientPlayerEntity;
        }
        return false;
    }

    public static boolean isRemotePlayer(Entity entity) {
        if (entity.level.isClientSide()) {
            return entity instanceof RemoteClientPlayerEntity;
        }
        return false;
    }
}