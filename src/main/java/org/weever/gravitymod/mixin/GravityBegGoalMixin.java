package org.weever.gravitymod.mixin;

import net.minecraft.entity.ai.goal.BegGoal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.weever.gravitymod.util.GravityAPI;

@Mixin(BegGoal.class)
public abstract class GravityBegGoalMixin {
    @Shadow
    @Nullable
    private PlayerEntity player;

    @Shadow
    @Final
    private WolfEntity wolf;

    @Shadow
    private int lookTime;

    @Inject(
            method = "tick()V",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    private void gravitymod$tick(CallbackInfo ci) {
        Direction gravityDirection = GravityAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN)
            return;
        ci.cancel();
        this.wolf.getLookControl().setLookAt(this.player.getEyePosition(1).x, this.player.getEyePosition(1).y, this.player.getEyePosition(1).z, 10.0F, (float) this.wolf.getMaxHeadXRot());
        this.lookTime--;
    }
}
