package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.opstrykontest.Condition;

/**
 * Created by Dante on 7/4/2016.
 */
public class TechTree {

    public Clan clan;
    public Tech root;

    public List<Tech> researchingTechQueue;

    //public HashMap<BuildingType, List<BuildingType>> allowedModules;
    public HashMap<BuildingType, Boolean> allowedBuildings;
    public HashMap<BuildingType, List<BuildingType>> allowedBuildingsAndModules;
    public HashMap<Person.PersonType, Boolean> allowedUnits;
    public HashMap<ItemType, Boolean> allowedHarvestable;
    public HashMap<String, Boolean> specialAbilities;

    public TechTree(Clan clan) {
        this.clan = clan;
        clan.techTree = this;

        researchingTechQueue = new ArrayList<>();

        allowedBuildings = new HashMap<>();

        allowedBuildingsAndModules = new HashMap<>();

        List<BuildingType> list = new ArrayList<>();
        list.add(BuildingType.FARM);
        allowedBuildingsAndModules.put(BuildingType.CITY, list);

        allowedUnits = new HashMap<>();

        allowedHarvestable = new HashMap<>();

        specialAbilities = new HashMap<>();

        //TODO: Define a method for parsing a tech tree from XML using Android utilities
    }

    public void research(int inputScience) {
        if (researchingTechQueue.size() == 0) {
            return;
        }
        Tech researching = researchingTechQueue.get(0);
        if (researching.unlocked()) {
            activateTechAbilities(researching);
        }
        researchingTechQueue.remove(0);
    }

    public void forceUnlock(Tech tech) {
        tech.forceUnlock();
        activateTechAbilities(tech);
    }

    public void activateTechAbilities(Tech tech) {
        for (BuildingType buildingType: tech.unlockedBuildings) {
            allowedBuildings.put(buildingType, true);
        }
        for (Person.PersonType personType: tech.unlockedUnits) {
            allowedUnits.put(personType, true);
        }
        for (ItemType itemType: tech.harvestableResources) {
            allowedHarvestable.put(itemType, true);
        }
        for (String ability: tech.unlockedSpecialAbilities) {
            specialAbilities.put(ability, true);
        }
        /*for (String stringy: strings) {
            if (stringy.startsWith("AddBuilding")) {
                String[] split = stringy.split("/");
                String building = split[1];
                BuildingType parent = BuildingType.fromString(building);
                allowedBuildingsAndModules.put(parent, new ArrayList<BuildingType>());
            }
            else if (stringy.startsWith("AddModule")) {
                String[] split = stringy.split("/");
                String building = split[1], moduleToAdd = split[2];
                BuildingType parent = BuildingType.fromString(building);
                BuildingType module = BuildingType.fromString(moduleToAdd);
                if (allowedBuildingsAndModules.get(parent) == null) {
                    allowedBuildingsAndModules.put(parent, new ArrayList<BuildingType>());
                }
                allowedBuildingsAndModules.get(parent).add(module);
            }
        }*/
    }

    public List<Tech> traverse(Condition cond) {
        return traverse(root, cond);
    }
    private List<Tech> traverse(Tech techInspect, Condition cond) {
        List<Tech> techFulfillingCond = new ArrayList<>();
        if (cond.allowed(techInspect)) {
            techFulfillingCond.add(techInspect);
            int children = techInspect.unlockedTechs.size();
            if (children > 0) {
                for (int i = 0; i < children; i++) {
                    Tech techChildInspect = techInspect.unlockedTechs.get(i);
                    List<Tech> techChildFulfillingCond = traverse(techChildInspect, cond);
                    for (Tech t: techChildFulfillingCond) {
                        techFulfillingCond.add(t);
                    }
                }
            }
        }
        return techFulfillingCond;
    }

    public List<Tech> findBorderTech() {
        Condition borderCondition = new Condition() {
            //public String match = null;
            public boolean allowed(Object obj) {
                if (!(obj instanceof Tech)) return false; //Safety check
                Tech tech = (Tech) obj;
                return tech.unlocked() && tech.hasUnresearchedChildren();
            }
        };
        return traverse(borderCondition);
    }

    public List<Tech> getResearchableTech() {
        List<Tech> borderTech = findBorderTech();
        List<Tech> researchable = new ArrayList<>();
        for (Tech t: borderTech) {
            for (Tech child: t.unlockedTechs) {
                if (!child.unlocked()) {
                    researchable.add(child);
                }
            }
        }
        return researchable;
    }

    public void unlock(String techNameToUnlock) {
        Condition matchCondition = new Condition() {
            public String match = null;
            public boolean allowed(Object obj) {
                if (!(obj instanceof Tech)) return false; //Safety check
                Tech tech = (Tech) obj;
                return tech.name.equals(match);
            }
            public void init(String s) {
                match = s;
            }
        };
        matchCondition.init(techNameToUnlock); //Circumvent anonymous extends restriction
        List<Tech> candidate = traverse(matchCondition);
        if (candidate.size() != 1) {
            return;
        }
        Tech techToUnlock = candidate.get(0);
        forceUnlock(techToUnlock);
        //TODO: Unlock the tech
    }

    public void traverseAndPrint() {
        traverseAndPrint(root, 0);
        /*List<Tech> fringe = new ArrayList<>();
        fringe.add(root);
        while (true) {
            Tech first = fringe.remove(0);
            //System.out.println(first);
            System.out.println(first.name);
            for (Tech child: first.unlockedTechs) {
                fringe.add(child);
            }
            if (fringe.size() == 0) return;
        }*/
    }
    private void traverseAndPrint(Tech t, int level) {
        String stringy = "";
        for (int i = 0; i < level; i++) {
            stringy += ".   .";
        }
        System.out.println(stringy + t.name);
        for (Tech tech: t.unlockedTechs) {
            traverseAndPrint(tech, level + 1);
        }
    }

}
