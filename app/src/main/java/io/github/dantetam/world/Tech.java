package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 7/4/2016.
 */
public class Tech {

    public String name;
    public List<Tech> unlockedTechs;

    private List<String> unlockedAbilities;

    public int researchCompleted, researchNeeded;

    public Tech(String n, int researchCompleted, int researchNeeded) {
        name = n;
        this.researchCompleted = researchCompleted;
        this.researchNeeded = researchNeeded;

        //unlocked = false;
        unlockedTechs = new ArrayList<>();
        unlockedAbilities = new ArrayList<>();
        //allowedBuildingsAndModules = new HashMap<>();
    }

    public boolean unlocked() {
        return researchCompleted >= researchNeeded;
    }

    public List<String> research(int researchAmount) {
        researchCompleted += researchAmount;
        return unlockedAbilities;
    }

    public List<String> forceUnlock() {
        return research(researchNeeded);
    }

    public boolean hasUnresearchedChildren() {
        for (Tech tech: unlockedTechs) {
            if (!tech.unlocked()) {
                return true;
            }
        }
        return false;
    }

}
