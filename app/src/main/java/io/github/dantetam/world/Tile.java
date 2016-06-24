package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public class Tile extends Representable {

    public int q, r;
    public int elevation;
    public Biome biome; //combined climate of land
    public Terrain terrain; //shape of the land
    public List<Resource> resources;
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
        private static float[][] colors = {
                {0,0,1},
                {0,0.5f,1},
                {0.5f,0.5f,1},
                {1,0.5f,0.5f},
                {0,1,0},
                {0,0.5f,0},
                {1,0.5f,0}
        };
        public static Biome fromInt(int n) {
            if (n >= 0 && n < types.length) {
                return types[n];
            }
            throw new IllegalArgumentException("Invalid biome type: " + n);
        }
        public static float[] colorFromInt(int n) {return colors[n];}
        public static final int numBiomes = types.length;
    }

    public enum Terrain {
        PLAINS (0),
        HILLS (1),
        CLIFFS (2),
        MOUNTAINS (3),
        SHALLOW_SEA (4),
        DEEP_SEA (5);
        public int type;
        Terrain(int t) {type = t;}
        private static Terrain[] types = {PLAINS, HILLS, CLIFFS, MOUNTAINS, SHALLOW_SEA, DEEP_SEA};
        public static Terrain fromInt(int n) {
            if (n >= 0 && n < types.length) {
                return types[n];
            }
            throw new IllegalArgumentException("Invalid terrain type: " + n);
        }
        public static final int numTerrains = types.length;
    }

    public enum Resource {
        NO_RESOURCE,
        WHEAT,
        FISH,
        IRON;
        private static Resource[] types = {NO_RESOURCE, WHEAT, FISH, IRON};
        public static final int numResources = types.length;
        public static Resource fromInt(int n) {
            if (n >= 0 && n < types.length) {
                return types[n];
            }
            throw new IllegalArgumentException("Invalid resource type: " + n);
        }
        public static Resource randomResource() {
            return Resource.fromInt((int) (Math.random() * (numResources - 1)) + 1);
        }
    }

    public Tile(int a, int c) {
        q = a; r = c;
        resources = new ArrayList<Resource>();
        occupants = new ArrayList<Entity>();
    }

    //public float dist(Tile t) {return (float) Math.sqrt(Math.pow(row - t.row, 2) + Math.pow(col - t.col, 2));}
    public float dist(Tile t) {
        return (Math.abs(q - t.q)
                + Math.abs(q + r - t.q - t.r)
                + Math.abs(r - t.r)) / 2;
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

    public int compareX(Tile a, Tile b) {return a.q - b.q;}
    public int compareY(Tile a, Tile b) {return a.r - b.r;}

}
