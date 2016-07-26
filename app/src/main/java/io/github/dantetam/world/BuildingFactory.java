package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by Dante on 7/17/2016.
 */
public class BuildingFactory {

    public static City newCity(World world, Clan clan, Tile tile, Collection<Tile> cityTiles) {
        City city = new City(world, clan, BuildingType.CITY, cityTiles);
        city.modules = new Building[tile.numSpaces];

        city.actionPoints = 1;
        city.maxActionPoints = 1;
        city.workNeeded = 1;
        city.workCompleted = 1;

        city.move(tile);
        return city;
    }

    public static Building newBuilding(World world, Clan clan, BuildingType type, Tile tile, double completionPercentage) {
        return newBuilding(world, clan, type, tile, completionPercentage, null);
    }

    public static Building newModule(World world, Clan clan, BuildingType type, Tile tile, double completionPercentage, Building parent) {
        return newBuilding(world, clan, type, tile, completionPercentage, parent);
    }

    private static Building newBuilding(World world, Clan clan, BuildingType type, Tile tile, double completionPercentage, Building parent) {
        Building build = new Building(world, clan, type);
        build.modules = new Building[tile.numSpaces];

        int actionPoints = 1, maxActionPoints = 1;
        double workNeeded = 15;

        switch (type) {
            case FARM:
                build.addInput(ItemType.FOOD, 1);
                build.addInput(ItemType.LABOR, 1);
                for (Item item: ItemType.itemsWithinCategory(tile, "RawFood")) {
                    build.addOutput(item.type, 1);
                }
                break;
            default:
                System.err.println("Invalid building type for BuildingFactory");
                break;
        }

        build.actionPoints = actionPoints;
        build.maxActionPoints = maxActionPoints;

        build.workNeeded = workNeeded;
        build.workCompleted = workNeeded*completionPercentage;

        if (parent != null) {
            build.addThisAsModuleToBuilding(parent);
        }
        else {
            build.move(tile);
        }
        return build;
    }

}
