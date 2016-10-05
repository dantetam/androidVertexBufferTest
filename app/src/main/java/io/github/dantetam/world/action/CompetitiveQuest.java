package io.github.dantetam.world.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Person;

/**
 * Created by Dante on 10/2/2016.
 */
public abstract class CompetitiveQuest {

    public String desc;

    public Ability reward;
    public int[] flatReward;
    public int influence;
    public HashMap<Clan, Integer> clansScoreInitial, clansScoreFinal;

    public CompetitiveQuest(String text, List<Clan> clans) {
        desc = text;
        clansScoreInitial = new HashMap<>();
        clansScoreFinal = new HashMap<>();
        for (Clan clan: clans) {
            clansScoreInitial.put(clan, score(clan));
            clansScoreFinal.put(clan, 0);
        }
    }

    public static Clan takeHighest(HashMap<Clan, Integer> map) {
        Map<Clan, Integer> sorted = OpstrykonUtil.sortMapByValue(map);
        if (sorted.size() == 0) {
            return null;
        }
        return sorted.entrySet().iterator().next().getKey();
    }

    public abstract int score(Clan c);

    public abstract int turnScore(Clan c);

    public void turn(Clan c) {
        clansScoreFinal.put(c, clansScoreFinal.get(c) + turnScore(c));
    }

    public Clan evalFinish(List<Clan> clans) {
        //clansScoreFinal = new HashMap<>();
        for (Clan clan: clans) {
            clansScoreFinal.put(clan, clansScoreFinal.get(clan) + score(clan));
        }

        HashMap<Clan, Integer> temp = new HashMap<>();
        for (Clan clan: clans) {
            temp.put(clan, clansScoreFinal.get(clan) - clansScoreInitial.get(clan));
        }

        return CompetitiveQuest.takeHighest(temp);
    }

    public static CompetitiveQuest randomCityQuest(List<Clan> clans) {
        List<CompetitiveQuest> quests = new ArrayList<>();
        quests.add(new CompetitiveQuest(" is looking for the most industrious and productive empire.", clans) { //Gold quest
            public int score(Clan c) {
                return 0;
            }
            public int turnScore(Clan c) {
                return c.lastYield[1];
            }
        });
        quests.add(new CompetitiveQuest(" is looking for the most refined and enlightened civilization.", clans) {
            public int score(Clan c) {
                return 0;
            }
            public int turnScore(Clan c) {
                return c.lastYield[2];
            }
        });
        quests.add(new CompetitiveQuest(" desires to appreciate the most cultured people.", clans) {
            public int score(Clan c) {
                return 0;
            }
            public int turnScore(Clan c) {
                return c.lastYield[6];
            }
        });
        quests.add(new CompetitiveQuest(" is looking for the most militaristic empire for protection.", clans) {
            public int score(Clan c) {
                int i = 0;
                for (Person person: c.people) {
                    if (person.personType.category.equals("combat")) {
                        i += person.personType.atk + person.personType.def;
                    }
                }
                return i;
            }
            public int turnScore(Clan c) {
                return c.lastYield[6];
            }
            public Clan evalFinish(List<Clan> clans) {
                for (Clan clan: clans) {
                    clansScoreFinal.put(clan, score(clan));
                }
                HashMap<Clan, Integer> temp = new HashMap<>();
                for (Clan clan: clans) {
                    temp.put(clan, clansScoreFinal.get(clan));
                }
                return CompetitiveQuest.takeHighest(temp);
            }
        });
        return quests.get((int)(Math.random() * quests.size()));
    }

    /*public static CompetitiveQuest randomCityQuest() {
        List<CompetitiveQuest> quests = new ArrayList<>();
        quests.add(new CompetitiveQuest() {
            public void start(List<Clan> clans) {
                for (Clan clan: clans) {

                }
            }
            public HashMap<Clan, Integer> eval(List<Clan> clans) {
                for (Clan clan: clans) {

                }
                return CompetitiveQuest.takeHighest()
            }
        })
    }*/

}
