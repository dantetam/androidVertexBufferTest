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

    public ArtificialIntelligence(Clan c) {
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
                    city.queueActionBuildModule((BuildingType) result);
                }
                else if (result instanceof PersonType) {
                    city.queueActionBuildUnit((PersonType) result);
                }
            }
        }
        System.out.println(clan + ">>>" + clan.techTree);
        if (clan.techTree.researchingTechQueue.size() == 0) {
            Tech tech = computeBestTech();
            clan.techTree.researchingTechQueue.add(tech);
        }
    }

    public void computerClanCombat(CombatPlan combatPlan) {
        for (Person person: clan.people) {
            while (person.actionPoints > 0) {
                person.gameMove(person.world.randomNeighbor(person.location()));
            }
        }
    }

    //This is just a simple naive maximization of immediate ROI + score.
    //Possibly used for lower difficulties. Harder difficulties should use some sort
    //of layered expectimax/multimax structure.
    public Object computeBestOfOptions(City city, List<BuildingType> buildingTypes, List<PersonType> personTypes) {
        Map<BuildingType, Integer> buildingOptionsScore = new LinkedHashMap<>();
        Map<PersonType, Integer> personOptionsScore = new LinkedHashMap<>();
        for (BuildingType buildingType: buildingTypes) {
            int finalScore = computeBuildingTypeScore(clan, city, buildingType);
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
            int finalScore = computeUnitTypeScore(city, personType);
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
        System.out.println(clan.techTree + "<<<<");
        List<Tech> researchableTech = clan.techTree.getResearchableTech();
        Map<Tech, Integer> techByScore = new LinkedHashMap<>();
        for (Tech tech: researchableTech) {
            int techScore = 0;
            for (BuildingType buildingType: tech.unlockedBuildings) {
                int averagedBuildingScore = 0;
                for (City city: clan.cities) {
                    averagedBuildingScore += computeBuildingTypeScore(clan, city, buildingType);
                }
                //averagedBuildingScore /= clan.cities.size();
                techScore += averagedBuildingScore;
            }
            for (PersonType personType: tech.unlockedUnits) {
                int averagedPersonScore = 0;
                for (City city: clan.cities) {
                    averagedPersonScore += computeUnitTypeScore(city, personType);
                }
                //averagedBuildingScore /= clan.cities.size();
                techScore += averagedPersonScore;
            }
            techByScore.put(tech, techScore);
        }
        Map<Tech, Integer> sortedByScore = OpstrykonUtil.sortMapByValue(techByScore);
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

        double foodPerTurn = 0;
        double prodPerTurn = 0;
        double sciPerTurn = 0;
        double capPerTurn = 0;
        if (clan instanceof CityState) {
            foodPerTurn = 2 * buildingType.food();
            prodPerTurn = 2 * buildingType.production();
            sciPerTurn = buildingType.science();
            capPerTurn = buildingType.capital();
        } else {
            foodPerTurn = (strategy.get("Growth") / 10d + 0.5d) * buildingType.food();
            prodPerTurn = (strategy.get("Expansion") / 10d + 0.5d) * buildingType.production();
            sciPerTurn = (strategy.get("Science") / 10d + 0.5d) * buildingType.science();
            capPerTurn = (strategy.get("Gold") / 10d + 0.5d) * buildingType.capital();
        }
        int scorePerTenTurns = (int)((foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn) * 10d);

        double roiTurns = workNeeded / scorePerTenTurns;
        double snowball = Math.pow(0.8d, (roiTurns + turnsNeeded) / 2);

        int finalScore = (int) (snowball * scorePerTenTurns);
        return finalScore;
    }

    private static int computeUnitTypeScore(City city, PersonType type) {
        return 0;
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
            Inventory inventory = (Inventory) yieldData[1];
            for (int i = 0; i <= 3; i++)
                yield[i] += cityYield[i];
        }

        //City population score and number of cities + food output
        for (City city: clan.cities) {
            score += city.population();
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
            score += foodPerTurn + prodPerTurn + sciPerTurn + capPerTurn;
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
