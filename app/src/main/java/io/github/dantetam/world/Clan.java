package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public class Clan {

    public String name;
    public List<Person> people;
    public ClanType clanType;
    public ClanFaction clanFaction;

    public List<Building> buildings;

    public Clan(String n) {
        name = n;
        people = new ArrayList<>();
        buildings = new ArrayList<>();
    }

    public enum ClanType {
        CLAN_AGGRESSIVE,
        CLAN_INDUSTRIOUS,
        CLAN_TRADITIONAL,
        CLAN_SETTLER;
        public static ClanType[] types = null;
        public static int numClanTypes = -1;
        public static ClanType random() {
            if (types == null) {
                types = ClanType.values();
                numClanTypes = types.length;
            }
            return types[(int)(Math.random()*numClanTypes)];
        }
    }

    public enum ClanFaction {
        FACTION_BARBARIAN,
        FACTION_SAVAGE,
        FACTION_FOREIGNER,
        FACTION_CIVILIZED;
        public static ClanFaction[] types = null;
        public static int numClanFactions = -1;
        public static ClanFaction random() {
            if (types == null) {
                types = ClanFaction.values();
                numClanFactions = types.length;
            }
            return types[(int)(Math.random()*numClanFactions)];
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Clan)) return false;
        Clan clan = (Clan) obj;
        return name.equals(clan.name);
    }

}
