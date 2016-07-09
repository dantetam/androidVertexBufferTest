package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public class Person extends Entity {

    public int age;
    public List<Tech> skills;
    public int actionPoints, maxActionPoints;

    public Person(Clan clan, String name) {
        super(clan);
        clan.people.add(this);
        this.name = name;
        skills = new ArrayList<>();
    }

}
