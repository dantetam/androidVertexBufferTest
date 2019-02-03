package io.github.dantetam.opstrykontest;

/**
 * Created by Dante on 10/13/2016.
 */
public class WorldParams {

    public int len1, len2, numCivs;
    public int difficulty;

    public String terrainType, civChoice;

    public WorldParams(int q, int r, int civs, int d, String t, String clan) {
        len1 = q; len2 = r;
        numCivs = civs;
        difficulty = d;
        terrainType = t;
        civChoice = clan;
    }

}
