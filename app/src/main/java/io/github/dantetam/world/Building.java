package io.github.dantetam.world;

import java.util.HashMap;

/**
 * Created by Dante on 6/16/2016.
 */
public class Building extends Entity {

    public enum BuildingType {
        //WHEAT_PLOT (0, "Wheat Plot"),
        //SHALLOW_MINE (1, "Shallow Mine");
        FARM1 (0, "Farm1"),
        FARM2 (1, "Farm2"),
        FARM3 (2, "Farm3"),
        FARM4 (3, "Farm4");
        public int id; public String name;
        BuildingType(int t, String n) {id = t; name = n;}
        private static BuildingType[] rawTypes = BuildingType.class.getEnumConstants();
        public static HashMap<Integer, BuildingType> types = null;
        public static BuildingType fromInt(int n) {
            if (types == null) {
                init();
            }
            if (types.containsKey(n)) {
                return types.get(n);
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

    public BuildingType buildingType;

    public Building(Tile t, BuildingType type) {
        //super(t);
        //move(t);
        move(t);
        name = type.name;
    }

    public void move(Tile t) {
        if (location != null) {
            location.improvement = null;
        }
        t.improvement = this;
        super.location = t;
        //super.move(t);
    }

}
