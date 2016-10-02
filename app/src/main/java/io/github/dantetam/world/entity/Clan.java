package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.world.action.Quest;
import io.github.dantetam.world.ai.ArtificialIntelligence;

/**
 * Created by Dante on 9/15/2016.
 */
public class Clan extends PoliticalEntity {

    public ArtificialIntelligence ai;
    public List<String> cityNames;

    public Quest personalQuest;

    public Clan(String n) {
        super(n);
        ai = new ArtificialIntelligence(this);
        cityNames = new ArrayList<>();
    }

}
