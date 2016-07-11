package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.opstrykontest.Condition;

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

    public static HashMap<String, Condition> conditionsForTile(Tile tile) {
        HashMap<String, Condition> conditions = new HashMap<>();
        if (tile.biome == Tile.Biome.ICE) {
            conditions.put("Ice", new Condition() {
                public boolean allowedTile(Tile tile) {
                    return true;
                }
            });
        }
        else if (tile.biome == Tile.Biome.DESERT) {
            conditions.put("Sand", new Condition() {
                public boolean allowedTile(Tile tile) {
                    return Math.random() < 0.7;
                }
            });
        }
        return conditions;
    }

    public static List<ItemType> evaluateResourceConditions(Tile tile, HashMap<String, Condition> possibleResources) {
        List<ItemType> items = new ArrayList<>();
        for (Map.Entry<String, Condition> en: possibleResources.entrySet()) {
            if (en.getValue().allowedTile(tile)) {
                items.add(ItemType.fromString(en.getKey()));
            }
        }
        return items;
    }

}
