package io.github.dantetam.opstrykontest;

import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.ai.ArtificialIntelligence;
import io.github.dantetam.world.ai.RelationMap;
import io.github.dantetam.world.ai.RelationModifier;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Inventory;
import io.github.dantetam.world.entity.Person;
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

        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            Object[] objects = city.gameYield();
            int[] yield = (int[]) objects[0];
            Inventory inventory = (Inventory) objects[1];

            city.lastYieldHealth = yield[5];

            int workingPopulation = city.population() - city.freeWorkingPopulation();
            double foodGain = yield[0] - workingPopulation * 2 - city.freeWorkingPopulation() * 1;
            if (clan.lastHappiness < 0) {
                foodGain *= 0.5;
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
                    if (target.workCompleted >= target.workNeeded) {
                        target.clan = clan;
                        clan.people.add(target);
                        target.move(city.location());
                        city.actionsQueue.remove(0);
                    }
                }
                else if (action.type == Action.ActionType.QUEUE_BUILD_MODULE) {
                    Building target = (Building) action.data;
                    target.workCompleted += production;
                    if (target.workCompleted >= target.workNeeded) {
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

            clan.totalGold += yield[3];
            clan.lastHappiness += yield[4];
            clan.totalCulture += yield[6];

            totalResources.addAnotherInventory(inventory);
        }

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

}
