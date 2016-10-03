package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.action.Ability;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.ai.ArtificialIntelligence;
import io.github.dantetam.world.ai.RelationMap;
import io.github.dantetam.world.ai.RelationModifier;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.IdeologyTree;
import io.github.dantetam.world.entity.Inventory;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tech;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 7/13/2016.
 */
public class WorldSystem {

    public World world;
    //public ArtificialIntelligence artificialIntelligence;
    public static WorldPathfinder worldPathfinder;

    public int turnNumber = 0;
    public int turnLimit = 200;

    public boolean victory = false;

    public Clan playerClan;

    //public List<RelationModifier>[][] relations;
    public HashMap<Clan, RelationMap> relations;

    public HashMap<Clan, Integer> clanId;

    public static HashMap<Clan, Integer> calculatedClanScores;

    public WorldSystem(WorldHandler worldHandler) {
        world = worldHandler.world;
        world.worldSystem = this;
        List<Clan> clans = world.getClans();
        initClan(clans.get(0));
        //artificialIntelligence = new ArtificialIntelligence(world, clan);
        worldPathfinder = new WorldPathfinder(world);
        int len = world.getClans().size();
        //relations = (List<RelationModifier>[][]) new Object[len][len];
        relations = new HashMap<>();
        //relations = (List<RelationModifier>[][]) Array.newInstance(Object.class, len, len);
        clanId = new HashMap<>();
        for (int i = 0; i < len; i++) {
            clanId.put(clans.get(i), i);
        }
        calculatedClanScores = new HashMap<>();
        for (int i = 0; i < len; i++) {
            calculatedClanScores.put(clans.get(i), 0);
        }

        for (Clan c: clans) {
            relations.put(c, new RelationMap(c, clans));
        }
    }

    public boolean containsMod(Clan atk, Clan def, RelationModifier mod) {
        //int atkId = clanId.get(atk), defId = clanId.get(def);
        List<RelationModifier> relationModifierList = relations.get(atk).getRelations(def);
        return relationModifierList != null && relationModifierList.contains(mod);
    }

    public boolean atWar(Clan atk, Clan def) {
        //int atkId = clanId.get(atk), defId = clanId.get(def);
        List<RelationModifier> relationModifierList = relations.get(atk).getRelations(def);
        return relationModifierList != null && relationModifierList.contains(RelationModifier.AT_WAR);
    }

    public void declareWar(Clan atk, Clan def) {
        relations.get(atk).addMod(def, RelationModifier.AT_WAR);
        relations.get(def).addMod(atk, RelationModifier.AT_WAR);
        relations.get(def).addMod(atk, RelationModifier.AGGRESSIVE_WAR);
        relations.get(atk).updateOpinions(world.getClans());
        relations.get(def).updateOpinions(world.getClans());
    }

    public void makePeace(Clan atk, Clan def) {
        relations.get(atk).removeMod(def, RelationModifier.AT_WAR);
        relations.get(def).removeMod(atk, RelationModifier.AT_WAR);
        relations.get(atk).addMod(def, RelationModifier.WAS_AT_WAR);
        relations.get(def).addMod(atk, RelationModifier.WAS_AT_WAR);
        relations.get(atk).updateOpinions(world.getClans());
        relations.get(def).updateOpinions(world.getClans());
    }

    public void denounce(Clan atk, Clan def) {
        relations.get(atk).addMod(def, RelationModifier.DENOUNCE);
        relations.get(def).addMod(atk, RelationModifier.DENOUNCED);
        relations.get(atk).updateOpinions(world.getClans());
        relations.get(def).updateOpinions(world.getClans());
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

        dealWithCityStateTech();

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
            spreadIdeology(c);
        }

        HashMap<IdeologyTree.Ideology, Integer> ideologyByFollowers = new HashMap<>();
        for (Clan c: world.getClans()) {
            for (City city: c.cities) {
                if (city.ideologyInfluence.size() > 0) {
                    Map<IdeologyTree.Ideology, Integer> sorted = OpstrykonUtil.sortMapByValue(city.ideologyInfluence);
                    IdeologyTree.Ideology dominant = sorted.keySet().iterator().next();
                    city.dominantIdeology = dominant;
                    if (!(ideologyByFollowers.containsKey(dominant))) {
                        ideologyByFollowers.put(dominant, 0);
                    }
                    ideologyByFollowers.put(dominant, ideologyByFollowers.get(dominant) + city.population());
                }
            }
        }

        Check for tech when looking at resources and their impr yield
        Look at phone notes

        //When done processing all actions, calculate new scores.
        Clan maxClan = null;
        int maxScore = -1;
        for (Clan c: world.getClans()) {
            int score = ArtificialIntelligence.calcClanTotalScore(world, c);
            calculatedClanScores.put(c, score);
            if (maxClan == null || score > maxScore) {
                maxClan = c;
                maxScore = score;
            }
        }
        turnNumber++;
        System.err.println("#turns passed: " + turnNumber);

        checkVictoryConditions(maxClan);
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
        //int totalScience = 0, totalGold = 0;
        Inventory totalResources = new Inventory();

        clan.lastHappiness = 4;

        ArrayList<Ability> abilities = new ArrayList<>();

        for (Ability ability : clan.techTree.specialAbilities.keySet()) {
            abilities.add(ability);
        }

        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            Object[] objects = city.gameYield();
            int[] yield = (int[]) objects[0];
            Inventory inventory = (Inventory) objects[1];

            city.lastYieldHealth = yield[5];

            int workingPopulation = city.population() - city.freeWorkingPopulation();
            double foodGain = yield[0] - workingPopulation * 2 - city.freeWorkingPopulation() * 1;
            if (clan.lastHappiness < 0) {
                foodGain *= 0.333;
            }
            if (city.lastYieldHealth < 0) {
                foodGain += city.lastYieldHealth * 2;
            }
            city.foodStoredForGrowth += (int) foodGain;
            if (city.foodStoredForGrowth >= city.foodNeededForGrowth) {
                city.foodStoredForGrowth -= city.foodNeededForGrowth;
                city.increasePopulation();
            }

            city.cultureStoredForExpansion += yield[6];
            if (city.cultureStoredForExpansion >= city.cultureNeededForExpansion) {
                city.cultureStoredForExpansion -= city.cultureNeededForExpansion;

                city.tilesExpanded++;
                city.expandToBestTile();
                city.cultureStoredForExpansion = City.cityFoodData()[city.tilesExpanded];
            }
            if (city.actionsQueue.size() > 0) {
                Action action = city.actionsQueue.get(0);
                int production = yield[1];
                //TODO: Differentiate behavior
                if (action.type == Action.ActionType.QUEUE_BUILD_UNIT) {
                    Person target = (Person) action.data;
                    target.workCompleted += production;
                    if (target.workCompleted >= target.personType.workNeeded) {
                        System.out.println("done: " + target.personType.workNeeded + " " + target.workCompleted + " " + production);
                        target.clan = clan;
                        clan.people.add(target);
                        target.move(city.location());
                        city.actionsQueue.remove(0);
                    }
                }
                else if (action.type == Action.ActionType.QUEUE_BUILD_MODULE) {
                    Building target = (Building) action.data;
                    target.workCompleted += production;
                    if (target.workCompleted >= target.buildingType.workNeeded) {
                        System.out.println("done2: " + target.buildingType.workNeeded + " " + target.workCompleted + " " + production);
                        city.actionsQueue.remove(0);
                        if (target.buildingType.wonder) {
                            endAllWondersInQueue(target.buildingType);
                            System.err.println(clan.ai.leaderName + " has built the " + target.buildingType.name);
                        }
                    }
                }
            }

            int science = yield[2];
            int possibleExtra = clan.techTree.research(science);
            if (possibleExtra > 0) {
                //TODO: Do something with this?
            }

            for (Building building: city.modules) {
                if (building.buildingType.abilities != null) {
                    for (Ability ability: building.buildingType.abilities) {
                        abilities.add(ability);
                    }
                }
            }

            parseAllCityAbilities(abilities, city, yield);

            clan.totalGold += yield[3];
            clan.lastHappiness += yield[4];
            clan.lastHappiness -= city.population();
            clan.totalIdeologyPower += yield[6] * Math.max(0.5, Math.min(2.0, (1 + (yield[4] / 10))));

            if (!(clan instanceof CityState)) {
                int numIdeologies = clan.ideologyTree.numIdeologiesAndTenetsUnlocked();
                clan.nextIdeologyCost = (int) (10 * Math.pow(1.28, numIdeologies) + 10 * numIdeologies);
                if (clan.totalIdeologyPower >= clan.nextIdeologyCost) {
                    System.out.println("Need ideology update here WorldSystem");
                }
            }

            totalResources.addAnotherInventory(inventory);
        }

        parseAllGlobalAbilities(abilities, clan);

        for (Person person: clan.people) {
            if (person.fortify || (person.location() != null && person.location().improvement instanceof City)) {
                if (person.health < person.maxHealth) {
                    person.health += person.maxHealth * 0.1f;
                    if (person.health > person.maxHealth) {
                        person.health = person.maxHealth;
                    }
                }
            }
        }

        for (City city: clan.cities) {
            if (city.health < city.maxHealth) {
                city.health += city.maxHealth * 0.1f;
                if (city.health > city.maxHealth) {
                    city.health = city.maxHealth;
                }
            }
        }
    }

    public void parseAllCityAbilities(List<Ability> abilities, City city, int[] yield) {

    }

    public void parseAllGlobalAbilities(List<Ability> abilities, Clan clan) {

    }

    public void spreadIdeology(Clan clan) {
        HashMap<IdeologyTree.Ideology, Integer> pressure = new HashMap<>();
        for (IdeologyTree.Ideology ideology: IdeologyTree.globalMap.values()) {
            pressure.put(ideology, 0);
        }
        if (clan.ideologyTree.primaryIdeology != null) {
            pressure.put(clan.ideologyTree.primaryIdeology, 100);
        }
        for (City city: clan.cities) {
            for (Map.Entry<IdeologyTree.Ideology, Integer> entry: city.ideologyInfluence.entrySet()) {
                int individualPressure = entry.getValue() + pressure.get(entry.getKey());
                for (Clan otherClan: world.getClans()) {
                    for (City otherCity: otherClan.cities) {
                        float dist = city.location().dist(otherCity.location());
                        if (!otherCity.ideologyInfluence.containsKey(entry.getKey())) {
                            otherCity.ideologyInfluence.put(entry.getKey(), 0);
                        }
                        otherCity.ideologyInfluence.put(entry.getKey(), otherCity.ideologyInfluence.get(entry.getKey()) + (int)((float) individualPressure / dist));
                    }
                }
            }
        }
    }

    public boolean allowedToAccessTile(Entity en, Tile t) {
        if (t.biome == Tile.Biome.SEA) {
            return false;
        }
        Clan owner = world.getTileOwner(t);
        if (owner == null) {
            return true;
        } else {
            boolean sameClan = en.clan.equals(owner);
            boolean openBorders = this.containsMod(owner, en.clan, RelationModifier.OPEN_BORDERS);
            boolean war = this.atWar(owner, en.clan);
            return sameClan || openBorders || war;
        }
    }

    private void checkVictoryConditions(Clan maxScoreClan) {
        List<Clan> clans = world.getClans();

        for (Clan c: clans) {
            int capitalCount = 0;
            for (City city: c.cities) {
                if (city.isCapital != null) {
                    capitalCount++;
                }
            }
            if (capitalCount == clans.size()) {
                victory = true;
                System.err.println(c.ai.leaderName + " of the " + c.name + " has won a conquest victory!");
                return;
            }
        }

        for (Clan c: clans) {
            if (c.techTree.researchedTech.get("Transcendence") != null) {
                victory = true;
                System.err.println(c.ai.leaderName + " of the " + c.name + " has won a transcendence victory!");
                return;
            }
        }

        for (Clan c: clans) {
            if (c.totalGold > 5000) {
                victory = true;
                System.err.println(c.ai.leaderName + " of the " + c.name + " has won a business victory!");
                return;
            }
        }

        if (turnNumber == turnLimit) {
            victory = true;
            System.err.println(maxScoreClan.ai.leaderName + " of the " + maxScoreClan.name + " has won a time victory!");
            return;
        }
    }

    private void endAllWondersInQueue(BuildingType wonderBuilt) {
        List<Clan> clans = world.getClans();
        for (Clan clan: clans) {
            for (City city: clan.cities) {
                for (Action action: city.actionsQueue) {
                    if (action.type == Action.ActionType.QUEUE_BUILD_UNIT) {
                        Building queuedBuilding = (Building) action.data;
                        if (queuedBuilding.buildingType == wonderBuilt) {
                            clan.totalGold += queuedBuilding.workCompleted;
                            city.actionsQueue.remove(action);
                        }
                    }
                }
            }
        }
    }

    private void dealWithCityStateTech() {
        TechTree playerTechTree = playerClan.techTree;
        HashMap<Tech, Boolean> cityStateTree = TechTree.cityStateTech;
        for (Map.Entry<String, Tech> entry: playerTechTree.techMap.entrySet()) {
            Tech tech = entry.getValue();
            if (cityStateTree.get(tech) != null) {

            }
            else {
                float numResearched = 0, numClans = 0;
                for (Clan clan: world.getClans()) {
                    if (!(clan instanceof CityState)) {
                        numClans++;
                        if (clan.techTree.researchedTech.get(tech) != null) {
                            numResearched++;
                        }
                    }
                }
                numResearched /= numClans;
                if (numResearched >= 0.5) {
                    cityStateTree.put(tech, true);
                    for (Clan clan: world.getClans()) {
                        if (clan instanceof CityState) {
                            ((CityState) clan).techTree.forceUnlock(tech);
                        }
                    }
                }
            }
        }
    }

}
