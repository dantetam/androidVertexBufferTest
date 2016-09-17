package io.github.dantetam.world.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.opstrykontest.WorldSystem;
import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.Clan;

/**
 * Created by Dante on 9/8/2016*.
 */
public class RelationMap {
    public Clan subjectClan;
    private HashMap<Clan, List<RelationModifier>> map;
    public List<RelationModifier> getRelations(Clan c) {return map.get(c);}
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
        //System.out.println("I am " + subjectClan.name + ", here is how I feel about these clans:");
        for (Clan c: clans) {
            if (c instanceof CityState) {
                continue;
            }
            System.out.println(c.name);
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

            //baseScore += getFirstFlavor();
            map.get(c).add(getFirstFlavor());

            baseScore = Math.max(-40, Math.min(40, baseScore));

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

            //System.out.println(c.name + ": trust -> " + trustScore + ", opinion -> " + baseScore + ", deceptive -> " + deceiving.get(c) + " (" + chanceOfDeception + ")");
        }
    }

    public void updateOpinions(List<Clan> clans) {
        for (Clan c: clans) {
            if (c instanceof CityState) continue;
            updateOpinionClan(c);
        }
    }

    public void updateOpinionClan(Clan c) {
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
            if (trust > 4) {
                opinion = RelationOpinion.FRIENDLY;
            } else if (trust > 2){
                opinion = RelationOpinion.ALLIED;
            } else {
                opinion = RelationOpinion.INTIMIDATED;
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

    private RelationModifier getFirstFlavor() {
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
            if (rand <= flavors[j] / runSum) {
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
                return RelationModifier.COOPERATIVE;
            case "Jealous":
                return RelationModifier.LACK_OF_TRUST;
            case "Friendly":
                return RelationModifier.COOPERATIVE;
            case "Hostile":
                return RelationModifier.HOSTILE;
            case "Loyal":
                return RelationModifier.COOPERATIVE;
            case "Deceptive":
                return RelationModifier.LACK_OF_TRUST;
            case "Diplomatic":
                return RelationModifier.COOPERATIVE;
            case "Vicious":
                return RelationModifier.HOSTILE;
            default:
                System.out.println("Error, could not find first flavor");
                return null;
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
            if (WorldSystem.calculatedClanScores.get(subjectClan) == 0) {
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
        if (WorldSystem.calculatedClanScores.get(subjectClan) == 0) {
            scoreDiff = 1.0;
        } else {
            scoreDiff = WorldSystem.calculatedClanScores.get(clan) / WorldSystem.calculatedClanScores.get(subjectClan);
        }
        return scoreDiff;
    }

    public List<String> getRelationModsForClan(Clan c) {
        List<String> strings = new ArrayList<>();
        for (RelationModifier mod: map.get(c)) {
            strings.add(mod.description);
        }
        return strings;
    }

}
