package io.github.dantetam.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dante on 8/2/2016.
 */
public class CombatWorld {

    public World linkedWorld;

    public Tile combatZoneCenter;
    public int inclusiveCombatRadius;

    public Collection<Tile> allTiles;
    public HashMap<Entity, Tile> originalPositions;

    public CombatWorld(World world, Tile tile, int radius) {
        linkedWorld = world;
        combatZoneCenter = tile;
        inclusiveCombatRadius = radius;

        originalPositions = new HashMap<>();
    }

    public boolean checkTileWithinZone(Tile t) {
        return combatZoneCenter.dist(t) <= inclusiveCombatRadius;
    }

    public void initWorld() {
        Collection<Tile> tiles = linkedWorld.getRing(combatZoneCenter, inclusiveCombatRadius);
        allTiles = tiles;
        for (Tile tile: allTiles) {
            for (Entity entity: tile.occupants) {
                originalPositions.put(entity, entity.location);
            }
        }
    }

    /*
    Return to the normal game state, keeping the results of combat.
    Remove killed entities if necessary.
     */
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

}
