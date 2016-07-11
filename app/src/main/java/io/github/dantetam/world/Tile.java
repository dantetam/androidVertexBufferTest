package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public class Tile extends Representable implements Traversable<Tile> {

    public World world;
    public int q, r;
    public int elevation;
    public Biome biome; //combined climate of land
    public Terrain terrain; //shape of the land
    public Building improvement;
    public List<Item> resources;
    public List<Entity> occupants;

    public enum Biome {
        SEA (0),
        ICE (1),
        TUNDRA (2),
        DESERT (3),
        STEPPE (4),
        FOREST (5),
        RAINFOREST (6);
        public int type;
        Biome(int t) {type = t;}
        private static Biome[] types = {SEA, ICE, TUNDRA, DESERT, STEPPE, FOREST, RAINFOREST};
        private static String[] names = {"Sea", "Ice", "Tundra", "Desert", "Steppe", "Forest", "Rainforest"};
        private static float[][] colors = {
                {0,0,255,255},
                {0,150,1,255},
                {150,150,1,255},
                {255,150,150,255},
                {0,255,0,255},
                {0,150,0,255},
                {250,155,0,255}
        };
        public static Biome fromInt(int n) {
            if (n >= 0 && n < types.length) {
                return types[n];
            }
            throw new IllegalArgumentException("Invalid biome type: " + n);
        }
        public static String nameFromInt(int n) {
            if (n >= 0 && n < names.length) {
                return names[n];
            }
            throw new IllegalArgumentException("Invalid biome type: " + n);
        }
        public static float[] colorFromInt(int n) {return colors[n];}
        public static final int numBiomes = types.length;
    }

    public enum Terrain {
        SHALLOW_SEA (0),
        DEEP_SEA (1),
        PLAINS (2),
        HILLS (3),
        CLIFFS (4),
        MOUNTAINS (5);
        public int type;
        Terrain(int t) {type = t;}
        private static Terrain[] types = {SHALLOW_SEA, DEEP_SEA, PLAINS, HILLS, CLIFFS, MOUNTAINS};
        private static String[] names = {"Shallow Waters", "Deep Waters", "Plains", "Hills", "Cliffs", "Mountains"};
        public static Terrain fromInt(int n) {
            if (n >= 0 && n < types.length) {
                return types[n];
            }
            throw new IllegalArgumentException("Invalid terrain type: " + n);
        }
        public static String nameFromInt(int n) {
            if (n >= 0 && n < names.length) {
                return names[n];
            }
            throw new IllegalArgumentException("Invalid terrain type: " + n);
        }
        public static final int numTerrains = types.length;
        public static final int numSeaTerrains = 2;
    }

    public Tile(World world, int a, int b) {
        this.world = world;
        q = a; r = b;
        resources = new ArrayList<Item>();
        occupants = new ArrayList<Entity>();
    }

    //public float dist(Tile t) {return (float) Math.sqrt(Math.pow(row - t.row, 2) + Math.pow(col - t.col, 2));}
    public float dist(Tile t) {
        return (Math.abs(q - t.q)
                + Math.abs(q + r - t.q - t.r)
                + Math.abs(r - t.r)) / 2;
    }

    public List<Tile> neighbors() {
        return world.neighbors(this);
    }

    public String toString() {
        return "Tile: (" + q + ", " + r;
    }

    public int compare(Tile a, Tile b) { //Default behavior
        int dy = compareY(a, b);
        if (dy != 0) {
            return dy;
        }
        return compareX(a, b);
    }

    public boolean equals(Object a) {
        if (!(a instanceof Tile)) {
            return false;
        }
        Tile t = (Tile) a;
        return q == t.q && r == t.r;
    }

    public int hashCode() {
        return q*100 + r;
    }

    public int compareX(Tile a, Tile b) {return a.q - b.q;}
    public int compareY(Tile a, Tile b) {return a.r - b.r;}

}
