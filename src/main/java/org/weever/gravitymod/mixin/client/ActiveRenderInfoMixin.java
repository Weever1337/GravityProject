package org.weever.gravitymod.mixin.client;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.weever.gravitymod.api.GravityDirection;
import org.weever.gravitymod.api.IGravityEntity;

@Mixin(ActiveRenderInfo.class)
public abstract class ActiveRenderInfoMixin {
    @Shadow
    private boolean initialized;
    @Shadow
    private IBlockReader level;
    @Shadow
    private Entity entity;
    @Shadow
    private boolean detached;
    @Shadow
    private boolean mirror;
    @Shadow
    private float eyeHeightOld;
    @Shadow
    private float eyeHeight;
    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Shadow
    protected abstract double getMaxZoom(double p_216779_1_);

    @Shadow
    protected abstract void setRotation(float p_216776_1_, float p_216776_2_);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    private Vector3d position;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    protected abstract void setPosition(Vector3d p_216774_1_);

    /**
     * @author Weever1337
     * @reason Fix position of the camera
     */
    @Overwrite
    public void setup(IBlockReader p_216772_1_, Entity entity, boolean detached, boolean mirror, float tick) {
        this.initialized = true;
        this.level = p_216772_1_;
        this.entity = entity;
        this.detached = detached;
        this.mirror = mirror;
        IGravityEntity gravitableEntity = (IGravityEntity) entity;
        GravityDirection gravityDirection = gravitableEntity.getGravityDirection();
        this.setRotation(entity.getViewYRot(tick), entity.getViewXRot(tick));
        switch (gravityDirection) {
            case EAST:
                this.setPosition(MathHelper.lerp(tick, entity.xo, entity.getX()) + (double) MathHelper.lerp(tick, this.eyeHeightOld, this.eyeHeight), MathHelper.lerp(tick, entity.yo, entity.getY()), MathHelper.lerp(tick, entity.zo, entity.getZ()));
                break;
            case WEST:
                this.setPosition(MathHelper.lerp(tick, entity.xo, entity.getX()) - (double) MathHelper.lerp(tick, this.eyeHeightOld, this.eyeHeight), MathHelper.lerp(tick, entity.yo, entity.getY()), MathHelper.lerp(tick, entity.zo, entity.getZ()));
                break;
            case UP:
                this.setPosition(MathHelper.lerp(tick, entity.xo, entity.getX()), MathHelper.lerp(tick, entity.yo, entity.getY()) - (double) MathHelper.lerp(tick, this.eyeHeightOld, this.eyeHeight), MathHelper.lerp(tick, entity.zo, entity.getZ()));
                break;
            default:
                this.setPosition(MathHelper.lerp(tick, entity.xo, entity.getX()), MathHelper.lerp(tick, entity.yo, entity.getY()) + (double) MathHelper.lerp(tick, this.eyeHeightOld, this.eyeHeight), MathHelper.lerp(tick, entity.zo, entity.getZ()));
                break;
        }
        if (detached) {
            if (mirror) {
                this.setRotation(this.yRot + 180.0F, -this.xRot);
            }
            this.move(-this.getMaxZoom(4.0D), 0.0D, 0.0D);
        } else if (entity instanceof LivingEntity && ((LivingEntity) entity).isSleeping()) {
            Direction direction = ((LivingEntity) entity).getBedOrientation();
            this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
            this.move(0.0D, 0.3D, 0.0D);
        }
    }

    /**
     * @author Weever1337
     * @reason Fix F5 position by rotating the camera offset vector
     */
    @Overwrite
    protected void move(double distanceForwards, double distanceUp, double distanceLeft) { // todo: Fix this
        double d0 = (double) this.forwards.x() * distanceForwards + (double) this.up.x() * distanceUp + (double) this.left.x() * distanceLeft;
        double d1 = (double) this.forwards.y() * distanceForwards + (double) this.up.y() * distanceUp + (double) this.left.y() * distanceLeft;
        double d2 = (double) this.forwards.z() * distanceForwards + (double) this.up.z() * distanceUp + (double) this.left.z() * distanceLeft;

        if (this.entity instanceof IGravityEntity) {
            IGravityEntity gravityEntity = (IGravityEntity) this.entity;
            GravityDirection direction = gravityEntity.getGravityDirection();

            if (direction != GravityDirection.DOWN) {
                Vector3f offset = new Vector3f((float) d0, (float) d1, (float) d2);

                Quaternion rotation = org.weever.gravitymod.util.GravityUtil.getWorldRotation(direction);

                offset.transform(rotation);

                d0 = offset.x();
                d1 = offset.y();
                d2 = offset.z();
            }
        }

        this.setPosition(new Vector3d(this.position.x + d0, this.position.y + d1, this.position.z + d2));
    }
}