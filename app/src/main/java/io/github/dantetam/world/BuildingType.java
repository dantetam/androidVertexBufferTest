package io.github.dantetam.world;

import java.util.HashMap;

/**
 * Created by Dante on 7/21/2016.
 */
public enum BuildingType {
    //WHEAT_PLOT (0, "Wheat Plot"),
    //SHALLOW_MINE (1, "Shallow Mine");
    ENCAMPMENT (0, "Encampment"),
    FARM (10, "Farm");
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
        throw new IllegalArgumentException("Invalid terrain type: " + n);
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
