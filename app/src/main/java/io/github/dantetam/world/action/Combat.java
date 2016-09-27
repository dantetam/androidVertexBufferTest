package io.github.dantetam.world.action;

import io.github.dantetam.world.entity.Entity;

/**
 * Created by Dante on 9/27/2016.
 */
public class Combat {

    public static void attackMelee(Entity attacker, Entity defender) {
        int[] damage = calculateMelee(attacker, defender);
        attacker.health -= damage[0];
        defender.health -= damage[1];
    }

    public static void attackRanged(Entity attacker, Entity defender) {
        int[] damage = calculateRanged(attacker, defender);
        attacker.health -= damage[0];
        defender.health -= damage[1];
    }

    private static int[] calculateMelee(Entity attacker, Entity defender) {
        return new int[]{15,15};
    }

    private static int[] calculateRanged(Entity attacker, Entity defender) {
        return new int[]{0,15};
    }

}
