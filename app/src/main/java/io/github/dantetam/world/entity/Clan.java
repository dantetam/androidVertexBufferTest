package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.utilmath.Vector4f;

/**
 * Created by Dante on 6/13/2016.
 */
public class Clan {

    public ArtificialIntelligence ai;

    public String name;
    public Vector4f color, reducedColor;
    public Vector4f secondaryColor, reducedSecondaryColor;
    public List<Person> people;
    public ClanType clanType;
    public ClanFaction clanFaction;

    public TechTree techTree;
    public UnitTree unitTree;
    public BuildingTree buildingTree;

    public List<Building> buildings;
    public List<City> cities;
    public Inventory resources;

    public Clan(String n) {
        name = n;
        ai = new ArtificialIntelligence(this);
        people = new ArrayList<>();
        buildings = new ArrayList<>();
        cities = new ArrayList<>();
        resources = new Inventory();
        //techTree = new TechTree(this);
    }

    public enum ClanType {
        CLAN_AGGRESSIVE,
        CLAN_INDUSTRIOUS,
        CLAN_TRADITIONAL,
        CLAN_SETTLER;
        public static ClanType[] types = null;
        private static int numClanTypes = -1;
        /*public static int getNumClanTypes() {
            if (types == null) {
                init();
            }
            return numClanTypes;
        }*/
        public static ClanType random() {
            if (types == null) {
                init();
            }
            return types[(int)(Math.random()*numClanTypes)];
        }
        private static void init() {
            types = ClanType.values();
            numClanTypes = types.length;
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
        return name.equals(clan.name) && color.equals(clan.color) ;
    }

    /*public int hashCode() {
        return name.hashCode();
    }*/

}
