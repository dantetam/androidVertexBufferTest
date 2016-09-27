package io.github.dantetam.world.entity;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by Dante on 8/13/2016.
 */
public class PersonType {
    public String name;
    public int workNeeded;
    public FieldType fieldType;
    public String category;
    public int health, maxH, actionPoints, maxP;
    public int atk, def, maneuver, fire, shock;
    public int range = 0; //Range of 0 represents no ranged ability

    public String resourceNeeded;

    public String iconName;
    public String modelName, textureName;
    public float modelScale = 1;

    public boolean created = false;
    /*public void init() {
        if (!created) {
            types = new HashMap<>();
        }
    }
    public void addPersonType(String name, int a, int d, int m, int f, int s) {
        PersonType newType = new PersonType();
        newType.name = name;
        newType.PersonType(a, d, m, f, s);
        types.put(name, newType);
    }*/
    public PersonType(String n, FieldType ft, String ca, int h, int mh, int p, int mp, int a, int d, int m, int f, int s) {
        name = n;
        fieldType = ft;
        category = ca;
        iconName = "science";
        health = h; maxH = mh;
        actionPoints = p; maxP = mp;
        atk = a; def = d;
        maneuver = m; fire = f; shock = s;
    }

    public enum FieldType {
        LAND,
        SEA,
        AIR,
        UNDERGROUND;
        public static FieldType fromString(String string) {
            if (string.equalsIgnoreCase("land")) {
                return LAND;
            }
            else if (string.equalsIgnoreCase("sea")) {
                return SEA;
            }
            else if (string.equalsIgnoreCase("air")) {
                return AIR;
            }
            else if (string.equalsIgnoreCase("underground")) {
                return UNDERGROUND;
            }
            else {
                //System.err.println("Invalid type of unit fieldType: " + string);
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
