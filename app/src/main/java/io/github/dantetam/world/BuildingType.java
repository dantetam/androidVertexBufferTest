package io.github.dantetam.world;

import java.util.HashMap;

/**
 * Created by Dante on 7/21/2016.
 */
public enum BuildingType {
    //WHEAT_PLOT (0, "Wheat Plot"),
    //SHALLOW_MINE (1, "Shallow Mine");
    CITY (0, "City"),
    CITY_HOUSE (1, "House"),
    CITY_TENT (2, "Tent"),
    CITY_TRAINING_GROUND (3, "Training Ground"),
    FARM (10, "Farm"),
    MINE (15, "Mine"),
    MINE_TUNNEL (16, "Tunnel"),
    MINE_PIT (17, "Pit"),
    MINE_QUARRY (18, "Quarry"),
    MINE_STORAGE (19, "Warehouse"),
    SMITH (20, "Smith"),
    SMITH_FURNACES (21, "Metal Furnace"),
    SMITH_ANVILS (22, "Anvils"),
    SMITH_STORAGE (23, "Warehouse"),
    WORKSHOP (30, "Workshop"),
    WORKSHOP_FURNACES (31, "Fuel Furnace"),
    WORKSHOP_SAW (32, "Saw"),
    WORKSHOP_STORAGE (33, "Warehouse"),
    GRANARY (40, "Granary"),
    GRANARY_BAKERY (41, "Bakery"),
    GRANARY_STORAGE (42, "Warehouse");
    public int id; public String name;
    BuildingType(int t, String n) {id = t; name = n;}
    private static BuildingType[] rawTypes = BuildingType.class.getEnumConstants();
    public static HashMap<Integer, BuildingType> types = null;
    public static HashMap<String, BuildingType> typesByName = null;
    public static BuildingType fromInt(int n) {
        if (types == null) {
            init();
        }
        if (types.containsKey(n)) {
            return types.get(n);
        }
        throw new IllegalArgumentException("Invalid terrain type: " + n);
    }
    public static BuildingType fromString(String n) {
        if (typesByName == null) {
            init();
        }
        if (typesByName.containsKey(n)) {
            return typesByName.get(n);
        }
        for (BuildingType item: BuildingType.values()) {
            if (item.name.equalsIgnoreCase(n)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid building type: " + n);
    }
    public String objResourceName() {
        return name.toLowerCase().replaceAll(" ", "_");
    }
    private static void init() {
        types = new HashMap<>();
        for (int i = 0; i < rawTypes.length; i++) {
            types.put(rawTypes[i].id, rawTypes[i]);
        }
        typesByName = new HashMap<>();
        for (int i = 0; i < rawTypes.length; i++) {
            typesByName.put(rawTypes[i].name, rawTypes[i]);
        }
    }
    private static final int numBuildingTypes = 0;
    public static final int getNumBuildingTypes() {
        if (numBuildingTypes == 0) init();
        return types.size();
    }
    public static BuildingType randomBuilding() {
        return BuildingType.fromInt((int) (Math.random() * numBuildingTypes));
    }
}
