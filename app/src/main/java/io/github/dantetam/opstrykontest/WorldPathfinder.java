package io.github.dantetam.opstrykontest;

import java.util.List;

import io.github.dantetam.world.Pathfinder;
import io.github.dantetam.world.Tile;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 7/14/2016.
 */
public class WorldPathfinder extends Pathfinder<Tile> {

    public World world;
    public static List<Tile> lastPath;
    public static int lastPathLen;

    public WorldPathfinder(World w) {
        world = w;
    }

    public List<Tile> findPath(Tile a, Tile b) {
        lastPath = super.findPath(a, b);
        lastPathLen = lastPath.size();
        return lastPath;
    }

}
