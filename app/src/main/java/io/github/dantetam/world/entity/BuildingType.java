package io.github.dantetam.world.entity;

import java.util.HashMap;

import io.github.dantetam.world.action.Ability;

/**
 * Created by Dante on 7/21/2016.
 */

public class BuildingType {
    public String name;
    public int workNeeded;
    private int[] yield;
    public int[] yield() {return yield;}
    //public int food, production, science, capital, happiness, health, culture;
    public int food() {return yield[0];}
    public int production() {return yield[1];}
    public int science() {return yield[2];}
    public int capital() {return yield[3];}
    public int happiness() {return yield[4];}
    public int health() {return yield[5];}
    public int culture() {return yield[6];}

    public Ability[] abilities;

    public boolean wonder = false;

    public String resourceNeeded;

    public String iconName;
    public String[] modelName;
    public String textureName;

    public boolean created = false;
    /*public void init() {
        if (!created) {
            types = new HashMap<>();
        }
    }
    public void addPersonType(String name, int a, int d, int m, int f, int s) {
        PersonType newType = new PersonType();
        newType.name = name;
        newType.PersonType(a, d, m, f, s);
        types.put(name, newType);
    }*/
    public BuildingType(String n, int[] data) {
        name = n;
        iconName = "building";
        yield = data;
    }

    public int[] getYield() {
        return yield;
    }

    public boolean equals(Object other) {
        if (!(other instanceof BuildingType)) {
            return false;
        }
        BuildingType type = (BuildingType) other;
        return name.equals(type.name);
    }

    @Override
    public String toString() {
        return name;
    }
}

/*public enum BuildingType {
    //WHEAT_PLOT (0, "Wheat Plot"),
    //SHALLOW_MINE (1, "Shallow Mine");
    CITY              (0,   "City"),

    FARM              (1,   "Farm"),
    MINE              (2,   "Mine"),
    BOATS             (3,   "Boats"),
    WORKSHOP          (4,   "Workshop"),
    LAB               (5,   "Lab"),
    ;
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
    public int[] getYield() {return new int[]{0,0,0,0};}
}*/
