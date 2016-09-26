package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.android.FileParser;
import io.github.dantetam.android.RawResourceReader;
import io.github.dantetam.utilmath.DiamondSquare;
import io.github.dantetam.utilmath.Vector2f;
import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.factory.BuildingFactory;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.factory.ClanFactory;
import io.github.dantetam.world.entity.Item;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.factory.PersonFactory;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;
import io.github.dantetam.world.terrain.TerrainUtil;
import io.github.dantetam.xml.BuildingXmlParser;
import io.github.dantetam.xml.IdeologyXmlParser;
import io.github.dantetam.xml.ResourceXmlParser;
import io.github.dantetam.xml.TechXmlParser;
import io.github.dantetam.xml.UnitXmlParser;

/**
 * Created by Dante on 6/17/2016.
 * This is intended to be the site of all methods that generate data
 * and affect the given world. It "latches" onto a world and then transforms it accordingly.
 */
public class WorldGenerator {

    private static LessonSevenActivity mActivity;
    private World world;

    public WorldGenerator(LessonSevenActivity mActivity, World w) {
        this.mActivity = mActivity;
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
        int[][] biomes = new DiamondSquare(width, 16, 0.4).seed(System.currentTimeMillis()/4).getIntTerrain(0, Tile.Biome.numBiomes - 1);
        int[][] terrains = new DiamondSquare(width, 12, 0.4).seed(System.currentTimeMillis()).getIntTerrain(0, Tile.Terrain.numTerrains - 1);
        //Item[][] resources = makeNewResources(width, width);
        int[][] elevations = new DiamondSquare(width, 10, 0.5).seed(System.currentTimeMillis()/2).getIntTerrain(0, 10);
        TerrainUtil.printIntTable(elevations);
        world.init(biomes, terrains, elevations);

        TerrainUtil.printIntTable(elevations);
        //makeRandomBuildings();
        world.initClans(makeClans());
        makeNewResources(world);
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
        for (Tile tile: world.getAllValidTiles()) {
            HashMap<ItemType, List<Condition>> conditions = Item.conditionsForTile();
            List<ItemType> resources = Item.evaluateResourceConditions(tile, conditions);
            for (ItemType itemType: resources) {
                tile.resources.add(new Item(itemType, 1));
            }
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
        int num = world.getAllValidTiles().size() / 40;
        for (int i = 0; i < num; i++) {
            Clan clan;
            if (i == 0) {
                clan = ClanFactory.randomAvailableClan();
                //clan.name = "PlayerClan";
            }
            else {
                clan = ClanFactory.randomAvailableClan();
            }

            clans.add(clan);
            UnitXmlParser.parseUnitTree(clan, mActivity, R.raw.unit_tree);
            BuildingXmlParser.parseBuildingTree(clan, mActivity, R.raw.building_tree);
            clan.techTree = new TechTree(clan);

            clan.ideologyTree = IdeologyXmlParser.parseIdeologyTree(R.raw.ideology_tree);

            if (TechTree.itemTypes == null) {
                ResourceXmlParser.parseResourceTree(clan.techTree, mActivity, R.raw.resource_tree);
            }

            TechXmlParser.parseTechTree(clan.techTree, mActivity, R.raw.tech_tree, R.raw.tech_tree_layout);
            clan.techTree.unlock("Landing");
        }
        for (int i = 0; i < 2*num; i++) {
            CityState cityState = ClanFactory.randomAvailableCityState();
            clans.add(cityState);
            UnitXmlParser.parseUnitTree(cityState, mActivity, R.raw.unit_tree);
            BuildingXmlParser.parseBuildingTree(cityState, mActivity, R.raw.building_tree);
            cityState.techTree = new TechTree(cityState);

            if (TechTree.itemTypes == null) {
                ResourceXmlParser.parseResourceTree(cityState.techTree, mActivity, R.raw.resource_tree);
            }

            TechXmlParser.parseTechTree(cityState.techTree, mActivity, R.raw.tech_tree, R.raw.tech_tree_layout);
            //cityState.techTree.unlock("Landing");

            cityState.techTree.unlock("Landing");
            cityState.techTree.allowedUnits.remove("Settler");
        }
        return clans;
    }

    private void setClanLands(World world) {
        List<Clan> clans = world.getClans();
        List<Tile> clanStartingLocations = new ArrayList<>();
        //List<Tile> validTiles = world.getAllValidTiles();
        List<Tile> validTiles = world.getAllLandTiles();
        Tile start = validTiles.get((int)(Math.random()*validTiles.size()));

        clanStartingLocations.add(start);
        for (int i = 0; i < clans.size() - 1; i++) {
            //Find the averaged arithmetic center of the existing conditions
            /*float centerQ = 0, centerR = 0;
            for (Tile t: clanStartingLocations) {
                centerQ += t.q;
                centerR += t.r;
            }
            centerQ /= clanStartingLocations.size();
            centerR /= clanStartingLocations.size();

            Tile furthest = findFurthestTileAway(world, Math.round(centerQ), Math.round(centerR));
            while (true) {
                boolean contains = false;
                for (Tile clanCenter: clanStartingLocations) {
                    List<Tile> neighbors = clanCenter.neighbors();
                    neighbors.add(clanCenter);
                    contains = contains || neighbors.contains(furthest);
                }
                if (contains) {
                    furthest = validTiles.get((int) (Math.random() * validTiles.size()));
                }
                else {
                    break;
                }
            }
            clanStartingLocations.add(furthest);*/
            while (true) {
                Tile candidate = validTiles.get((int) (Math.random() * validTiles.size()));
                //if (clanStartingLocations.size() == 0) break;
                boolean allowed = true;
                for (Tile startTile : clanStartingLocations) {
                    if (startTile.dist(candidate) < 3) {
                        allowed = false;
                    }
                }
                if (allowed) {
                    clanStartingLocations.add(candidate);
                    break;
                }
            }
        }

        for (int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            Tile clanHome = clanStartingLocations.get(i);
            //world.setTileOwner(clanHome);
            Collection<Tile> territory = world.getRing(clanHome, 1);
            for (Tile territoryTile: territory) {
                if (world.getTileOwner(territoryTile) == null) {
                    world.setTileOwner(territoryTile, clan);
                }
            }
            //Building first = BuildingFactory.newBuilding(world, clan, BuildingType.ENCAMPMENT, clanHome, 1);
            City firstCity = BuildingFactory.newCity(world, clan, clanHome, territory);
            firstCity.isCapital = clan;
            firstCity.pickBestTiles();
            //first.move(clanHome);

            Person unit = PersonFactory.newPerson(clan.unitTree.personTypes.get("Soldier"), world, clan, 1.0);
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
        List<Tile> tiles = world.getAllLandTiles();
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

    private static HashMap<ItemType, float[][]> resourceSpawnRates;

    public static HashMap<ItemType, float[][]> parseResourceSpawnRates() {
        if (resourceSpawnRates == null) {
            resourceSpawnRates = new HashMap<>();
            List<String> data = RawResourceReader.loadListOfText(mActivity, R.raw.resource_spawn_rates);
            int numBiomes = Tile.Biome.numBiomes, numTerrains = Tile.Terrain.numTerrains;
            int biomeCounter = 0;
            String resource = "";
            String currentAction = "";
            float[][] resourceData = null;
            for (int i = 0; i < data.size(); i++) {
                String stringy = data.get(i);
                if (stringy.isEmpty() || stringy.equals("") || stringy.startsWith("//")) continue;
                if (stringy.startsWith("Resource/")) {
                    currentAction = "Resource";
                    if (resourceData != null) {
                        resourceSpawnRates.put(TechTree.itemTypes.get(resource), resourceData);
                    }
                    resource = stringy.substring(9);
                    resourceData = new float[numBiomes][numTerrains];
                } else if (stringy.startsWith("ResourceQuick/")) {
                    currentAction = "ResourceQuick";
                    if (resourceData != null) {
                        resourceSpawnRates.put(TechTree.itemTypes.get(resource), resourceData);
                    }
                    resource = stringy.substring(14);
                    resourceData = new float[numBiomes][numTerrains];
                } else if (currentAction.equals("Resource")) {
                    //System.out.println(stringy + "<end");
                    String[] values = stringy.split(" ");
                    float[] parsed = new float[values.length];
                    for (int j = 0; j < values.length; j++) {
                        parsed[j] = Float.parseFloat(values[j]);
                    }
                    resourceData[biomeCounter] = parsed;
                    biomeCounter++;
                    if (biomeCounter == numBiomes) {
                        biomeCounter = 0;
                    }
                } else if (currentAction.equals("ResourceQuick")) {
                    //System.out.println(stringy + "<end");
                    String[] biomeValues = data.get(i).split(" ");
                    String[] terrainValues = data.get(i+1).split(" ");
                    float[] biomeValuesParsed = new float[biomeValues.length];
                    float[] terrainValuesParsed = new float[terrainValues.length];
                    for (int j = 0; j < biomeValues.length; j++) {
                        biomeValuesParsed[j] = Float.parseFloat(biomeValues[j]);
                    }
                    for (int j = 0; j < terrainValues.length; j++) {
                        terrainValuesParsed[j] = Float.parseFloat(terrainValues[j]);
                    }

                    for (int biome = 0; biome < numBiomes; biome++) {
                        for (int terrain = 0; terrain < numTerrains; terrain++) {
                            float calculated = biomeValuesParsed[biome]*terrainValuesParsed[terrain];
                            resourceData[biome][terrain] = calculated;
                        }
                    }

                    i++;
                }
            }
        }
        return resourceSpawnRates;
    }

}
