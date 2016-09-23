package io.github.dantetam.world.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.opstrykontest.Condition;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Tile;

/**
 * Created by Dante on 8/1/2016.
 */
public class Ability {

    public String name, desc, code;
    public List<Benefit> benefits;
    public List<Condition> abilityOnConditions;
    public boolean chain = false;

    public static void init() {
        yieldAbbrev = new HashMap<>();
        yieldAbbrev.put("F", 0);
        yieldAbbrev.put("P", 1);
        yieldAbbrev.put("S", 2);
        yieldAbbrev.put("C", 3);
        yieldAbbrev.put("HA", 4);
        yieldAbbrev.put("HE", 5);
        yieldAbbrev.put("CU", 6);

        combatAbbrev.put("ATK", 0);
        combatAbbrev.put("DEF", 1);
        combatAbbrev.put("MAN", 2);
        combatAbbrev.put("FIRE", 3);
        combatAbbrev.put("SHOCK", 4);
    }

    public Ability(String n, String d, String c) {
        name = n;
        desc = d;
        code = c;

        benefits = new ArrayList<>();
        abilityOnConditions = new ArrayList<>();

        parseCode(code);
    }

    public void parseCode(String stringy) {
        stringy = stringy.replaceAll("\\s+", "");
        String[] splitStringy = stringy.split("/");
        String[] benefitsString = splitStringy[0].split(",");
        for (String benefit: benefitsString) {
            benefits.add(parseBenefit(benefit));
        }
        String[] conditions;
        if (splitStringy[1].contains("&")) {
            conditions = splitStringy[1].split("&");
            chain = true;
        }
        else {
            conditions = splitStringy[1].split(",");
        }
        for (String condition: conditions) {
            abilityOnConditions.add(parseCondition(condition));
        }
    }

    //+1 F/P/...
    //%15 F/P/...
    //+5 CF/CS..
    public static HashMap<String, Integer> yieldAbbrev, combatAbbrev;

    public Benefit parseBenefit(String benefit) {
        if (benefit.startsWith("+") || benefit.startsWith("-") || benefit.startsWith("%") || benefit.startsWith("-%")) {
            if (benefit.startsWith("-%")) {
                benefit = benefit.substring(2);
            } else {
                benefit = benefit.substring(1);
            }
            int i = 0;
            for (; i < benefit.length(); i++) {
                if (Character.isDigit(benefit.charAt(i)) || benefit.charAt(i) == '.') {
                    continue;
                } else {
                    break;
                }
            }

            float value = Float.parseFloat(benefit.substring(0, i));
            if (benefit.contains("-")) {
                value *= -1;
            }

            Integer indexYield = yieldAbbrev.get(benefit.substring(i).toUpperCase());
            Integer indexCombat = combatAbbrev.get(benefit.substring(i).toUpperCase());

            Benefit newBenefit = new Benefit();

            if (benefit.contains("%")) {
                if (indexYield != null) {
                    newBenefit.yields[indexYield] = (int)value;
                } else {
                    newBenefit.combat[indexCombat] = (int)value;
                }
            }
            else {
                if (indexYield != null) {
                    newBenefit.yieldFlatImpr[indexYield] = value;
                } else {
                    newBenefit.combatFlatImpr[indexCombat] = value;
                }
            }
            return newBenefit;
        } else {
            throw new IllegalArgumentException("Requires +, -, or % in parsed benefit string");
        }
        //return null;
    }

    public Condition parseCondition(String condition) {

    }

    public Action.ActionStatus gameExecuteAbility(Entity person) {
        return Action.ActionStatus.ALREADY_COMPLETED;
    }

    public class ContainsModuleCondition extends Condition {
        public BuildingType target;
        public void init(Object obj) {target = (BuildingType) obj;}
        public boolean allowed(Object obj) {
            if (!(obj instanceof City)) return false;
            City city = (City) obj;
            for (Building module: city.modules) {
                if (module.buildingType.equals(target)) {
                    return true;
                }
            }
            return false;
        }
    }

    public class BuildingCondition extends Condition {
        public BuildingType target;
        public void init(Object obj) {target = (BuildingType) obj;}
        public boolean allowed(Object obj) {
            return target.equals((BuildingType) obj);
        }
    }

    public class TileCondition extends Condition {
        public Tile.Biome biome;
        public Tile.Terrain terrain;
        public void init(Object obj) {
            if (obj instanceof Tile.Biome) {
                biome = (Tile.Biome) obj;
            } else if (obj instanceof Tile.Terrain) {
                terrain = (Tile.Terrain) obj;
            }
        }
        public void init(Object obj1, Object obj2) {
            init(obj1);
            init(obj2);
        }
        public boolean allowed(Object obj) {
            boolean cond = false;
            if (biome != null) {
                cond = cond || biome.equals((Tile.Biome) obj);
            }
            if (terrain != null) {
                cond = cond || terrain.equals((Tile.Terrain) obj);
            }
            return cond;
        }
    }

}
