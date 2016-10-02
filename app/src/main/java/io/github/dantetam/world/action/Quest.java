package io.github.dantetam.world.action;

import io.github.dantetam.world.entity.Clan;

/**
 * Created by Dante on 10/2/2016.
 */
public abstract class Quest {

    public Ability reward;
    public int[] flatReward;
    public int influence;

    public boolean finished(Clan c) {
        return false;
    }

}
