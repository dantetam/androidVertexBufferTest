package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.dantetam.world.Building;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.DiamondSquare;
import io.github.dantetam.world.Entity;
import io.github.dantetam.world.Person;
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
        //makeRandomBuildings();
        world.initClans(makeClans());
        setClanLands(world);
    }

    /**
     * //@param rows,cols Size of world to be worked with
     * @return a Tile.Resource[][]. We specifically work with Tile.Resource and not int, for convenience.
     */
    private void makeNewResources(World world) {
        /*Tile.Resource[][] temp = new Tile.Resource[rows][cols];
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
        return temp;*/
        for (Tile t: world.getAllValidTiles()) {

        }
    }

    /*
    Initialize some buildings to the tiles for testing purposes.
     */
    /*private void makeRandomBuildings() {
        List<Tile> tiles = world.getAllValidTiles();
        for (Tile tile: tiles) {
            if (Math.random() < 0.3 && tile != null) {
                Building building = new Building(null, Building.BuildingType.randomBuilding());
                building.move(tile);
                Person en = new Person();
                en.name = "TestUnit";
                en.move(tile);
                //building.buildingType = Building.BuildingType.randomBuilding();
                //building.move(tile);
            }
        }
    }*/

    private List<Clan> makeClans() {
        List<Clan> clans = new ArrayList<>();
        int num = world.getAllValidTiles().size() / 10;
        for (int i = 0; i < num; i++) {
            Vector4f color = new Vector4f((int)(Math.random()*255f), 0f, (int)(Math.random()*255f), 1f);
            Clan clan = new Clan("Clan" + i, color);
            clan.clanType = Clan.ClanType.random();
            clan.clanFaction = Clan.ClanFaction.random();
            clans.add(clan);
        }
        return clans;
    }

    private void setClanLands(World world) {
        List<Clan> clans = world.getClans();
        List<Tile> clanStartingLocations = new ArrayList<>();
        List<Tile> validTiles = world.getAllValidTiles();
        Tile start = validTiles.get((int)(Math.random()*validTiles.size()));
        clanStartingLocations.add(start);
        for (int i = 0; i < clans.size() - 1; i++) {
            //Find the averaged arithmetic center of the existing conditions
            float centerQ = 0, centerR = 0;
            for (Tile t: clanStartingLocations) {
                centerQ += t.q;
                centerR += t.r;
            }
            centerQ /= clanStartingLocations.size();
            centerR /= clanStartingLocations.size();

            Tile furthest = findFurthestTileAway(world, Math.round(centerQ), Math.round(centerR));
            while (clanStartingLocations.contains(furthest)) {
                furthest = validTiles.get((int)(Math.random()*validTiles.size()));
            }
            clanStartingLocations.add(furthest);
        }

        for (int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            Tile clanHome = clanStartingLocations.get(i);
            Set<Tile> territory = world.getRing(clanHome, 2);
            for (Tile territoryTile: territory) {
                world.setTileOwner(territoryTile, clan);
            }
            Building first = new Building(world, clan, Building.BuildingType.ENCAMPMENT);
            first.move(clanHome);
            Person unit = new Person(world, clan, "Warrior");
            unit.move(clanHome);
        }

        for (Tile tile: validTiles) {
            if (world.getTileOwner(tile) == null) {
                //Find the closest clan if no set owner
                //TODO: Really should write a map or comparator to handle this paradigm more elegantly.
                int indexMin = 0;
                float distMin = -1;
                for (int i = 0; i < clanStartingLocations.size(); i++) {
                    float dist = clanStartingLocations.get(i).dist(tile);
                    if (distMin == -1 || dist < distMin) {
                        indexMin = i;
                        distMin = dist;
                    }
                }
                Clan influencing = clans.get(indexMin);
                world.addTileInfluence(tile, influencing, 2);
            }
        }
    }
    private Tile findFurthestTileAway(World world, int q, int r) {
        List<Tile> tiles = world.getAllValidTiles();
        Vector2f t = new Vector2f(q,r);
        float maxDist = 0;
        Tile maxTile = null;
        for (Tile candidate: tiles) {
            float dist = t.dist(new Vector2f(candidate.q, candidate.r));
            if (maxTile == null || dist > maxDist) {
                maxTile = candidate;
                maxDist = dist;
            }
        }
        return maxTile;
    }

}
