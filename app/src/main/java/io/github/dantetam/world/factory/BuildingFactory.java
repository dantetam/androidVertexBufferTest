package io.github.dantetam.world.factory;

import java.util.Collection;

import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Item;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.entity.Recipe;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 7/17/2016.
 */
public class BuildingFactory {

    public static City newCity(World world, Clan clan, Tile tile, Collection<Tile> cityTiles) {
        /*System.out.println(clan.buildingTree);
        System.out.println(clan.buildingTree.buildingTypes);
        System.out.println(clan.buildingTree.buildingTypes.get("City"));*/

        String cityName;
        if (clan.cities.size() < clan.cityNames.size()) {
            cityName = clan.cityNames.get(clan.cities.size());
        }
        else {
            cityName = clan.adjective + " City " + (clan.cities.size() + 1);
        }

        City city = new City(world, clan, cityName, clan.buildingTree.buildingTypes.get("City"), cityTiles);
        clan.cities.add(city);
        //city.initModules(tile.numSpaces);

        //city.recipes.add(new Recipe().addIn(new Item(ItemType.FOOD, 1)).addOut(new Item(ItemType.PRODUCTION, 1)));

        city.actionPoints = 1;
        city.maxActionPoints = 1;
        city.workNeeded = 1;
        city.workCompleted = 1;

        int atk = 0, def = 0, fire = 0, shock = 0;
        int exp = 0;

        city.atk = atk; city.def = def;
        city.fire = fire; city.shock = shock;
        city.exp = exp;

        //city.inventorySpace = 10;

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
        clan.buildings.add(build);
        //build.initModules(tile.numSpaces);

        //int food = 0, production = 0, science = 0, capital = 0;

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

        NO_RESOURCE       (-1,  "No resource"),

        FOOD              (0,   "Food"), //These are not actual items but they're used for item recipes
        PRODUCTION        (1,   "Production"),
        SCIENCE           (2,   "Science"),
        CAPITAL           (3,   "Capital"),
        LABOR             (4,   "Labor"),
        NECESSITY         (5,   "Necessity"),
        LUXURY            (6,   "Luxury"),

        GRAIN             (10,  "Grain"), //Farm+
        ASCENDIA          (15,  "Ascendia"),
        EXTROMASS         (20,  "Extromass"),

        IRON              (30,  "Iron"), //Mine+
        ASH_STONE         (54,  "Ash Stone"),
        HELLENIA          (55,  "Hellenia"),

        ABYSS_MATTER      (60,  "Abyss Matter"), //Boats+

        STEEL             (100, "Steel"), //Workshop+

        GLASS_FIRE        (120, "Glass Fire"), //Lab+
        CRYSTAL_CELLS     (121,  "Crystal Cells"),
        PROGENITOR_MATTER (130, "Progenitor Matter"),
        XENOVOLTAIC_CELLS (131, "Xenovoltaic Cells"),
        ;
        public static int[] ranges = {-1,0,10,30,60,100,120,999999};
        public static String[] nameRanges = {"NoResource", "Base", "Farm", "Mine", "Boats", "Workshop", "Lab"};
        */

        /*build.food = yield[0];
        build.production = yield[1];
        build.science = yield[2];
        build.capital = yield[3];*/
        //build.setYield(new int[]{type.food, type.production, type.science, type.capital});

        build.actionPoints = 0;
        build.maxActionPoints = 0;

        build.workNeeded = type.workNeeded;
        build.workCompleted = type.workNeeded*completionPercentage;

        //build.inventorySpace = storageSpace;

        if (parent != null) {
            parent.modules.add(build);
        }
        else {
            if (completionPercentage >= 1) {
                build.move(tile);
            }
        }
        return build;
    }

    public static void removeBuilding(Building building) {
        /*public Person(World world, Clan clan, String name) {
            super(world, clan);
            clan.people.add(this);
            this.name = name;
            skills = new ArrayList<>();
        }*/
        /*public Entity(World w, Clan c) {
            //locations = new ArrayList<Tile>();
            world = w;
            clan = c;
            inventory = new ArrayList<Item>();
            id = globalIdCounter;
            Entity.globalIdCounter++;
            actionsQueue = new ArrayList<>();
        }*/
        if (building.isModule) {

        }
        else {
            if (building.location() != null)
                building.location().improvement = null;
        }
        building.clan.buildings.remove(building);

        building.world = null;
        building.clan = null;
        building.setLocation(null);

        building.enabled = false;
    }

}
