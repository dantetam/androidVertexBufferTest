package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 6/16/2016.
 */
public class Building extends Entity {

    public BuildingType buildingType;
    public List<Building> modules;
    private Building parent;
    public boolean isModule = false;

    private int food, production, science, capital;
    public void setYield(int[] yield) {
        food = yield[0];
    }

    public List<Recipe> recipes;
    public List<String> effects;
    //public List<Item> inputResources;
    //public List<Item> outputResources;

    //public int[] lastYield;

    public Building(World world, Clan clan, BuildingType type) {
        super(world, clan);
        clan.buildings.add(this);
        buildingType = type;
        name = type.name;
        modules = new ArrayList<>();
        recipes = new ArrayList<>();
        effects = new ArrayList<>();
    }

    /*public Building(Tile t, BuildingType type) {
        //super(t);
        //move(t);
        move(t);
        name = type.name;
    }*/

    /*public Building[] getModules() {
        return modules;
    }

    public void initModules(int len) {
        modules = new Building[len];
    }

    public void addThisAsModuleToBuilding(Building building) {
        for (int i = 0; i < building.modules.length; i++) {
            if (building.modules[i] == null) {
                replaceModuleToBuilding(building, i);
                return;
            }
        }
        throw new IllegalArgumentException("Building inserted into is full of modules");
    }
    public void replaceModuleToBuilding(Building buildingParent, int index) {
        buildingParent.modules[index] = this;
        this.parent = buildingParent;
        location = buildingParent.location;
        isModule = true;
    }*/

    public void executeQueue() {
        if (!enabled) {
            return;
        }
        //actionsQueue.add(new BuildingAction(Action.ActionType.PROCESS, this));
        while (true) {
            Action action = actionsQueue.get(0);
            Action.ActionStatus status = action.execute(this);

            if (status == Action.ActionStatus.ALREADY_COMPLETED || status == Action.ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            }
            else if (status == Action.ActionStatus.IMPOSSIBLE) {
                actionsQueue.remove(0);
                //TODO: Error code? Print info about errant action?
            }
            else if (status == Action.ActionStatus.OUT_OF_ENERGY) {
                break;
            }
            else if (status == Action.ActionStatus.CONTINUING) {
                //do nothing, keep the action in the first slot, it'll be repeated.
            }
        }
    }

    public void move(Tile t) {
        if (location != null) {
            location.improvement = null;
        }
        t.improvement = this;
        super.location = t;
        //super.move(t);
    }

    public int[] getYieldNoModules() {
        int[] yields = {food, production, science, capital};
        return yields;
    }
    public int[] getYieldWithModules() {
        int[] yields = {food, production, science, capital};
        if (modules != null) {
            for (Building module : modules) {
                if (module != null) {
                    int[] moduleYield = module.getYieldWithModules();
                    for (int i = 0; i < yields.length; i++) {
                        yields[i] += moduleYield[i];
                    }
                }
            }
        }
        return yields;
    }

    //TODO: //The sum of all modules' yields and storage space is added to the main building in which it is located
    //Each building may choose one recipe per turn
    /*public Action.ActionStatus gameProcess() {
        if (location() != null) {
            if (actionPoints <= 0) {
                return Action.ActionStatus.OUT_OF_ENERGY;
            }
            //actionPoints--;
            int[] yieldWithModules = getYieldWithModules();
            lastYield = new int[]{
                    yieldWithModules[0] + location.food,
                    yieldWithModules[1] + location.production,
                    yieldWithModules[2] + location.science,
                    yieldWithModules[3] + location.capital
            };
            Item[] items = {
                    new Item(ItemType.FOOD, lastYield[0]),
                    new Item(ItemType.PRODUCTION, lastYield[1]),
                    new Item(ItemType.SCIENCE, lastYield[2]),
                    new Item(ItemType.CAPITAL, lastYield[3])
            };
            addAllToInventory(Arrays.asList(items));
            while (true) {
                boolean completedRecipe = false;
                for (Recipe recipe: recipes) {
                    if (hasItemsInInventory(recipe.input, true)) {
                        completedRecipe = true;
                        addAllToInventory(recipe.output);
                    }
                }
                if (!completedRecipe) {
                    break;
                }
                *//*if (!hasItemsInInventory(inputResources, true)) {
                    break;
                }
                else {
                    addAllToInventory(outputResources);
                }*//*
            }
            return Action.ActionStatus.EXECUTED;
        }
        else {
            return Action.ActionStatus.IMPOSSIBLE;
        }
    }*/

    /*public Action.ActionStatus gameBuildModule(Building building) {
        if (building.workCompleted >= building.workNeeded) {
            return Action.ActionStatus.ALREADY_COMPLETED;
        }
        if (actionPoints <= 0) {
            return Action.ActionStatus.OUT_OF_ENERGY;
        }
        actionPoints--;
        building.workCompleted += location.production;
        if (building.workCompleted >= building.workNeeded) {
            return Action.ActionStatus.EXECUTED;
        }
        return Action.ActionStatus.CONTINUING;
    }*/

    /*public Action.ActionStatus gameBuildUnit(Person person) {
        if (person.workCompleted >= person.workNeeded) {
            return Action.ActionStatus.ALREADY_COMPLETED;
        }
        if (actionPoints <= 0) {
            return Action.ActionStatus.OUT_OF_ENERGY;
        }
        actionPoints--;
        person.workCompleted += lastYield[1];
        if (person.workCompleted >= person.workNeeded) {
            person.move(location);
            return Action.ActionStatus.EXECUTED;
        }
        return Action.ActionStatus.CONTINUING;
    }*/

    /*public void addInput(ItemType type, int quantity) {
        inputResources.add(new Item(type, quantity));
    }
    public void addOutput(ItemType type, int quantity) {
        outputResources.add(new Item(type, quantity));
    }*/
    public boolean containsInput(ItemType type) {
        for (Recipe recipe: recipes) {
            for (Item item: recipe.input) {
                if (item.type == type) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean containsOutput(ItemType type) {
        for (Recipe recipe: recipes) {
            for (Item item: recipe.output) {
                if (item.type == type) {
                    return true;
                }
            }
        }
        return false;
    }

}
