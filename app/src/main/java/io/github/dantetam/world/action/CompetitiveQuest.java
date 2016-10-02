package io.github.dantetam.world.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.entity.Clan;

/**
 * Created by Dante on 10/2/2016.
 */
public abstract class CompetitiveQuest {

    public Ability reward;
    public int[] flatReward;
    public int influence;

    public HashMap<Clan, Integer> eval(List<Clan> clans) {
        return null;
    }

    public static Clan takeHighest(HashMap<Clan, Integer> map) {
        Map<Clan, Integer> sorted = OpstrykonUtil.sortMapByValue(map);
        if (sorted.size() == 0) {
            return null;
        }
        return sorted.entrySet().iterator().next().getKey();
    }

}
