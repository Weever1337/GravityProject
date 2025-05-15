package org.weever.gravitymod.client;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.weever.gravitymod.GravityMod;
import org.weever.gravitymod.api.GravityAPI;
import org.weever.gravitymod.api.GravityDirection;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GravityMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        event.getMatrixStack().pushPose();
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity != null && GravityAPI.getGravityDirection(livingEntity) != GravityDirection.DOWN) {
            GravityDirection gravityDirection = GravityAPI.getGravityDirection(livingEntity);
//            System.out.println("CLIENT: " + gravityDirection);
            if (gravityDirection == GravityDirection.EAST) {
                Quaternion rotateLeft = Vector3f.YP.rotationDegrees(90f);
                Quaternion rotateToWall = Vector3f.ZP.rotationDegrees(-90f);

                rotateToWall.mul(rotateLeft);
                event.getMatrixStack().mulPose(rotateToWall);
            } else if (gravityDirection == GravityDirection.WEST) {
                Quaternion rotateLeft = Vector3f.YP.rotationDegrees(-90f);
                Quaternion rotateToWall = Vector3f.ZP.rotationDegrees(90f);

                rotateToWall.mul(rotateLeft);
                event.getMatrixStack().mulPose(rotateToWall);
            } else if (gravityDirection == GravityDirection.UP) {
                Quaternion q = Vector3f.XP.rotationDegrees(90f);
                event.getMatrixStack().mulPose(q);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        event.getMatrixStack().popPose();
    }
}