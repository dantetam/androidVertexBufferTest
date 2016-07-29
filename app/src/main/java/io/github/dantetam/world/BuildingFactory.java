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

        city.recipes.add(new Recipe().addIn(new Item(ItemType.FOOD, 1)).addOut(new Item(ItemType.PRODUCTION, 1)));

        city.actionPoints = 1;
        city.maxActionPoints = 1;
        city.workNeeded = 1;
        city.workCompleted = 1;

        city.inventorySpace = 10;

        int[] cityGrowthData = City.generateCityFoodData();
        city.population = 1;
        city.freeWorkingPopulation = 1;
        city.foodNeededForGrowth = cityGrowthData[city.population];
        city.foodStoredForGrowth = 0;

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

        //int food = 0, production = 0, science = 0, capital = 0;
        int[] yield = {0,0,0,0};

        int actionPoints = 1, maxActionPoints = 1;
        double workNeeded = 15;
        int storageSpace = 0;

        /*
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
        */

        switch (type) {
            case FARM:
                yield = new int[]{3,0,0,0};
                //build.addInput(ItemType.FOOD, 1);
                for (Item item: ItemType.itemsWithinCategory(tile, "RawFood")) {
                    build.recipes.add(new Recipe().addIn(new Item(ItemType.FOOD, 1)).addOut(new Item(item.type, 1)));
                }
                break;
            case MINE:
                yield = new int[]{0,2,0,0};
                for (Item item: ItemType.itemsWithinCategory(tile, "RawMetal")) {
                    build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 1)).addOut(new Item(item.type, 1)));
                }
                break;
            case MINE_TUNNEL:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 1)).addOut(new Item(ItemType.COAL, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 1)).addOut(new Item(ItemType.COPPER_ORE, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 2)).addOut(new Item(ItemType.IRON_ORE, 1)));
                break;
            case MINE_PIT:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 1)).addOut(new Item(ItemType.COPPER_ORE, 2)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 1)).addOut(new Item(ItemType.IRON_ORE, 1)));
            case MINE_QUARRY:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.PRODUCTION, 1)).addOut(new Item(ItemType.ROCKS, 1)));
            case MINE_STORAGE:
                storageSpace = 10;
                break;
            case SMITH:
                yield = new int[]{-1,3,0,0};
                break;
            case SMITH_ANVILS:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.METAL, 1)).addOut(new Item(ItemType.TOOLS, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.METAL, 1)).addOut(new Item(ItemType.WEAPONS, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.STEEL, 1)).addOut(new Item(ItemType.STRONG_TOOLS, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.STEEL, 1)).addOut(new Item(ItemType.STRONG_WEAPONS, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.ROCKS, 1)).addOut(new Item(ItemType.STONE, 2)));
                break;
            case SMITH_FURNACES:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.COPPER_ORE, 2)).addOut(new Item(ItemType.METAL, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.IRON_ORE, 1)).addOut(new Item(ItemType.METAL, 1)));
                break;
            case SMITH_STORAGE:
                storageSpace = 10;
                break;
            case WORKSHOP:
                yield = new int[]{-1,2,0,1};
                break;
            case WORKSHOP_FURNACES:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.BRANCHES, 2)).addOut(new Item(ItemType.NECESSITY, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.LOGS, 1)).addOut(new Item(ItemType.NECESSITY, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.COAL, 1)).addOut(new Item(ItemType.NECESSITY, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.SAND, 3)).addOut(new Item(ItemType.GLASS, 1)));
                break;
            case WORKSHOP_SAW:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.LOGS, 1)).addOut(new Item(ItemType.LUMBER, 2)));
                break;
            case WORKSHOP_STORAGE:
                storageSpace = 10;
                break;
            case GRANARY:
                yield = new int[]{1,0,0,0};
                break;
            case GRANARY_BAKERY:
                build.recipes.add(new Recipe().addIn(new Item(ItemType.WHEAT, 1)).addOut(new Item(ItemType.FOOD, 1), new Item(ItemType.NECESSITY, 1)));
                build.recipes.add(new Recipe().addIn(new Item(ItemType.FISH, 1)).addOut(new Item(ItemType.FOOD, 1), new Item(ItemType.NECESSITY, 1)));
                break;
            case GRANARY_STORAGE:
                storageSpace = 10;
                break;
            default:
                System.err.println("Invalid building type for BuildingFactory");
                break;
        }

        /*build.food = yield[0];
        build.production = yield[1];
        build.science = yield[2];
        build.capital = yield[3];*/
        build.setYield(yield);

        build.actionPoints = actionPoints;
        build.maxActionPoints = maxActionPoints;

        build.workNeeded = workNeeded;
        build.workCompleted = workNeeded*completionPercentage;

        build.inventorySpace = storageSpace;

        if (parent != null) {
            build.addThisAsModuleToBuilding(parent);
        }
        else {
            build.move(tile);
        }
        return build;
    }

}
