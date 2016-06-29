package io.github.dantetam.opstrykontest;

import java.util.List;

import io.github.dantetam.world.Building;
import io.github.dantetam.world.DiamondSquare;
import io.github.dantetam.world.Tile;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 6/17/2016.
 * This is intended to be the site of all methods that generate data
 * and affect the given world. It "latches" onto a world and then transforms it accordingly.
 */
public class WorldGenerator {

    private World world;

    public WorldGenerator(World w) {
        world = w;
    }

    /**
     * Initialize the world (declared in constructor).
     * This method should take in seeds that are not hard coded,
     * generate an appropriately sized set of data, which is used to set features
     * such as landscape, biome, etc.
     */
    public void init() {
        int width = Math.max(world.arrayLengthX, world.arrayLengthZ);
        int[][] biomes = new DiamondSquare(width, 10, 0.4).seed(870).getIntTerrain(0, Tile.Biome.numBiomes - 1);
        int[][] terrains = new DiamondSquare(width, 10, 0.4).seed(0417).getIntTerrain(0, Tile.Terrain.numTerrains - 1);
        Tile.Resource[][] resources = makeNewResources(width, width);
        int[][] elevations = new DiamondSquare(width, 10, 0.5).seed(916).getIntTerrain(1, 10);
        world.init(biomes, terrains, resources, elevations);
        makeRandomBuildings();
    }

    /**
     * @param rows,cols Size of world to be worked with
     * @return a Tile.Resource[][]. We specifically work with Tile.Resource and not int, for convenience.
     */
    private Tile.Resource[][] makeNewResources(int rows, int cols) {
        Tile.Resource[][] temp = new Tile.Resource[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile.Resource index;
                if (Math.random() < 0.1) {
                    index = Tile.Resource.randomResource();
                }
                else {
                    index = Tile.Resource.NO_RESOURCE;
                }
                temp[r][c] = index;
            }
        }
        return temp;
    }

    /*
    Initialize some buildings to the tiles for testing purposes.
     */
    private void makeRandomBuildings() {
        List<Tile> tiles = world.getAllValidTiles();
        for (Tile tile: tiles) {
            if (Math.random() < 0.3 && tile != null) {
                Building building = new Building(tile, Building.BuildingType.randomBuilding());
                //building.buildingType = Building.BuildingType.randomBuilding();
                //building.move(tile);
            }
        }
    }

}
