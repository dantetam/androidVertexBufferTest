package io.github.dantetam.world.ai;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Inventory;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.Tech;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 7/13/2016.
 */
public class ArtificialIntelligence {

    public Clan clan; //The 'parent' clan
    public String leaderName;
    public String abilityOne = null;
    public String abilityTwo = null;
    public HashMap<String, Integer> personality, strategy, tactics;

    public HashMap<String, List<String>> friendlyText = new HashMap<>();

    public Object[] currentStrategy;

    public ArtificialIntelligence(Clan c) {
        clan = c;
        personality = new HashMap<>();
        strategy = new HashMap<>();
        tactics = new HashMap<>();
    }

    public void allComputerClanActions() {
        currentStrategy = defineStrategy();
        for (City city: clan.cities) {
            if (city.actionsQueue.size() == 0) {
                city.pickBestTiles();
                List<BuildingType> buildingTypes = city.computePossibleBuildingsForCity();
                List<PersonType> personTypes = city.computePossibleUnitsForCity();
                Object result = computeBestOfOptions(city, buildingTypes, personTypes);
                if (result instanceof BuildingType) {
                    city.queueActionBuildModule((BuildingType) result);
                }
                else if (result instanceof PersonType) {
                    city.queueActionBuildUnit((PersonType) result);
                }
            }
        }
        if (clan.techTree.researchingTechQueue.size() == 0) {
            Tech tech = computeBestTech();
            clan.techTree.researchingTechQueue.add(tech);
        }
    }

    public void computerClanCombat() {
        for (Person person: clan.people) {
            while (person.actionPoints > 0) {
                person.gameMove(person.world.randomNeighbor(person.location()));
            }
        }
    }

    public void computeDiplomaticOptions() {

    }

    public Object[] defineStrategy() {
        HashMap<String, Float> buildingFlavors = new HashMap<>();
        HashMap<String, Float> unitFlavors = new HashMap<>();
        float[] yieldFlavors = {1,1,1,1,1,1,1};
        String queueFocusType = null;
        //TODO:
        //Calculate a multi-dimensional voronoi-ish diagram where each point is manually defined
        //Define dimensions to be different extremes of situations (e.g. too few cities vs too many cities)
        //then adjust for a civ's weights according to their strategy and personality flavors
        //as well as extra environmental factors, both natural features and other civilizations
        //as well as a civ's preferred style of play, which should adjust a bit every game.
        //Definitely the civ should use a rough expectimax and a civ-unique heuristic
        //to define the optimal strategy.

        String point = "Growth";

        if (point.equals("Expansion")) {
            unitFlavors.put("Settler", 2f);
            yieldFlavors = new float[]{1,3,1,1,1,1,1};
        }
        else if (point.equals("Growth")) {
            unitFlavors.put("Worker", 2f);
            yieldFlavors = new float[]{3,2,1,1,2,2,1};
        }
        else if (point.equals("Diplomacy")) {
            yieldFlavors = new float[]{2,2,1,2.5f,1,1,1};
        }
        else if (point.equals("War")) {
            queueFocusType = "combat";
            yieldFlavors = new float[]{1,2,1,1,1,1,1};
        }
        else if (point.equals("Science")) {
            queueFocusType = "building";
            yieldFlavors = new float[]{1,1,3,2,1,1,1};
        }
        else if (point.equals("Culture")) {
            queueFocusType = "building";
            yieldFlavors = new float[]{2,1,1,1,1,1,3};
        }

        //The idea behind random personalities is that now civ is like a game of poker,
        //where both players and AI civs make an attempt to 'read' other civs' qualities,
        //such as trustworthiness, expansion, war, grand strategy, etc. Much like a poker game,
        //the world should escalate in tension, and snowballing should be part of the 4X experience.

        return new Object[]{buildingFlavors, unitFlavors, yieldFlavors, queueFocusType};
    }

    //This is just a simple naive maximization of immediate ROI + score.
    //Possibly used for lower difficulties. Harder difficulties should use some sort
    //of layered expectimax/multimax structure.
    public Object computeBestOfOptions(City city, List<BuildingType> buildingTypes, List<PersonType> personTypes) {
        Map<BuildingType, Integer> buildingOptionsScore = new LinkedHashMap<>();
        Map<PersonType, Integer> personOptionsScore = new LinkedHashMap<>();

        /*HashMap<String, Float> buildingFlavors = new HashMap<>();
        HashMap<String, Float> unitFlavors = new HashMap<>();
        float[] yieldFlavors = {1,1,1,1,1,1,1};
        String queueFocusType = null;*/

        for (BuildingType buildingType: buildingTypes) {
            int finalScore = computeBuildingTypeScore(clan, city, buildingType);
            if (currentStrategy != null) {
                HashMap<String, Float> unitFlavors = (HashMap) currentStrategy[0];
                if (unitFlavors.containsKey(buildingType.name)) {
                    finalScore = (int) (finalScore * unitFlavors.get(buildingType.name));
                }
            }
            buildingOptionsScore.put(buildingType, finalScore);
        }
        for (PersonType personType: personTypes) {
            /*double foodPerTurn = (strategy.get("Growth") / 10d + 0.5d) * personType.food;
            double prodPerTurn = (strategy.get("Expansion") / 10d + 0.5d) * personType;
            double sciPerTurn = (strategy.get("Science") / 10d + 0.5d) * personType.food;
            double capPerTurn = (strategy.get("Gold") / 10d + 0.5d) * personType.food;*//*
            int scorePerTenTurns = (int)((personType.atk) / 10d);

            double roiTurns = workNeeded / scorePerTenTurns;
            double snowball = Math.pow(0.8d, (roiTurns + turnsNeeded) / 2);

            int finalScore = (int) (snowball * scorePerTenTurns);*/
            int finalScore = computeUnitTypeScore(clan, city, personType);
            if (currentStrategy != null) {
                HashMap<String, Float> unitFlavors = (HashMap) currentStrategy[1];
                if (unitFlavors.containsKey(personType.name)) {
                    finalScore = (int) (finalScore * unitFlavors.get(personType.name));
                }

                String queueFocusType = (String) currentStrategy[3];
                if (queueFocusType != null) {
                    if (personType.category.equals(queueFocusType)) {
                        finalScore *= 2;
                    }
                }
            }
            personOptionsScore.put(personType, finalScore);
        }
        Map<BuildingType, Integer> sortedByScoreBuilding = OpstrykonUtil.sortMapByValue(buildingOptionsScore);
        Map<PersonType, Integer> sortedByScorePerson = OpstrykonUtil.sortMapByValue(personOptionsScore);
        Map.Entry<BuildingType, Integer> entryBuilding = null;
        Map.Entry<PersonType, Integer> entryPerson = null;
        if (sortedByScoreBuilding.size() != 0) {
            entryBuilding = sortedByScoreBuilding.entrySet().iterator().next();
        }
        if (sortedByScorePerson.size() != 0) {
            entryPerson = sortedByScorePerson.entrySet().iterator().next();
        }
        /*for (Map.Entry<BuildingType, Integer> entry: sortedByScore.entrySet()) {

            break;
        }*/

        OpstrykonUtil.printMap(sortedByScoreBuilding);
        OpstrykonUtil.printMap(sortedByScorePerson);

        if (entryBuilding == null && entryPerson == null) {
            System.err.println("No options to choose from!");
            return null;
        }
        else if (entryBuilding == null) {
            return entryPerson.getKey();
        }
        else if (entryPerson == null) {
            return entryBuilding.getKey();
        }
        else {
            boolean greater = entryBuilding.getValue() > entryPerson.getValue();
            return greater ? entryBuilding : entryPerson;
        }
    }

    public Tech computeBestTech() {
        List<Tech> researchableTech = clan.techTree.getResearchableTech();
        Map<Tech, Integer> techByScore = new LinkedHashMap<>();
        for (Tech tech: researchableTech) {
            int techScore = 0;
            for (BuildingType buildingType: tech.unlockedBuildings) {
                int averagedBuildingScore = 0;
                for (City city: clan.cities) {
                    averagedBuildingScore += computeBuildingTypeScore(clan, city, buildingType);
                }
                techScore += averagedBuildingScore;
            }
            for (PersonType personType: tech.unlockedUnits) {
                int averagedPersonScore = 0;
                for (City city: clan.cities) {
                    averagedPersonScore += computeUnitTypeScore(clan, city, personType);
                }
                techScore += averagedPersonScore;
            }
            if (tech.unlockedBuildings.size() + tech.unlockedUnits.size() == 0) {
                techScore = 0;
            }
            else {
                techScore /= tech.unlockedBuildings.size() + tech.unlockedUnits.size();
            }
            techByScore.put(tech, techScore);
        }
        Map<Tech, Integer> sortedByScore = OpstrykonUtil.sortMapByValue(techByScore);

        OpstrykonUtil.printMap(sortedByScore);

        return sortedByScore.entrySet().iterator().next().getKey();
    }

    private int computeBuildingTypeScore(Clan clan, City city, BuildingType buildingType) {
        double workNeeded = buildingType.workNeeded;
        int turnsNeeded;
        if (city != null) {
            Object[] cityData = city.gameYield();
            int[] yields = (int[]) cityData[0];
            turnsNeeded = (int) Math.ceil(workNeeded / (double)yields[1]);
        }
        else {
            turnsNeeded = (int) Math.ceil(workNeeded / 8d);
        }

        double foodPerTurn, prodPerTurn, sciPerTurn, capPerTurn;
        double hapPerTurn, healthPerTurn, culPerTurn;
        if (clan instanceof CityState) {
            foodPerTurn = 2 * buildingType.food();
            prodPerTurn = 2 * buildingType.production();
            sciPerTurn = buildingType.science();
            capPerTurn = buildingType.capital();
            hapPerTurn = buildingType.happiness();
            healthPerTurn = buildingType.health();
            culPerTurn = buildingType.culture();
        } else {
            foodPerTurn = (strategy.get("Growth") / 10d + 0.5d) * buildingType.food();
            prodPerTurn = (strategy.get("Expansion") / 10d + 0.5d) * buildingType.production();
            sciPerTurn = (strategy.get("Science") / 10d + 0.5d) * buildingType.science();
            capPerTurn = (strategy.get("Gold") / 10d + 0.5d) * buildingType.capital();
            hapPerTurn = (strategy.get("Growth") / 10d + 0.5d) * buildingType.happiness();
            healthPerTurn = (strategy.get("Expansion") / 10d + 0.5d) * buildingType.health();
            culPerTurn = (strategy.get("Culture") / 10d + 0.5d) * buildingType.culture();
            if (currentStrategy != null) {
                float[] yieldFlavors = (float[]) currentStrategy[2];
                foodPerTurn *= yieldFlavors[0];
                prodPerTurn *= yieldFlavors[1];
                sciPerTurn *= yieldFlavors[2];
                capPerTurn *= yieldFlavors[3];
                hapPerTurn *= yieldFlavors[4];
                healthPerTurn *= yieldFlavors[5];
                culPerTurn *= yieldFlavors[6];
            }
        }
        int scorePerTenTurns = (int)((foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn + hapPerTurn + healthPerTurn + culPerTurn) * 10d);

        double roiTurns = workNeeded / scorePerTenTurns;
        double snowball = Math.pow(0.8d, (roiTurns + turnsNeeded) / 2);

        int finalScore = (int) (snowball * scorePerTenTurns);
        return finalScore;
    }

    private int computeUnitTypeScore(Clan clan, City city, PersonType personType) {
        double workNeeded = personType.workNeeded;
        int turnsNeeded;
        if (city != null) {
            Object[] cityData = city.gameYield();
            int[] yields = (int[]) cityData[0];
            turnsNeeded = (int) Math.ceil(workNeeded / (double)yields[1]);
        }
        else {
            turnsNeeded = (int) Math.ceil(workNeeded / 8d);
        }

        double foodPerTurn, prodPerTurn, sciPerTurn, capPerTurn, hapPerTurn;
        if (clan instanceof CityState) {
            foodPerTurn = 2 * personType.atk;
            prodPerTurn = 2 * personType.def;
            sciPerTurn = personType.maneuver;
            capPerTurn = personType.fire;
            hapPerTurn = personType.shock;
        } else {
            foodPerTurn = (tactics.get("Offense") / 10d + 0.5d) * personType.atk;
            prodPerTurn = (tactics.get("Defense") / 10d + 0.5d) * personType.def;
            sciPerTurn = (tactics.get("Mobile") / 10d + 0.5d) * personType.maneuver;
            capPerTurn = ((tactics.get("Ranged") + tactics.get("Defense")) / 2 / 10d + 0.5d) * personType.fire;
            hapPerTurn = ((tactics.get("Melee") + tactics.get("Offense")) / 2 / 10d + 0.5d) * personType.shock;
        }
        int scorePerTenTurns = (int)((foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn + hapPerTurn) * 10d / 30d);

        double roiTurns = workNeeded / scorePerTenTurns;
        double snowball = Math.pow(0.8d, (roiTurns + turnsNeeded) / 2);

        int finalScore = (int) (snowball * scorePerTenTurns);
        return finalScore;
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

    public static int calcClanTotalScore(World world, Clan clan) {
        int score = 0;

        int[] yield = new int[4];
        for (City city: clan.cities) {
            Object[] yieldData = city.gameYield();
            int[] cityYield = (int[]) yieldData[0];
            //Inventory inventory = (Inventory) yieldData[1];
            for (int i = 0; i <= 3; i++)
                yield[i] += cityYield[i];
        }

        //City population score and number of cities + food output
        for (City city: clan.cities) {
            score += city.population();
            score += city.cityTiles.size() / 3;
        }
        score += yield[0];

        //Tech score + science output
        score += clan.techTree.researchedTech.size();
        score += yield[1];

        //Military score (score exponential) + production output
        for (Person person: clan.people) {
            PersonType personType = person.personType;
            score += (personType.atk + personType.def + personType.maneuver + personType.fire + personType.shock)/25;
        }

        //Building + impr score
        for (Building building: clan.buildings) {
            BuildingType buildingType = building.buildingType;
            int foodPerTurn = buildingType.food();
            int prodPerTurn = buildingType.production();
            int sciPerTurn = buildingType.science();
            int capPerTurn = buildingType.capital();
            int culPerTurn = buildingType.culture();
            score += (foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn + culPerTurn) / 3;
        }
        /*for (Tile tile: world.getAllValidTiles()) {
            if (tile.improvement != null) {
                if (world.getTileOwner(tile) != null) {
                    if (world.getTileOwner(tile).equals(clan)) {
                        BuildingType buildingType = tile.improvement.buildingType;
                        int foodPerTurn = buildingType.food();
                        int prodPerTurn = buildingType.production();
                        int sciPerTurn = buildingType.science();
                        int capPerTurn = buildingType.capital();
                        score += foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn;
                    }
                }
            }
        }*/

        //Diplomacy score? Number of friends?
        //TODO:

        return score;
    }

}
