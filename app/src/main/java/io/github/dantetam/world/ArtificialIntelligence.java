package io.github.dantetam.world;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 7/13/2016.
 */
public class ArtificialIntelligence {

    public World world;
    public Clan clan; //The 'parent' clan
    public String abilityOne = null;
    public String abilityTwo = null;
    public HashMap<String, Integer> personality, strategy, tactics;

    public ArtificialIntelligence(World w, Clan c) {
        world = w;
        clan = c;
        personality = new HashMap<>();
        strategy = new HashMap<>();
        tactics = new HashMap<>();
    }

    public void allComputerClanActions() {
        for (City city: clan.cities) {
            if (city.actionsQueue.size() == 0) {
                city.pickBestTiles();
                List<BuildingType> buildingTypes = city.computePossibleBuildingsForCity();
                List<PersonType> personTypes = city.computePossibleUnitsForCity();
                Object result = computeBestOfOptions(city, buildingTypes, personTypes);
                if (result instanceof BuildingType) {
                    Building queueBuilding = BuildingFactory.newBuilding(world, clan, (BuildingType) result, city.location(), 0);
                    TODO: Use city method (subtract resources) city.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, queueBuilding));
                }
                else if (result instanceof PersonType) {
                    Person queuePerson = PersonFactory.newPerson((PersonType) result, world, clan, 0);
                    TODO: Use city method (subtract resources) city.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_UNIT, queuePerson));
                }
            }
        }
    }

    //This is just a simple naive maximization of immediate ROI + score.
    //Possibly used for lower difficulties. Harder difficulties should use some sort
    //of layered expectimax/multimax structure.
    public Object computeBestOfOptions(City city, List<BuildingType> buildingTypes, List<PersonType> personTypes) {
        Object[] cityData = city.gameYield();
        int[] yields = (int[]) cityData[0];
        for (BuildingType buildingType: buildingTypes) {
            double workNeeded = buildingType.workNeeded;
            int turnsNeeded = (int) Math.ceil((double)workNeeded / (double)yields[1]);

            double foodPerTurn = (strategy.get("Growth") / 10d + 0.5d) * buildingType.food;
            double prodPerTurn = (strategy.get("Expansion") / 10d + 0.5d) * buildingType.food;
            double sciPerTurn = (strategy.get("Science") / 10d + 0.5d) * buildingType.food;
            double capPerTurn = (strategy.get("Gold") / 10d + 0.5d) * buildingType.food;
            int scorePerTenTurns = (int)((foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn) * 10d);

            double roiTurns = workNeeded / scorePerTenTurns;
            int snowball =
        }
    }

    public Tech computeBestTech() {

    }

    /*public ArtificialIntelligence(World world) {
        this.world = world;
    }

    public static void computerClanActions(Clan c) {
        for (Person person: c.people) {
            while (person.actionPoints > 0) {
                person.gameMove(world.randomNeighbor(person.location));
            }
        }
    }

    public static void computerClanCombat(CombatPlan plan, Clan c) {
        plan.clear();
        for (Entity en: c.people) {
            plan.addAction(en, new CombatAction(Action.ActionType.COMBAT_MOVE, world.randomNeighbor(en.location)));
        }
    }*/

}
