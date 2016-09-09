package io.github.dantetam.opstrykontest;

import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.action.BuildingAction;
import io.github.dantetam.world.entity.ArtificialIntelligence;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
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
    public int turnLimit = 200;

    public boolean victory = false;

    public Clan playerClan;

    //public List<RelationModifier>[][] relations;
    public HashMap<Clan, RelationMap> relations;
    public HashMap<Clan, Integer> clanId;

    public HashMap<Clan, Integer> calculatedClanScores;

    public class RelationMap {
        public Clan subjectClan;
        private HashMap<Clan, List<RelationModifier>> map;
        public HashMap<Clan, Integer> initialScore = null;
        public HashMap<Clan, Integer> relationScore = null;

        public HashMap<Clan, Integer> trust;
        public HashMap<Clan, RelationOpinion> opinions;

        public HashMap<Clan, Boolean> deceiving;

        public RelationMap(Clan clan, List<Clan> clans) {
            subjectClan = clan;

            map = new HashMap<>();
            for (Clan c: clans) {
                map.put(c, new ArrayList<RelationModifier>());
            }

            trust = new HashMap<>();
            initialScore = new HashMap<>();
            relationScore = new HashMap<>();
            deceiving = new HashMap<>();
            opinions = new HashMap<>();

            setupInitialOpinions(clans);
            updateOpinions(clans);
        }

        public void setupInitialOpinions(List<Clan> clans) {
            System.out.println("I am " + subjectClan.name + ", here is how I feel about these clans:");
            for (Clan c: clans) {
                int trustScore = subjectClan.ai.personality.get("Cooperative") + subjectClan.ai.personality.get("Loyal");
                double scoreDiff = calculateScoreDiff(c);
                trustScore -= (int) (subjectClan.ai.personality.get("Jealous") * scoreDiff);
                trustScore += (int) (Math.random() * 4 - 2);
                trustScore = Math.max(0, Math.min(10, trustScore));
                trust.put(c, trustScore);

                int friendly = subjectClan.ai.personality.get("Cooperative") +
                        subjectClan.ai.personality.get("Loyal") +
                        subjectClan.ai.personality.get("Friendly") +
                        subjectClan.ai.personality.get("Diplomatic");

                int hostile = subjectClan.ai.personality.get("Hostile") +
                        subjectClan.ai.personality.get("Deceptive")*2 +
                        subjectClan.ai.personality.get("Vicious");

                int baseScore = (friendly - hostile) * 5;

                baseScore += (int) (Math.random() * 30.0 - 15.0);

                baseScore += getFirstFlavor();

                baseScore = Math.max(-60, Math.min(60, baseScore));

                initialScore.put(c, baseScore);

                double chanceOfDeception = Math.pow(0.8, trustScore + 4) * ((double) subjectClan.ai.personality.get("Deceptive") / 10d);
                //y = (0.8)^((x + 60)/16) + 0.2
                chanceOfDeception *= Math.pow(0.8, ((double) baseScore + 60.0) / 16) + 0.2;
                if (Math.random() < chanceOfDeception) {
                    deceiving.put(c, true);
                }
                else {
                    deceiving.put(c, false);
                }

                relationScore.put(c, baseScore);

                System.out.println(c.name + ": trust -> " + trustScore + ", opinion -> " + baseScore + ", deceptive -> " + deceiving.get(c) + " (" + chanceOfDeception + ")");
            }
        }

        public void updateOpinions(List<Clan> clans) {
            for (Clan c: clans) {
                updateOpinion(c);
            }
        }

        public void updateOpinion(Clan c) {
            List<RelationModifier> modifiers = map.get(c);
            int initial = initialScore.get(c);
            int score = initial;
            for (int i = 0; i < modifiers.size(); i++) {
                score += modifiers.get(i).score;
            }

            relationScore.put(c, score);

            int trust = this.trust.get(c);

            RelationOpinion opinion;

            double scoreDiff = calculateScoreDiff(c);

            if (score > 45) {
                if (trust > 3) {
                    opinion = RelationOpinion.FRIENDLY;
                } else {
                    opinion = RelationOpinion.ALLIED;
                }
            } else if (score > 15) {
                if (scoreDiff > 1.3d) {
                    opinion = RelationOpinion.INTIMIDATED;
                } else if (trust > 4) {
                    opinion = RelationOpinion.ALLIED;
                } else {
                    opinion = RelationOpinion.NEUTRAL;
                }
            } else if (scoreDiff > 1.2d) {
                opinion = RelationOpinion.INTIMIDATED;
            } else if (scoreDiff < 0.7d) {
                opinion = RelationOpinion.AGGRESSIVE;
            } else if (score > -10) {
                opinion = RelationOpinion.NEUTRAL;
            } else if (score > -45) {
                opinion = RelationOpinion.ANGRY;
            } else {
                opinion = RelationOpinion.HOSTILE;
            }
            opinions.put(c, opinion);
        }

        public void addMod(Clan defender, RelationModifier mod) {
            List<RelationModifier> relations = map.get(defender);
            relations.add(mod);
        }

        public void removeMod(Clan defender, RelationModifier removeMod) {
            List<RelationModifier> relations = map.get(defender);
            if (relations.size() == 0) return;
            for (int i = relations.size() - 1; i >= 0; i--) {
                RelationModifier mod = relations.get(i);
                if (mod == removeMod) {
                    relations.remove(i);
                }
            }
        }

        private int getFirstFlavor() {
            double sumPersonFlavors = 0;
            String[] flavorNames = new String[subjectClan.ai.personality.size()];
            double[] flavors = new double[subjectClan.ai.personality.size()];
            int i = 0;
            double runSum = 0;
            for (Map.Entry<String, Integer> entry: subjectClan.ai.personality.entrySet()) {
                flavorNames[i] = entry.getKey();
                runSum += entry.getValue();
                flavors[i] = runSum;
                i++;
            }
            double rand = Math.random();
            String chosenFlavor = null;
            for (int j = 0; j < flavors.length; j++) {
                if (rand <= flavors[j] / sumPersonFlavors) {
                    chosenFlavor = flavorNames[j];
                    break;
                }
            }
            if (chosenFlavor == null) chosenFlavor = flavorNames[flavorNames.length - 1];

            /*<personalityflavor name="Cooperative" value="6"></personalityflavor>
            <personalityflavor name="Jealous" value="3"></personalityflavor>
            <personalityflavor name="Friendly" value="5"></personalityflavor>
            <personalityflavor name="Hostile" value="5"></personalityflavor>
            <personalityflavor name="Loyal" value="8"></personalityflavor>
            <personalityflavor name="Deceptive" value="2"></personalityflavor>
            <personalityflavor name="Diplomatic" value="8"></personalityflavor>
            <personalityflavor name="Vicious" value="5"></personalityflavor>*/

            switch (chosenFlavor) {
                case "Cooperative":
                    return 30;
                case "Jealous":
                    return -40;
                case "Friendly":
                    return 50;
                case "Hostile":
                    return -50;
                case "Loyal":
                    return 20;
                case "Deceptive":
                    return -40;
                case "Diplomatic":
                    return 40;
                case "Vicious":
                    return -30;
                default:
                    System.out.println("Error, could not find first flavor");
                    return 0;
            }
        }

        public String getOpinionString(Clan c) {
            if (!deceiving.get(c)) {
                return opinions.get(c).toString();
            }
            else {
                RelationOpinion opinion;
                int score = relationScore.get(c), trust = this.trust.get(c);
                double scoreDiff;
                if (calculatedClanScores.get(subjectClan) == 0) {
                    scoreDiff = 1.0;
                } else {
                    scoreDiff = calculateScoreDiff(c);
                }
                if (scoreDiff > 1.2d) {
                    opinion = RelationOpinion.INTIMIDATED;
                } else if (score > 45) {
                    if (trust > 2) {
                        opinion = RelationOpinion.FRIENDLY;
                    } else {
                        opinion = RelationOpinion.ALLIED;
                    }
                } else if (score > 15) {
                    if (trust > 3) {
                        opinion = RelationOpinion.ALLIED;
                    } else {
                        opinion = RelationOpinion.NEUTRAL;
                    }
                } else {
                    opinion = RelationOpinion.NEUTRAL;
                }
                return opinion.toString();
            }
        }

        public double calculateScoreDiff(Clan clan) {
            double scoreDiff;
            if (calculatedClanScores.get(subjectClan) == 0) {
                scoreDiff = 1.0;
            } else {
                scoreDiff = calculatedClanScores.get(clan) / calculatedClanScores.get(subjectClan);
            }
            return scoreDiff;
        }

    }

    public enum RelationModifier {
        ALLIED (50),

        AT_WAR (-80),
        DENOUNCE (-30),

        WAS_AT_WAR (-20);
        int score;
        RelationModifier(int s) {
            score = s;
        }
    }

    public enum RelationOpinion {
        FRIENDLY,
        ALLIED,
        NEUTRAL,
        ANGRY,
        HOSTILE,
        INTIMIDATED,
        AGGRESSIVE,
        DECEPTIVE;
    }

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

    public boolean atWar(Clan atk, Clan def) {
        //int atkId = clanId.get(atk), defId = clanId.get(def);
        List<RelationModifier> relationModifierList = relations.get(atk).map.get(def);
        return relationModifierList != null && relationModifierList.contains(RelationModifier.AT_WAR);
    }

    public void declareWar(Clan atk, Clan def) {
        RelationMap map = relations.get(atk);
        map.addMod(def, RelationModifier.AT_WAR);
    }

    public void makePeace(Clan atk, Clan def) {
        RelationMap map = relations.get(atk);
        map.removeMod(def, RelationModifier.AT_WAR);
    }

    public void addRelationModifier(Clan atk, Clan def, RelationModifier mod) {
        RelationMap map = relations.get(atk);
        map.addMod(def, mod);
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
        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            Object[] objects = city.gameYield();
            int[] yield = (int[]) objects[0];
            Inventory inventory = (Inventory) objects[1];

            int workingPopulation = city.population - city.freeWorkingPopulation;
            city.foodStoredForGrowth += yield[0] - workingPopulation * 2 - city.freeWorkingPopulation * 1;
            if (city.foodStoredForGrowth >= city.foodNeededForGrowth) {
                city.foodStoredForGrowth -= city.foodNeededForGrowth;

                city.population++; city.freeWorkingPopulation++;
                city.foodNeededForGrowth = City.cityFoodData()[city.population];
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
            clan.totalCulture += yield[6];

            totalResources.addAnotherInventory(inventory);
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
