package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.opstrykontest.Condition;

/**
 * Created by Dante on 7/4/2016.
 */
public class UnitTree {

    public Clan clan;
    public Unit root;

    public HashMap<String, PersonType> personTypes;

    //A node class for this type of tree. Should not be exposed.
    //Info from this tree should be provided by encapsulated methods.
    public static class Unit {
        public PersonType personType;
        public List<Unit> unlockedUnits;
        public Unit(PersonType type) {
            personType = type;
            unlockedUnits = new ArrayList<>();
        }
    }

    public List<Person> allowedUnits;

    public UnitTree(Clan clan) {
        this.clan = clan;
        clan.unitTree = this;
        personTypes = new HashMap<>();
        allowedUnits = new ArrayList<>();
    }

    public void traverseAndPrint() {
        traverseAndPrint(root, 0);
    }
    private void traverseAndPrint(Unit u, int level) {
        String stringy = "";
        for (int i = 0; i < level; i++) {
            stringy += ".   .";
        }
        System.out.println(stringy + u.personType.name);
        for (Unit child: u.unlockedUnits) {
            traverseAndPrint(child, level + 1);
        }
    }

}
