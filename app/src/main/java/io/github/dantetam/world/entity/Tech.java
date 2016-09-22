package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.world.action.Ability;

/**
 * Created by Dante on 7/4/2016.
 */
public class Tech {

    public String name;
    public Tech parent;
    public List<Tech> extraReqs;
    public List<Tech> unlockedTechs;

    private boolean researched = false;

    public List<BuildingType> unlockedBuildings;
    public List<BuildingType> unlockedDistricts;
    public List<PersonType> unlockedUnits;
    public List<ItemType> revealResources;
    public List<ItemType> harvestableResources;
    public List<Ability> unlockedSpecialAbilities;

    public int researchCompleted, researchNeeded;

    public String iconName;
    public int treeOffsetX, treeOffsetY;

    public Tech(String n, int researchCompleted, int researchNeeded) {
        name = n;
        iconName = "science";
        this.researchCompleted = researchCompleted;
        this.researchNeeded = researchNeeded;

        //unlocked = false;
        extraReqs = new ArrayList<>();
        unlockedTechs = new ArrayList<>();
        unlockedBuildings = new ArrayList<>();
        unlockedDistricts = new ArrayList<>();
        unlockedUnits = new ArrayList<>();
        revealResources = new ArrayList<>();
        harvestableResources = new ArrayList<>();
        unlockedSpecialAbilities = new ArrayList<>();
        //allowedBuildingsAndModules = new HashMap<>();
    }

    public boolean researched() {
        return researched;
    }

    public boolean researchable() {
        for (Tech req: extraReqs) {
            if (!req.researched()) {
                return false;
            }
        }
        if (parent != null) {
            return parent.researched();
        }
        return !researched;
    }

    public void research(int researchAmount) {
        researchCompleted += researchAmount;
        if (researchCompleted >= researchNeeded) {
            researched = true;
        }
    }

    public void forceUnlock() {
        researched = true;
        researchCompleted = researchNeeded;
    }

    public boolean hasUnresearchedChildren() {
        for (Tech tech: unlockedTechs) {
            if (!tech.researched()) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Tech)) {
            return false;
        }
        Tech tech = (Tech) other;
        return name.equals(tech.name);
    }

}
