package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.action.BuildingAction;
import io.github.dantetam.world.factory.BuildingFactory;
import io.github.dantetam.world.factory.PersonFactory;

/**
 * Created by Dante on 7/25/2016.
 */
public class City extends Building {

    public Clan isCapital;

    private int population, freeWorkingPopulation;
    public int population() {return population;}
    public int freeWorkingPopulation() {return freeWorkingPopulation;}
    public int foodStoredForGrowth, foodNeededForGrowth;

    public int lastYieldHealth = 4;

    //where generateCityFoodData[n] represents the needed food to go from n-1 to n
    private static int[] cityFoodData = null;
    public static int[] cityFoodData() {
        if (cityFoodData == null) {
            initCityData();
        }
        return cityFoodData;
    }

    public static void initCityData() {
        if (cityFoodData == null) {
            cityHealthData = new int[100];
            cityHealthData[0] = 0;
            cityFoodData = new int[100];
            cityFoodData[0] = 0;
            for (int i = 1; i < cityFoodData.length; i++) {
                cityHealthData[i] = 50 + i*5;
                cityFoodData[i] = 10 + i*5;
            }
        }
    }

    private static int[] cityHealthData = null;
    public static int[] cityHealthData() {
        if (cityHealthData == null) {
            initCityData();
        }
        return cityHealthData;
    }

    public void updateCityData() {
        foodNeededForGrowth = City.cityFoodData()[population];
        //cultureNeededForExpansion = City.cityHealthData()[population];
    }

    public int tilesExpanded = 0;
    public int cultureStoredForExpansion, cultureNeededForExpansion;

    private static int[] cultureExpansionData = null;
    public static int[] cultureExpansionData() {
        if (cultureExpansionData == null) {
            cultureExpansionData = new int[30];
            for (int i = 0; i < cultureExpansionData.length; i++) {
                cultureExpansionData[i] = 10 + (int)Math.pow(i, 1.1)*5;
            }
        }
        return cultureExpansionData;
    }

    public HashMap<Tile, Boolean> workedTiles;
    public Collection<Tile> cityTiles;

    public City(World world, Clan clan, String name, BuildingType type, Collection<Tile> tiles) {
        super(world, clan, type);
        this.name = name;
        workedTiles = new HashMap<>();
        cityTiles = tiles;

        population = 1;
        freeWorkingPopulation = 1;
    }

    public void queueActionBuildModule(BuildingType buildingType) {
        if (buildingType.resourceNeeded != null) {
            clan.resources.subtractFromInventory(new Item(TechTree.itemTypes.get(buildingType.resourceNeeded)));
        }
        Building queueBuilding = BuildingFactory.newBuilding(world, clan, buildingType, location, 0);
        actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, queueBuilding));
    }

    public void queueActionBuildUnit(PersonType personType) {
        if (personType.resourceNeeded != null) {
            clan.resources.subtractFromInventory(new Item(TechTree.itemTypes.get(personType.resourceNeeded)));
        }
        Person queuePerson = PersonFactory.newPerson(personType, world, clan, 0);
        actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_UNIT, queuePerson));
    }

    public List<BuildingType> computePossibleBuildingsForCity() {
        TechTree tree = this.clan.techTree;
        List<BuildingType> results = new ArrayList<>();
        for (Map.Entry<BuildingType, Boolean> entry: tree.allowedBuildings.entrySet()) {
            BuildingType buildingType = entry.getKey();
            if (entry.getValue()) {
                if (buildingType.resourceNeeded != null) {
                    ItemType resourceNeeded = TechTree.itemTypes.get(buildingType.resourceNeeded);
                    if (!clan.resources.hasItemInInventory(resourceNeeded, false)) {
                        continue;
                    }
                }
                boolean foundCopy = false;
                for (Building building: modules) {
                    if (building.buildingType.equals(buildingType)) {
                        foundCopy = true;
                        break;
                    }
                }
                if (!foundCopy)
                    results.add(buildingType);
            }
        }
        return results;
    }

    public List<PersonType> computePossibleUnitsForCity() {
        TechTree tree = this.clan.techTree;
        List<PersonType> results = new ArrayList<>();
        for (Map.Entry<PersonType, Boolean> entry: tree.allowedUnits.entrySet()) {
            PersonType personType = entry.getKey();
            if (entry.getValue()) {
                if (buildingType.resourceNeeded != null) {
                    ItemType resourceNeeded = TechTree.itemTypes.get(buildingType.resourceNeeded);
                    if (!clan.resources.hasItemInInventory(resourceNeeded, false)) {
                        continue;
                    }
                }
                results.add(personType);
            }
        }
        return results;
    }

    public void executeQueue() {
        /*while (true) {
            if (actionsQueue.size() == 0) {
                return;
            }
            Action action = actionsQueue.get(0);
            Action.ActionStatus status = action.execute(this);

            if (status == Action.ActionStatus.ALREADY_COMPLETED || status == Action.ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            }
            else if (status == Action.ActionStatus.IMPOSSIBLE) {
                actionsQueue.remove(0);
                //TODO: Error code? Print info about errant action?
            }
            else if (status == Action.ActionStatus.OUT_OF_ENERGY) {
                break;
            }
            else if (status == Action.ActionStatus.CONTINUING) {
                //do nothing, keep the action in the first slot, it'll be repeated.
            }
        }*/
    }

    public Object[] gameYield() {
        if (freeWorkingPopulation > 0) {
            pickBestTiles();
        }

        double[] totalTileYield = new double[7];
        Inventory inventory = new Inventory();

        HashMap<ItemType, Boolean> allowedHarvestable = clan.techTree.allowedHarvestable;

        for (Tile tile: workedTiles.keySet()) {
            int[] tileYield = evalTile(tile);
            for (int i = 0; i < tileYield.length; i++) {
                totalTileYield[i] += tileYield[i];
            }
            if (tile.improvement != null) {
                int[] imprYield = tile.improvement.getYieldWithModules();
                for (int i = 0; i < imprYield.length; i++) {
                    tileYield[i] += imprYield[i];
                }
                for (Recipe recipe: tile.improvement.recipes) {
                    for (Item item: recipe.output) {
                        if (allowedHarvestable.containsKey(item)) {
                            inventory.addToInventory(item);
                        }
                    }
                }
            }
        }
        //System.out.println(food + " " + production + " " + science + " " + capital);

        //lastYield = new int[]{(int)food, (int)production, (int)science, (int)capital};

        int[] intYield = new int[totalTileYield.length];
        for (int i = 0; i < intYield.length; i++) {
            intYield[i] = (int) totalTileYield[i];
        }

        return new Object[]{intYield, inventory};
    }

    public static int[] evalTile(Tile tile) {
        int[] calcYield = tile.yield();
        int[] temp = new int[calcYield.length];
        System.arraycopy(calcYield, 0, temp, 0, calcYield.length);
        if (tile.improvement != null) {
            int[] imprYield = tile.improvement.getYieldWithModules();
            for (int i = 0; i < imprYield.length; i++) {
                temp[i] += imprYield[i];
            }
            /*for (Recipe recipe: tile.improvement.recipes) {
                for (Item item: recipe.output) {
                    if (allowedHarvestable.containsKey(item)) {
                        clan.resources.addToInventory(item);
                    }
                }
            }*/
        }
        return temp;
    }

    public void addTileToTerritory(Tile t) {
        if (world.getTileOwner(t) == null && !cityTiles.contains(t)) {
            cityTiles.add(t);
        }
    }

    public void pickBestTiles() {
        //workedTiles.clear();
        freeWorkingPopulation = population - workedTiles.size();
        TreeMap<Tile, Double> scoreTiles = new TreeMap<>(new Comparator<Tile>() {
            public int compare(Tile lhs, Tile rhs) {
                return lhs.hashCode() - rhs.hashCode();
            }
        });
        for (Tile tile: cityTiles) {
            scoreTiles.put(tile, scoreTile(tile));
        }
        Map<Tile, Double> sorted = OpstrykonUtil.sortMapByValue(scoreTiles);
        Set<Tile> tilesToPick = sorted.keySet();
        for (Tile tile: tilesToPick) {
            if (freeWorkingPopulation > 0) {
                pickTile(tile);
            }
            else {
                break;
            }
        }

        /*for (Map.Entry<Tile, Double> en: sorted.entrySet()) {
            System.out.println(en.getKey().toString() + " " + en.getValue());
        }*/
    }

    public double scoreTile(Tile tile) {
        double score = 0;
        int[] weights = {3, 3, 2, 1, 1, 1, 1};
        int[] tileYield = tile.yield();
        for (int i = 0; i < tileYield.length; i++) {
            score += weights[i] * tileYield[i];
        }
        if (tile.resources.size() > 0) {
            score += tile.resources.size()*3;
        }
        return score;
    }

    public boolean pickTile(Tile t) {
        if (workedTiles.get(t) == null && cityTiles.contains(t) && freeWorkingPopulation > 0) {
            workedTiles.put(t, true);
            freeWorkingPopulation--;
            return true;
        }
        return false;
    }

    public boolean freeTile(Tile t) {
        if (workedTiles.get(t) != null && cityTiles.contains(t)) {
            workedTiles.remove(t);
            freeWorkingPopulation++;
            return true;
        }
        return false;
    }

    public void freeAllTiles() {
        workedTiles.clear();
        freeWorkingPopulation = population;
    }

    public void increasePopulation() {
        population++; freeWorkingPopulation++;
        foodNeededForGrowth = City.cityFoodData()[population];
        maxHealth = City.cityFoodData()[population];
    }

    public void expandToBestTile() {
        LinkedHashMap<Tile, Double> tilesByScore = new LinkedHashMap<>();

        List<Tile> expansion = world.getValidExpansionTiles(this);
        for (Tile tile: expansion) {
            tilesByScore.put(tile, scoreTile(tile));
        }

        Map<Tile, Double> sorted = OpstrykonUtil.sortMapByValue(tilesByScore);
        Set<Tile> tilesToPick = sorted.keySet();

        Tile bestTile = tilesToPick.iterator().next();
        addTileToTerritory(bestTile);
        //cityTiles.add(bestTile);
        world.setTileOwner(bestTile, clan);
    }

}
