package io.github.dantetam.world;

import java.util.ArrayList;

/**
 * Created by Dante on 7/17/2016.
 */
public class BuildingFactory {

    public static Building newTile(World world, Clan clan, BuildingType type, Tile tile, double completionPercentage) {
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
                System.err.println("Invalid biome");
                break;
        }

        build.actionPoints = actionPoints;
        build.maxActionPoints = maxActionPoints;

        build.workNeeded = workNeeded;
        build.workCompleted = workNeeded*completionPercentage;

        build.move(tile);
        return build;
    }

}
