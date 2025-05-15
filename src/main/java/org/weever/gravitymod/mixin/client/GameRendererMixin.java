package org.weever.gravitymod.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;
import org.weever.gravitymod.util.GravityUtil;

import static net.minecraftforge.client.ForgeHooksClient.dispatchRenderLast;
import static net.minecraftforge.client.ForgeHooksClient.onCameraSetup;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private LightTexture lightTexture;

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private ActiveRenderInfo mainCamera;
    @Shadow private float renderDistance;
    @Shadow private int tick;
    @Shadow private boolean renderHand;

    @Shadow
    public abstract void pick(float p_78473_1_);

    @Shadow
    protected abstract boolean shouldRenderBlockOutline();

    @Shadow
    public abstract Matrix4f getProjectionMatrix(ActiveRenderInfo p_228382_1_, float p_228382_2_, boolean p_228382_3_);

    @Shadow
    protected abstract void bobHurt(MatrixStack p_228380_1_, float p_228380_2_);

    @Shadow
    protected abstract void bobView(MatrixStack p_228383_1_, float p_228383_2_);

    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f p_228379_1_);

    @Shadow
    protected abstract void renderItemInHand(MatrixStack p_228381_1_, ActiveRenderInfo p_228381_2_, float p_228381_3_);

//    @Inject(
//            method = "renderLevel",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lcom/mojang/blaze3d/matrix/MatrixStack;mulPose(Lnet/minecraft/util/math/vector/Quaternion;)V",
//                    ordinal = 3,
//                    shift = At.Shift.AFTER
//            )
//    )
//    private void injectWorldRotation(float partialTicks, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
//        if (this.mainCamera.getEntity() instanceof IGravityEntity) {
//            IGravityEntity gravityEntity = (IGravityEntity) this.mainCamera.getEntity();
//            GravityDirection direction = gravityEntity.getGravityDirection();
//
//            matrixStack.mulPose(GravityUtil.getWorldRotation(direction));
//        }
//    }

    @Overwrite
    public void renderLevel(float p_228378_1_, long p_228378_2_, MatrixStack p_228378_4_) {
        this.lightTexture.updateLightTexture(p_228378_1_);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.pick(p_228378_1_);
        this.minecraft.getProfiler().push("center");
        boolean flag = this.shouldRenderBlockOutline();
        this.minecraft.getProfiler().popPush("camera");
        ActiveRenderInfo activerenderinfo = this.mainCamera;
        this.renderDistance = (float) (this.minecraft.options.renderDistance * 16);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.last().pose().multiply(this.getProjectionMatrix(activerenderinfo, p_228378_1_, true));
        this.bobHurt(matrixstack, p_228378_1_);
        if (this.minecraft.options.bobView) {
            this.bobView(matrixstack, p_228378_1_);
        }

        float f = MathHelper.lerp(p_228378_1_, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime)
                * this.minecraft.options.screenEffectScale * this.minecraft.options.screenEffectScale;
        if (f > 0.0F) {
            int i = this.minecraft.player.hasEffect(Effects.CONFUSION) ? 7 : 20;
            float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
            f1 = f1 * f1;
            Vector3f vector3f = new Vector3f(0.0F, MathHelper.SQRT_OF_TWO / 2.0F, MathHelper.SQRT_OF_TWO / 2.0F);
            matrixstack.mulPose(vector3f.rotationDegrees(((float) this.tick + p_228378_1_) * (float) i));
            matrixstack.scale(1.0F / f1, 1.0F, 1.0F);
            float f2 = -((float) this.tick + p_228378_1_) * (float) i;
            matrixstack.mulPose(vector3f.rotationDegrees(f2));
        }

        Matrix4f matrix4f = matrixstack.last().pose();
        this.resetProjectionMatrix(matrix4f);
        activerenderinfo.setup(
                this.minecraft.level,
                this.minecraft.getCameraEntity() == null
                        ? this.minecraft.player
                        : this.minecraft.getCameraEntity(),
                !this.minecraft.options.getCameraType().isFirstPerson(),
                this.minecraft.options.getCameraType().isMirrored(),
                p_228378_1_
        );

        EntityViewRenderEvent.CameraSetup cameraSetup = onCameraSetup((GameRenderer) (Object) this, activerenderinfo, p_228378_1_);
        activerenderinfo.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
        p_228378_4_.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

        IGravityEntity entity = (IGravityEntity) activerenderinfo.getEntity();
        p_228378_4_.mulPose(Vector3f.XP.rotationDegrees(activerenderinfo.getXRot()));
        p_228378_4_.mulPose(Vector3f.YP.rotationDegrees(activerenderinfo.getYRot() + 180.0F));
        p_228378_4_.mulPose(GravityUtil.getWorldRotation(entity.getGravityDirection()));
        this.minecraft.levelRenderer.renderLevel(
                p_228378_4_,
                p_228378_1_,
                p_228378_2_,
                flag,
                activerenderinfo,
                (GameRenderer) (Object) this,
                this.lightTexture,
                matrix4f
        );
        this.minecraft.getProfiler().popPush("forge_render_last");
        dispatchRenderLast(
                this.minecraft.levelRenderer,
                p_228378_4_,
                p_228378_1_,
                matrix4f,
                p_228378_2_
        );
        this.minecraft.getProfiler().popPush("hand");
        if (this.renderHand) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            this.renderItemInHand(p_228378_4_, activerenderinfo, p_228378_1_);
        }

        this.minecraft.getProfiler().pop();
    }
}