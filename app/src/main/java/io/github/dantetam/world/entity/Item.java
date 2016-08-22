package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.opstrykontest.Condition;
import io.github.dantetam.opstrykontest.WorldGenerator;

/**
 * Created by Dante on 6/16/2016.
 */
public class Item {

    public String name;
    public ItemType type;
    public int quantity;

    public Item(ItemType t, int n) {
        type = t;
        name = t.renderName;
        quantity = n;
    }

    public Item(ItemType t) {
        this(t, 1);
    }

    /*public static void generateResourcesForTile(Tile tile) {

    }*/

    /**
     * Takes in the raw float data, consisting of percentages linked to each biome/terrain combination.
     * This turns the data into a set of conditions linked to each ItemType, such that if the
     * overridden condition is fulfilled, then allow the ItemType. This last bit is actually implemented in
     * evaluateResourceConditions().
     * @return all the items listed in the resource file, linked to the biome/terrain percentages
     */
    public static HashMap<ItemType, List<Condition>> conditionsForTile() {
        HashMap<ItemType, List<Condition>> conditions = new HashMap<>();
        HashMap<ItemType, float[][]> resourceSpawnRates = WorldGenerator.parseResourceSpawnRates();
        for (Map.Entry<ItemType, float[][]> entry: resourceSpawnRates.entrySet()) {
            float[][] data = entry.getValue();
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    Condition cond = new Condition() {
                        public int biomeNum, terrainNum;
                        public float chance;
                        public boolean allowedTile(Tile tile) {
                            return tile.biome.equals(Tile.Biome.fromInt(biomeNum)) &&
                                    tile.terrain.equals(Tile.Terrain.fromInt(terrainNum)) &&
                                    Math.random() < chance;
                        }
                        public void init(int a, int b, float c) {biomeNum = a; terrainNum = b; chance = c;}
                    };
                    cond.init(i, j, data[i][j]);
                    List<Condition> resourceConditions = conditions.get(entry.getKey());
                    if (resourceConditions != null) {
                        resourceConditions.add(cond);
                    }
                    else {
                        resourceConditions = new ArrayList<>();
                        resourceConditions.add(cond);
                        conditions.put(entry.getKey(), resourceConditions);
                    }
                }
            }
        }
        /*if (tile.biome == Tile.Biome.SEA) {

        }
        else if (tile.biome == Tile.Biome.ICE) {
            conditions.put("Ice", new Condition() {
                public boolean allowedTile(Tile tile) {
                    return true;
                }
            });
        }
        else if (tile.biome == Tile.Biome.TUNDRA) {

        }
        else if (tile.biome == Tile.Biome.DESERT) {
            conditions.put("Sand", new Condition() {
                public boolean allowedTile(Tile tile) {
                    return Math.random() < 0.7;
                }
            });
        }
        else if (tile.biome == Tile.Biome.STEPPE) {

        }
        else if (tile.biome == Tile.Biome.FOREST) {

        }
        else if (tile.biome == Tile.Biome.RAINFOREST) {

        }*/
        return conditions;
    }

    /**
     * @param tile The tile for the conditions to be evaluated over
     * @param possibleResources A set of conditions (biome/terrain) linked to each ItemType
     * @return the set of resources generated on this tile per the conditions
     */
    public static List<ItemType> evaluateResourceConditions(Tile tile, HashMap<ItemType, List<Condition>> possibleResources) {
        List<ItemType> items = new ArrayList<>();
        for (Map.Entry<ItemType, List<Condition>> en: possibleResources.entrySet()) {
            ItemType itemToAdd = en.getKey();
            List<Condition> conditions = en.getValue();
            for (Condition condition: conditions) {
                if (condition.allowedTile(tile)) {
                    items.add(itemToAdd);
                }
            }
        }
        return items;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Item)) {
            return false;
        }
        Item item = (Item) other;
        return type == item.type && quantity == item.quantity;
    }

    public String toString() {
        if (quantity == 1) {
            return type.toString();
        }
        return quantity + " " + type.toString();
    }

}
