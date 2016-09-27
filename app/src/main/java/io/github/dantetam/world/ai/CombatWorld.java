/*
package io.github.dantetam.world.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.action.CombatAction;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;
import io.github.dantetam.world.factory.PersonFactory;

*/
/**
 * Created by Dante on 8/2/2016.
 *//*

public class CombatWorld {

    public World linkedWorld;

    public Tile combatZoneCenter;
    public int inclusiveCombatRadius;

    public List<Tile> allTiles;
    public HashMap<Entity, Tile> originalPositions;

    public CombatPlan currentCombatPlan;

    public CombatWorld(World world, Tile tile, int radius) {
        linkedWorld = world;
        combatZoneCenter = tile;
        inclusiveCombatRadius = radius;

        originalPositions = new HashMap<>();

        initWorld();
    }

    public void addAction(Entity entity, CombatAction action) {
        if (currentCombatPlan == null) {
            currentCombatPlan = new CombatPlan(combatZoneCenter, inclusiveCombatRadius);
        }
        currentCombatPlan.addAction(entity, action);
    }

    public void advanceTurn() {
        currentCombatPlan.execute();
        currentCombatPlan.clear();
        for (Clan clan: linkedWorld.getClans()) {
            clan.ai.computerClanCombat(currentCombatPlan);
            currentCombatPlan.execute();
            currentCombatPlan.clear();
        }
    }

    public boolean checkTileWithinZone(Tile t) {
        return combatZoneCenter.dist(t) <= inclusiveCombatRadius;
    }

    public void initWorld() {
        Collection<Tile> tiles = linkedWorld.getRing(combatZoneCenter, inclusiveCombatRadius);
        allTiles = new ArrayList<>();
        for (Tile tile: tiles) {
            allTiles.add(tile);
        }
        for (Tile tile: allTiles) {
            for (Entity entity: tile.occupants) {
                originalPositions.put(entity, entity.location());
                Tile random = allTiles.get((int)(Math.random()*allTiles.size()));
                entity.move(random);
            }
        }
    }

    */
/*
    Return to the normal game state, keeping the results of combat.
    Remove killed entities if necessary.
     *//*

    public void pauseCombatWorld() {
        for (Map.Entry<Entity, Tile> entry: originalPositions.entrySet()) {
            Entity entity = entry.getKey();
            if (entity.health <= 0) {
                if (entity instanceof Person) {
                    PersonFactory.removePerson((Person) entity);
                }
            }
            entry.getKey().move(entry.getValue());
        }
    }

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
        return new int[]{2,2};
    }

    private static int[] calculateRanged(Entity attacker, Entity defender) {
        return new int[]{2,2};
    }

}
*/
