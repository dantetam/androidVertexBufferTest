package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 7/21/2016.
 */
public enum ItemType {
    NO_RESOURCE (-1),

    FOOD (0),
    PRODUCTION (1),
    SCIENCE (2),
    CAPITAL (3),
    LABOR (4),
    NECESSITY (5),
    LUXURY (6),

    WHEAT (10),
    FISH (11),

    BRANCHES (50),
    LOGS (51),
    ROCKS (60),

    ICE (99),
    STONE (100),
    CLAY (101),
    SAND (102),

    COPPER_ORE (150),
    IRON_ORE (151),
    COAL (152),

    BREAD (200),
    LUMBER (210),
    BRICK (220),
    GLASS (225),
    METAL (230),
    STEEL (231),

    TOOLS (240),
    STRONG_TOOLS (241),
    WEAPONS (245),
    STRONG_WEAPONS (246);
    public static int[] ranges = {-1,0,10,50,99,150,200,999999};
    public static String[] nameRanges = {"NoResource", "Base", "RawFood", "OrganicMaterial", "NaturalMaterial", "RawMetal", "Processed"};
    public static boolean withinCategory(String target, int id) {
        int targetIndex = -1;
        for (int i = 0; i < nameRanges.length; i++) {
            if (nameRanges[i].equalsIgnoreCase(target)) {
                targetIndex = i;
            }
        }
        if (targetIndex == -1) {
            throw new IllegalArgumentException("Could not find target string item category: " + target);
        }
        return id >= ranges[targetIndex] && id < ranges[targetIndex + 1];
    }
    public static List<Item> itemsWithinCategory(Tile tile, String target) {
        List<Item> items = new ArrayList<>();
        for (Item item: tile.resources) {
            if (withinCategory(target, item.type.id)) {
                items.add(item);
            }
        }
        return items;
    }
    public int id;
    //public int quantity; //For data manipulation purposes, not for the game
    public String renderName;
    ItemType(int n) {
        id = n;
    }
    /*ItemType(int i, int q) {
        id = i;
        quantity = q;
    }*/
    ItemType(ItemType type) {
        id = type.id;
        renderName = type.renderName;
    }
    private static HashMap<Integer, ItemType> types;
    private static String[] names = {
            "No resource",
            "Food",
            "Production",
            "Science",
            "Capital",
            "Labor",
            "Necessity",
            "Luxury",
            "Wheat",
            "Fish",
            "Branches",
            "Logs",
            "Rocks",
            "Ice",
            "Stone",
            "Clay",
            "Sand",
            "Copper Ore",
            "Iron Ore",
            "Coal",
            "Bread",
            "Lumber",
            "Brick",
            "Glass",
            "Metal",
            "Steel",
            "Tools",
            "Strong Tools",
            "Weapons",
            "Strong Weapons"
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
    public String toString() {
        return types.get(id).renderName;
    }

    public String getAndroidResourceName() {
        return renderName.replace(" ", "_").toLowerCase();
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
