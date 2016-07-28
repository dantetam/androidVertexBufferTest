package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 7/15/2016.
 */
public class PersonFactory {

    public static Person newPerson(Person.PersonType type, World world, Clan clan, double completionPercentage) {
        int health, maxHealth;
        int actionPoints, maxActionPoints;
        List<Tech> skills = new ArrayList<>();
        String name;

        int workCompleted = 15; //, workNeeded = 0;
        int storageSpace = 3;

        switch (type) {
            case WARRIOR:
                health = 5;
                maxHealth = 5;
                actionPoints = 3;
                maxActionPoints = 3;
                //No special skills here
                name = "Warrior";
                break;
            default:
                System.err.println("Invalid person type: " + type);
                return null;
        }
        Person person = new Person(world, clan, name);
        person.health = health;
        person.maxHealth = maxHealth;
        person.actionPoints = actionPoints;
        person.maxActionPoints = maxActionPoints;
        person.skills = skills;

        person.workCompleted = workCompleted;
        //person.workNeeded = workNeeded;
        person.workNeeded = completionPercentage*workCompleted;

        person.storageSpace = storageSpace;

        return person;
    }

}
