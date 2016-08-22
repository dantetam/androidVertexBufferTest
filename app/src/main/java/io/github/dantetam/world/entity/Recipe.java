package io.github.dantetam.world.entity;

/**
 * Created by Dante on 7/27/2016.
 */
public class Recipe {

    public Item[] input, output;

    public Recipe() {

    }

    public Recipe addIn(Item... items) {
        input = items;
        return this;
    }

    public Recipe addOut(Item... items) {
        output = items;
        return this;
    }

    public String stored = null;
    public String toString() {
        if (stored == null) {
            String inString = "", outString = "";
            for (int i = 0; i < input.length; i++) {
                inString += input[i].toString();
                if (i != input.length - 1) {
                    inString += " + ";
                }
            }
            for (int i = 0; i < output.length; i++) {
                outString += output[i].toString();
                if (i != output.length - 1) {
                    outString += " + ";
                }
            }
            stored = inString + " -> " + outString;
        }
        return stored;
    }

}
