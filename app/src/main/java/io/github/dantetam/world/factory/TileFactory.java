package io.github.dantetam.world.factory;

import java.util.ArrayList;

import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Item;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 7/17/2016.
 */
public class TileFactory {

    public static Tile newTile(Tile.Biome biome, Tile.Terrain terrain, Tile.Feature feature, World world, int a, int b) {
        Tile tile = new Tile();
        tile.world = world;
        tile.q = a; tile.r = b;
        tile.resources = new ArrayList<Item>();
        tile.occupants = new ArrayList<Entity>();
        tile.biome = biome;
        tile.terrain = terrain;
        //TODO: tile.feature
        tile.initBaseResources(2,1,0,0,0,0,0);
        int numSpaces = 3;
        switch (biome) {
            case SEA:
                tile.addBaseResources(0,0,0,1,0,0,0);
                numSpaces = 6;
                break;
            case ICE:
                tile.addBaseResources(-2,1,0,0,0,0,0);
                break;
            case TUNDRA:
                tile.addBaseResources(-1,1,0,0,0,0,0);
                break;
            case DESERT:
                tile.addBaseResources(-1,1,0,1,0,0,0);
                break;
            case STEPPE:
                tile.addBaseResources(1,0,0,0,0,0,0);
                numSpaces = 4;
                break;
            case FOREST:
                tile.addBaseResources(1,1,0,0,0,0,0);
                numSpaces = 4;
                break;
            case RAINFOREST:
                tile.addBaseResources(1,0,1,1,0,0,0);
                break;
            default:
                System.err.println("Invalid biome");
                break;
        }
        switch (terrain) {
            case SHALLOW_SEA:
                break;
            case DEEP_SEA:
                break;
            case ISLANDS:
                tile.addBaseResources(0,0,1,1,0,0,0);
                numSpaces -= 2;
                break;
            case PLAINS:
                tile.addBaseResources(1,0,0,0,0,0,0);
                numSpaces += 2;
                break;
            case HILLS:
                tile.addBaseResources(0,1,0,0,0,0,0);
                break;
            case CLIFFS:
                tile.addBaseResources(0,1,0,0,0,0,0);
                numSpaces -= 1;
                break;
            case MOUNTAINS:
                tile.addBaseResources(-1,1,1,0,0,0,0);
                numSpaces -= 2;
                break;
            default:
                System.err.println("Invalid terrain");
                break;
        }
        tile.numSpaces = numSpaces;
        return tile;
    }

}
