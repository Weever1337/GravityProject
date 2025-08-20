package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;
import org.weever.gravitymod.util.RotationUtil;

import java.awt.*;

@Mixin(value = EntityRenderer.class,priority = 1001)
public abstract class GravityEntityRendererMixin<T extends Entity>  {
    @Shadow @Final protected EntityRendererManager entityRenderDispatcher;

    @Shadow
    public abstract FontRenderer getFont();

    @Inject(
            method = "renderNameTag",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void gravitymod$modifyExpressionValue_renderLabelIfPresent_getRotation_0(T $$0, ITextComponent $$1, MatrixStack $$2, IRenderTypeBuffer $$3, int $$4, CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection($$0);
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();

        double $$5 = this.entityRenderDispatcher.distanceToSqr($$0);
        if (!($$5 > 4096.0)) {
            boolean $$6 = !$$0.isDiscrete();
            float $$7 = $$0.getBbHeight() + 0.5F;
            int $$8 = "deadmau5".equals($$1.getString()) ? -10 : 0;
            $$2.pushPose();
            $$2.translate(0.0F, $$7, 0.0F);

            Quaternion quaternion = new Quaternion(RotationUtil.getCameraRotationQuaternion(gravityDirection));
            quaternion.conj();
            quaternion.mul(this.entityRenderDispatcher.cameraOrientation());

            $$2.mulPose(quaternion);
            $$2.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f $$9 = $$2.last().pose();
            float $$10 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int $$11 = (int)($$10 * 255.0F) << 24;
            FontRenderer $$12 = this.getFont();
            float $$13 = (float)(-$$12.width($$1) / 2);

            $$12.drawInBatch($$1, $$13, (float)$$8, 553648127, false, $$9, $$3, $$6, $$11, $$4);
            if ($$6) {
                $$12.drawInBatch($$1, $$13, (float)$$8, -1, false, $$9, $$3, false, 0, $$4);
            }

            $$2.popPose();
        }
    }
}