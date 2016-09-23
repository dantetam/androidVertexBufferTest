package io.github.dantetam.world.action;

/**
 * Created by Dante on 9/22/2016.
 */
public class Benefit {

    public int[] yields;
    public float[] yieldFlatImpr;
    public int[] combat;
    public float[] combatFlatImpr;
    public String special;

    public Benefit() {
        yields = new int[7];
        yieldFlatImpr = new float[7];
        combat = new int[5];
        combatFlatImpr = new float[5];
        special = null;
    }

}
