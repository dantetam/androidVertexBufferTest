package io.github.dantetam.world.entity;

import io.github.dantetam.world.action.CompetitiveQuest;

/**
 * Created by Dante on 9/15/2016.
 */
public class CityState extends Clan {

    //public String cityName;

    public ClanType clanType;
    public ClanFaction clanFaction;

    public CompetitiveQuest activeQuest;

    public CityState(String name) {
        super(name);
    }

}
