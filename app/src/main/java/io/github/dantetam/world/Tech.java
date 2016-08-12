package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 7/4/2016.
 */
public class Tech {

    public String name;
    public Tech parent;
    public List<Tech> extraReqs;
    public List<Tech> unlockedTechs;

    public List<BuildingType> unlockedAbilities;
    public List<Person.PersonType> unlockedUnits;
    public List<ItemType> harvestableResources;

    public int researchCompleted, researchNeeded;

    public Tech(String n, int researchCompleted, int researchNeeded) {
        name = n;
        this.researchCompleted = researchCompleted;
        this.researchNeeded = researchNeeded;

        //unlocked = false;
        extraReqs = new ArrayList<>();
        unlockedTechs = new ArrayList<>();
        unlockedAbilities = new ArrayList<>();
        unlockedUnits = new ArrayList<>();
        harvestableResources = new ArrayList<>();
        //allowedBuildingsAndModules = new HashMap<>();
    }

    public boolean unlocked() {
        return researchCompleted >= researchNeeded;
    }

    public void research(int researchAmount) {
        researchCompleted += researchAmount;
    }

    public void forceUnlock() {
        researchCompleted = researchNeeded;
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
