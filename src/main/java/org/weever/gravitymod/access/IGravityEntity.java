package org.weever.gravitymod.access;

import net.minecraft.util.Direction;

public interface IGravityEntity {
    /**Gravity*/
    Direction gravitymod$getGravityDirection();
    void gravitymod$setGravityDirection(Direction direction);
    void gravitymod$setBaseGravityDirection(Direction direction);

    /**
     * Sets the gravity direction for the current tick only;
     * once no effect calls this anymore, gravity falls back to the base direction
     */
    void gravitymod$applyGravityEffect(Direction direction);


    double gravitymod$getGravityStrength();
    void gravitymod$setGravityStrength(double str);
    void gravitymod$setTaggedForFlip(boolean flip);
}
