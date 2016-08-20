package io.github.dantetam.world;

import java.util.HashMap;

/**
 * Created by Dante on 7/13/2016.
 */
public class ArtificialIntelligence {

    public static Clan parentClan;
    public String abilityOne = null;
    public String abilityTwo = null;
    public HashMap<String, Integer> personality, strategy, tactics;

    public ArtificialIntelligence(Clan clan) {
        parentClan = clan;
        personality = new HashMap<>();
        strategy = new HashMap<>();
        tactics = new HashMap<>();
    }

    /*public ArtificialIntelligence(World world) {
        this.world = world;
    }

    public static void computerClanActions(Clan c) {
        for (Person person: c.people) {
            while (person.actionPoints > 0) {
                person.gameMove(world.randomNeighbor(person.location));
            }
        }
    }

    public static void computerClanCombat(CombatPlan plan, Clan c) {
        plan.clear();
        for (Entity en: c.people) {
            plan.addAction(en, new CombatAction(Action.ActionType.COMBAT_MOVE, world.randomNeighbor(en.location)));
        }
    }*/

}
