package io.github.dantetam.opstrykontest;

import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.entity.ArtificialIntelligence;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Inventory;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 7/13/2016.
 */
public class WorldSystem {

    public World world;
    //public ArtificialIntelligence artificialIntelligence;
    public static WorldPathfinder worldPathfinder;

    public int turnNumber = 0;

    public Clan playerClan;

    //public List<RelationModifier>[][] relations;
    public HashMap<Clan, HashMap<Clan, List<RelationModifier>>> relations;
    public HashMap<Clan, Integer> clanId;

    public HashMap<Clan, Integer> calculatedClanScores;

    public enum RelationModifier {
        AT_WAR,
        AT_PEACE
    }

    public WorldSystem(WorldHandler worldHandler) {
        world = worldHandler.world;
        initClan(world.getClans().get(0));
        //artificialIntelligence = new ArtificialIntelligence(world, clan);
        worldPathfinder = new WorldPathfinder(world);
        int len = world.getClans().size();
        //relations = (List<RelationModifier>[][]) new Object[len][len];
        relations = new HashMap<>();
        //relations = (List<RelationModifier>[][]) Array.newInstance(Object.class, len, len);
        clanId = new HashMap<>();
        for (int i = 0; i < len; i++) {
            clanId.put(world.getClans().get(i), i);
        }
        calculatedClanScores = new HashMap<>();
        for (int i = 0; i < len; i++) {
            calculatedClanScores.put(world.getClans().get(i), 0);
        }
    }

    public boolean atWar(Clan atk, Clan def) {
        int atkId = clanId.get(atk), defId = clanId.get(def);
        List<RelationModifier> relationModifierList = relations.get(atk).get(def);
        return relationModifierList != null && relationModifierList.contains(RelationModifier.AT_WAR);
    }

    public void initClan(Clan c) {
        playerClan = c;
    }

    public void turn() {
        processClan(playerClan);
        for (Clan clan: world.getClans()) {
            if (!clan.equals(playerClan)) {
                clan.ai.allComputerClanActions();
            }
            processClan(clan);
        }

        for (Clan c: world.getClans()) {
            for (Person person: c.people) {
                person.actionPoints = person.maxActionPoints;
            }
            for (Building building: c.buildings) {
                building.actionPoints = building.maxActionPoints;
                //building.executeQueue();
            }
            for (City city: c.cities) {
                city.actionPoints = city.maxActionPoints;
                //building.executeQueue();
            }
        }

        //When done processing all actions, calculate new scores.
        for (Clan c: world.getClans()) {
            int score = ArtificialIntelligence.calcClanTotalScore(world, c);
            calculatedClanScores.put(c, score);
        }
        turnNumber++;
        System.err.println("#turns passed: " + turnNumber);
    }

    public static int getGlobalScience(Clan clan) {
        int globalScience = 0;
        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            Object[] objects = city.gameYield();
            int[] yield = (int[]) objects[0];
            globalScience += yield[2];
        }
        return globalScience;
    }

    private void processClan(Clan clan) {
        /*for (Building building: clan.buildings) {
            building.executeQueue();
        }*/
        for (Person person: clan.people) {
            person.executeQueue();
        }
        int totalScience = 0, totalGold = 0;
        Inventory totalResources = new Inventory();
        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            Object[] objects = city.gameYield();
            int[] yield = (int[]) objects[0];
            Inventory inventory = (Inventory) objects[1];

            int workingPopulation = city.population - city.freeWorkingPopulation;
            city.foodStoredForGrowth += yield[0] - workingPopulation;
            if (city.foodStoredForGrowth >= city.foodNeededForGrowth) {
                city.foodStoredForGrowth -= city.foodNeededForGrowth;

                city.population++; city.freeWorkingPopulation++;
                city.foodNeededForGrowth = City.generateCityFoodData()[city.population];
            }

            if (city.actionsQueue.size() > 0) {
                Action action = city.actionsQueue.get(0);
                int production = yield[1];
                //TODO: Differentiate behavior
                if (action.type == Action.ActionType.QUEUE_BUILD_UNIT) {
                    Person target = (Person) action.data;
                    target.workCompleted += production;
                    if (target.workCompleted >= target.workNeeded) {
                        target.move(city.location());
                        city.actionsQueue.remove(0);
                    }
                }
                else if (action.type == Action.ActionType.QUEUE_BUILD_MODULE) {
                    Building target = (Building) action.data;
                    target.workCompleted += production;
                    if (target.workCompleted >= target.workNeeded) {
                        city.actionsQueue.remove(0);
                    }
                }
            }

            int science = yield[2];
            int possibleExtra = clan.techTree.research(science);
            if (possibleExtra > 0) {
                //TODO: Do something with this?
            }

            totalGold += yield[3];

            totalResources.addAnotherInventory(inventory);
        }
    }

}
