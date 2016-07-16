package io.github.dantetam.world;

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

    public enum ItemType {
        NO_RESOURCE (-1),

        WHEAT (0),
        FISH (10),

        BRANCHES (50),
        LOGS (51),

        ICE (99),
        STONE (100),
        CLAY (101),
        SAND (102),

        COPPER (150),
        IRON (151),
        COAL (152),
        ;
        public int id;
        public String renderName;
        ItemType(int n) {
            id = n;
        }
        ItemType(ItemType type) {
            id = type.id;
            renderName = type.renderName;
        }
        private static HashMap<Integer, ItemType> types;
        private static String[] names = {
                "No resource",
                "Wheat",
                "Fish",
                "Branches",
                "Logs",
                "Ice",
                "Stone",
                "Clay",
                "Sand",
                "Copper",
                "Iron",
                "Coal"
        };
        public static int numItems;

        public static void init() {
            types = new HashMap<>();
            ItemType[] allEnum = ItemType.values();
            for (int i = 0; i < allEnum.length; i++) {
                ItemType item = allEnum[i];
                item.renderName = names[i];
                types.put(item.id, item);
            }
            numItems = types.size();
        }

        public static ItemType fromString(String name) {
            if (types == null) {
                init();
            }
            for (ItemType item: values()) {
                if (item.renderName.equalsIgnoreCase(name)) {
                    return item;
                }
            }
            System.out.println("Could not find resource name: " + name);
            return null;
        }

        public static ItemType fromInt(int n) {
            if (types == null) {
                init();
            }
            if (n >= 0 && n < numItems) {
                return types.get(n);
            }
            throw new IllegalArgumentException("Invalid item type: " + n);
        }
        public static String nameFromInt(int n) {
            if (n >= 0 && n < names.length) {
                return names[n];
            }
            throw new IllegalArgumentException("Invalid item type: " + n);
        }
        public static ItemType randomResource() {
            return ItemType.fromInt((int) (Math.random() * (numItems - 1)) + 1);
        }
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
                    conditions.get(entry.getKey()).add(cond);
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

}
