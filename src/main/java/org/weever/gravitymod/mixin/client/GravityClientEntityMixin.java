package org.weever.gravitymod.mixin.client;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.weever.gravitymod.access.IClientEntity;
import org.weever.gravitymod.util.RotationAnimation;

@Mixin(value= Entity.class)
public class GravityClientEntityMixin implements IClientEntity {
    @Unique
    public RotationAnimation gravitymod$gravityRotationAnim =new RotationAnimation();
    @Unique
    @Override
    public void gravitymod$setGravityAnimation(RotationAnimation ra){
        gravitymod$gravityRotationAnim = ra;
    }
    @Unique
    @Override
    public RotationAnimation gravitymod$getGravityAnimation(){
        return gravitymod$gravityRotationAnim;
    }
}
