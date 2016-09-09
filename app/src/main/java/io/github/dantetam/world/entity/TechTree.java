package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.opstrykontest.Condition;
import io.github.dantetam.utilmath.Vector2f;
import io.github.dantetam.world.action.Ability;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.Tech;

/**
 * Created by Dante on 7/4/2016.
 */
public class TechTree {

    public Clan clan;
    public Tech root;

    public HashMap<String, Tech> techMap;
    public HashMap<Tech, Boolean> researchedTech;

    public static HashMap<String, ItemType> itemTypes;

    public List<Tech> researchingTechQueue;

    //public HashMap<BuildingType, List<BuildingType>> allowedModules;
    public HashMap<BuildingType, Boolean> allowedBuildings, allowedDistricts;
    public HashMap<BuildingType, List<BuildingType>> allowedBuildingsAndModules;
    public HashMap<PersonType, Boolean> allowedUnits;
    public HashMap<ItemType, Boolean> allowedHarvestable;
    public HashMap<Ability, Boolean> specialAbilities;

    public static HashMap<BuildingType, Boolean> wonders = new HashMap<>();

    //These values are used for interfaces that render the tech tree GUI
    //presumably as a GridLayout.
    public float screenCenterX, screenCenterY;
    public int sightX = 2, sightY = 3;
    public int minX, maxX, minY, maxY;

    public TechTree(Clan clan) {
        this.clan = clan;
        clan.techTree = this;

        techMap = new HashMap<>();
        researchedTech = new HashMap<>();

        researchingTechQueue = new ArrayList<>();

        allowedBuildings = new HashMap<>();
        allowedDistricts = new HashMap<>();

        allowedBuildingsAndModules = new HashMap<>();

        allowedUnits = new HashMap<>();
        allowedHarvestable = new HashMap<>();
        specialAbilities = new HashMap<>();

        //TODO: Define a method for parsing a tech tree from XML using Android utilities
    }

    //Put a specified amount of research into the tree. Return any extra left.
    public int research(int inputScience) {
        if (researchingTechQueue.size() == 0) {
            throw new RuntimeException(clan.name + " is not researching a technology.");
            //return;
        }
        Tech researching = researchingTechQueue.get(0);
        researching.research(inputScience);
        if (researching.researched()) {
            activateTechAbilities(researching);
            researchingTechQueue.remove(0);
            return researching.researchCompleted - researching.researchNeeded;
        }
        return 0;
    }

    public void forceUnlock(Tech tech) {
        tech.forceUnlock();
        activateTechAbilities(tech);
    }

    public void activateTechAbilities(Tech tech) {
        researchedTech.put(tech, true);

        for (BuildingType buildingType: tech.unlockedBuildings) {
            allowedBuildings.put(buildingType, true);
        }
        for (BuildingType buildingType: tech.unlockedDistricts) {
            allowedDistricts.put(buildingType, true);
        }
        for (PersonType personType: tech.unlockedUnits) {
            /*if (personType == null) {
                throw new IllegalArgumentException("For tech: " + tech.name + ", could not find " + stringy + " in unit tree");
            }*/
            allowedUnits.put(personType, true);
        }
        for (ItemType itemType: tech.harvestableResources) {
            allowedHarvestable.put(itemType, true);
        }
        for (Ability ability: tech.unlockedSpecialAbilities) {
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

    /*
    Recursive beeline of a tech through its 'natural' prereqs (parent within the tree structure),
    as well as its 'artificial' prereqs (extra prereqs added on and defined, since it makes
    no sense for a node in a tree to have two direct parents).

    Add all necessary techs as well as the first tech argument called to the research queue.
    Topological sort?
     */
    public void beeline(Tech tech) {
        if (researchingTechQueue.contains(tech)) {
            return;
        }
        if (tech.researched()) {
            return;
        }
        if (tech.researchable()) {

        }
        else {
            beeline(tech.parent);
            for (Tech extra: tech.extraReqs) {
                beeline(extra);
            }
        }
        if (!researchingTechQueue.contains(tech))
            researchingTechQueue.add(tech);
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
                return tech.researched() && tech.hasUnresearchedChildren();
            }
        };
        return traverse(borderCondition);
    }

    public List<Tech> getResearchableTech() {
        List<Tech> borderTech = findBorderTech();
        List<Tech> researchable = new ArrayList<>();
        for (Tech t: borderTech) {
            for (Tech child: t.unlockedTechs) {
                if (!child.researched()) {
                    researchable.add(child);
                }
            }
        }
        return researchable;
    }

    public void unlock(String techNameToUnlock) {
        /*Condition matchCondition = new Condition() {
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
        }*/
        Tech techToUnlock = techMap.get(techNameToUnlock);
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

    public void modifyX(float dx) {
        screenCenterX += dx;
        if (screenCenterX < minX) screenCenterX = minX;
        if (screenCenterX > maxX - 3) screenCenterX = maxX - 3;
    }

    public void modifyY(float dy) {
        screenCenterY += dy;
        if (screenCenterY < minY) screenCenterY = minY;
        if (screenCenterY > maxY - 3) screenCenterY = maxY - 3;
    }

}
