package org.weever.gravitymod.access;

import net.minecraft.util.Direction;

public interface IGravityEntity {
    /**Gravity*/
    Direction gravitymod$getGravityDirection();
    void gravitymod$setGravityDirection(Direction direction);
    void gravitymod$setBaseGravityDirection(Direction direction);


    double gravitymod$getGravityStrength();
    void gravitymod$setGravityStrength(double str);
    void gravitymod$setTaggedForFlip(boolean flip);
}
