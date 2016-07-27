package io.github.dantetam.world;

import java.util.List;

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

}
