package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 7/4/2016.
 */
public class Tech {

    public String name;
    private boolean unlocked;
    public List<Tech> unlockedTechs;

    private List<String> unlockedAbilities;

    public Tech(String n) {
        name = n;
        unlocked = false;
        unlockedTechs = new ArrayList<>();
        unlockedAbilities = new ArrayList<>();
        //allowedBuildingsAndModules = new HashMap<>();
    }

    public boolean unlocked() {
        return unlocked;
    }

    public List<String> unlock() {
        unlocked = true;
        return unlockedAbilities;
    }

    public boolean hasUnresearchedChildren() {
        for (Tech tech: unlockedTechs) {
            if (!tech.unlocked) {
                return true;
            }
        }
        return false;
    }

}
