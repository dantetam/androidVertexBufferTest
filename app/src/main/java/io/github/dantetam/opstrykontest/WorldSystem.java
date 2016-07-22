package io.github.dantetam.opstrykontest;

import io.github.dantetam.world.ArtificialIntelligence;
import io.github.dantetam.world.Building;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.Pathfinder;
import io.github.dantetam.world.Person;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 7/13/2016.
 */
public class WorldSystem {

    public World world;
    public ArtificialIntelligence artificialIntelligence;
    public static WorldPathfinder worldPathfinder;

    public int turnNumber = 0;

    public Clan playerClan;

    public WorldSystem(WorldHandler worldHandler) {
        world = worldHandler.world;
        initClan(world.getClans().get(0));
        artificialIntelligence = new ArtificialIntelligence(world);
        worldPathfinder = new WorldPathfinder(world);
    }

    public void initClan(Clan c) {
        playerClan = c;
    }

    public void turn() {
        for (Building building: playerClan.buildings) {
            building.executeQueue();
        }
        for (Person person: playerClan.people) {
            person.executeQueue();
        }
        for (Clan clan: world.getClans()) {
            if (!clan.equals(playerClan)) {
                artificialIntelligence.computerClanActions(clan);
            }
            for (Person person: clan.people) {
                person.executeQueue();
            }
        }

        for (Clan c: world.getClans()) {
            for (Person person: c.people) {
                person.actionPoints = person.maxActionPoints;
            }
        }
        turnNumber++;
        System.err.println("#turns passed: " + turnNumber);
    }

}
