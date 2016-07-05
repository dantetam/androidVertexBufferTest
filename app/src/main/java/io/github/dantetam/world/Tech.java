package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 7/4/2016.
 */
public class Tech {

    public String name;
    public List<Tech> unlockedTechs;

    public List<String> unlockedAbilities;

    public Tech(String n) {
        name = n;
        unlockedTechs = new ArrayList<>();
        unlockedAbilities = new ArrayList<>();
    }

}
