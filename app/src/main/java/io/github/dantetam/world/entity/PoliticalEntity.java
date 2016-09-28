package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.utilmath.Vector4f;
import io.github.dantetam.world.ai.ArtificialIntelligence;

/**
 * Created by Dante on 6/13/2016.
 */
public class PoliticalEntity {

    public String name;
    public String adjective;
    public List<String> cityNames;

    public World world;

    public Vector4f color, reducedColor;
    public Vector4f secondaryColor, reducedSecondaryColor;
    public List<Person> people;

    public TechTree techTree;
    public UnitTree unitTree;
    public BuildingTree buildingTree;
    public IdeologyTree ideologyTree;

    public List<Building> buildings;
    public List<City> cities;
    public Inventory resources;

    public int totalGold; //, totalCulture;
    public int totalIdeologyPower;
    public int lastHappiness = 4;

    public PoliticalEntity(String n) {
        name = n;
        people = new ArrayList<>();
        buildings = new ArrayList<>();
        cities = new ArrayList<>();
        cityNames = new ArrayList<>();
        resources = new Inventory();
        totalGold = 0;
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
        if (!(obj instanceof PoliticalEntity)) return false;
        PoliticalEntity clan = (PoliticalEntity) obj;
        return name.equals(clan.name) && color.equals(clan.color);
    }

    public int hashCode() {
        return name.hashCode();
    }

}
