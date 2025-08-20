package org.weever.gravitymod.access;

import org.weever.gravitymod.util.RotationAnimation;

public interface IClientEntity {
    public void gravitymod$setGravityAnimation(RotationAnimation ra);
    RotationAnimation gravitymod$getGravityAnimation();
}
