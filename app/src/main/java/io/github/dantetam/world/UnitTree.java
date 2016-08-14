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

    //A node class for this type of tree. Should not be exposed.
    //Info from this tree should be provided by encapsulated methods.
    public class Unit {
        public Person person;
        public List<Unit> unlockedUnits;
        public Unit() {
            unlockedUnits = new ArrayList<>();
        }
    }

    public List<Person> allowedUnits;

    public UnitTree(Clan clan) {
        this.clan = clan;
        clan.unitTree = this;
    }

    public void traverseAndPrint() {
        traverseAndPrint(root, 0);
    }
    private void traverseAndPrint(Unit u, int level) {
        String stringy = "";
        for (int i = 0; i < level; i++) {
            stringy += ".   .";
        }
        System.out.println(stringy + u.person.name);
        for (Unit child: u.unlockedUnits) {
            traverseAndPrint(child, level + 1);
        }
    }

}
