package io.github.dantetam.world.entity;

import java.util.HashMap;

/**
 * Created by Dante on 8/13/2016.
 */
public class PersonType {
    public String name;
    public int workNeeded;
    public int health, maxH, actionPoints, maxP;
    public int atk, def, maneuver, fire, shock;

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
    public PersonType(String n, int h, int mh, int p, int mp, int a, int d, int m, int f, int s) {
        name = n;
        iconName = "science";
        health = h; maxH = mh;
        actionPoints = p; maxP = mp;
        atk = a; def = d;
        maneuver = m; fire = f; shock = s;
    }

}
