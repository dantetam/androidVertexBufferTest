package io.github.dantetam.world.factory;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.world.action.Ability;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.Tech;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 7/15/2016.
 */
public class PersonFactory {

    public static void removePerson(Person person) {
        /*public Person(World world, Clan clan, String name) {
            super(world, clan);
            clan.people.add(this);
            this.name = name;
            skills = new ArrayList<>();
        }*/
        /*public Entity(World w, Clan c) {
            //locations = new ArrayList<Tile>();
            world = w;
            clan = c;
            inventory = new ArrayList<Item>();
            id = globalIdCounter;
            Entity.globalIdCounter++;
            actionsQueue = new ArrayList<>();
        }*/
        if (person.location() != null)
            person.location().occupants.remove(person);
        person.clan.people.remove(person);

        person.world = null;
        person.clan = null;
        person.setLocation(null);

        person.enabled = false;
    }

    public static Person newPerson(PersonType type, final World world, final Clan clan, double completionPercentage) {
        try {
            if (type == null) {
                throw new IllegalArgumentException("Improper type null given");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        int health, maxHealth;
        final int actionPoints, maxActionPoints;
        List<Tech> skills = new ArrayList<>();
        String name;

        //int atk = 0, def = 0, fire = 0, shock = 0, maneuver = 0;
        int exp = 0;

        int workCompleted = 15; //, workNeeded = 0;
        //int storageSpace = 3;

        Ability ability = null;

        /*switch (type) {
            case WARRIOR:
                health = 5;
                maxHealth = 5;
                actionPoints = 2;
                maxActionPoints = 2;
                //No special skills here
                name = "Warrior";
                break;
            case SETTLER:
                health = 3;
                maxHealth = 3;
                actionPoints = 3;
                maxActionPoints = 3;
                name = "Settler";
                ability = new Ability("Settle") {
                    @Override
                    public Action.ActionStatus gameExecuteAbility(Entity entity) {
                        Person person = (Person) entity;
                        if (actionPoints <= 0) {
                            return Action.ActionStatus.OUT_OF_ENERGY;
                        }
                        City city = BuildingFactory.newCity(world, clan, person.location(), world.getRing(person.location(), 1));
                        return Action.ActionStatus.CONSUME_UNIT;
                    }
                };
                break;
            default:
                System.err.println("Invalid person type: " + type);
                return null;
        }*/
        Person person = new Person(world, clan, type.name);
        person.health = type.health;
        person.maxHealth = type.maxH;
        person.actionPoints = type.actionPoints;
        person.maxActionPoints = type.maxP;
        person.skills = skills;

        person.atk = type.atk; person.def = type.def;
        person.fire = type.fire; person.shock = type.shock;
        person.maneuver = type.maneuver;
        person.exp = exp;

        person.workCompleted = workCompleted;
        //person.workNeeded = workNeeded;
        person.workNeeded = completionPercentage*workCompleted;

        //person.inventorySpace = storageSpace;

        person.specialAbility = ability;

        return person;
    }

}
