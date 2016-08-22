package io.github.dantetam.world.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.entity.Clan;

/**
 * Created by Dante on 7/5/2016.
 */
public class Influence {

    private HashMap<Clan, Integer> clanInfluences;

    public Influence(List<Clan> clans) {
        clanInfluences = new HashMap<Clan, Integer>();
        for (Clan c: clans) {
            addClan(c);
        }
    }

    public void addClan(Clan c) {
        clanInfluences.put(c, 0);
    }

    public void addClanInfluence(Clan c, int influence) {
        if (!clanInfluences.containsKey(c)) {
            System.out.println("Warning, clan influence hashmap has no entry " + c.name);
            addClan(c);
        }
        clanInfluences.put(c, clanInfluences.get(c) + influence);
    }

    public HashMap<Clan, Float> calculatePercentages() {
        HashMap<Clan, Float> data = new HashMap<>();
        float total = 0;
        for (Map.Entry<Clan, Integer> en: clanInfluences.entrySet()) {
            total += en.getValue();
        }
        for (Map.Entry<Clan, Integer> en: clanInfluences.entrySet()) {
            data.put(en.getKey(), (float) en.getValue() / total);
        }
        return data;
    }

    public Clan influencingClan() {
        HashMap<Clan, Float> data = calculatePercentages();
        Clan maxClan = null;
        float maxPercentage = 0;
        for (Map.Entry<Clan, Float> en: data.entrySet()) {
            if (maxClan == null || en.getValue() > maxPercentage) {
                maxClan = en.getKey();
                maxPercentage = en.getValue();
            }
        }
        return maxClan;
    }

    public float percentInfluenceOfClan(Clan clan) {
        HashMap<Clan, Float> data = calculatePercentages();
        for (Map.Entry<Clan, Float> en: data.entrySet()) {
            if (en.getKey().equals(clan))
                return en.getValue();
        }
        return 0;
    }

}
