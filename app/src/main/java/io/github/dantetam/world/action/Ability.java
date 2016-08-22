package io.github.dantetam.world.action;

import io.github.dantetam.world.entity.Entity;

/**
 * Created by Dante on 8/1/2016.
 */
public class Ability {

    public String name;

    public Ability(String n) {
        name = n;
    }

    public Action.ActionStatus gameExecuteAbility(Entity person) {
        return Action.ActionStatus.ALREADY_COMPLETED;
    }

}
