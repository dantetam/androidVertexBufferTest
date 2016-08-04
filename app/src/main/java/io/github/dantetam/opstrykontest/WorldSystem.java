package io.github.dantetam.opstrykontest;

import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.ArtificialIntelligence;
import io.github.dantetam.world.Building;
import io.github.dantetam.world.City;
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

    public List<RelationModifier>[][] relations;
    public HashMap<Clan, Integer> clanId;

    public enum RelationModifier {
        AT_WAR,
        AT_PEACE
    }

    public WorldSystem(WorldHandler worldHandler) {
        world = worldHandler.world;
        initClan(world.getClans().get(0));
        artificialIntelligence = new ArtificialIntelligence(world);
        worldPathfinder = new WorldPathfinder(world);
        int len = world.getClans().size();
        relations = (List<RelationModifier>[][]) new Object[len][len];
        clanId = new HashMap<>();
        for (int i = 0; i < len; i++) {
            clanId.put(world.getClans().get(i), i);
        }
    }

    public boolean atWar(Clan atk, Clan def) {
        int atkId = clanId.get(atk), defId = clanId.get(def);
        List<RelationModifier> relationModifierList = relations[atkId][defId];
        return relationModifierList != null && relationModifierList.contains(RelationModifier.AT_WAR);
    }

    public void initClan(Clan c) {
        playerClan = c;
    }

    public void turn() {
        processClan(playerClan);
        for (Clan clan: world.getClans()) {
            if (!clan.equals(playerClan)) {
                artificialIntelligence.computerClanActions(clan);
            }
            processClan(clan);
        }

        for (Clan c: world.getClans()) {
            for (Person person: c.people) {
                person.actionPoints = person.maxActionPoints;
            }
            for (Building building: c.buildings) {
                building.actionPoints = building.maxActionPoints;
                building.executeQueue();
            }
        }
        turnNumber++;
        System.err.println("#turns passed: " + turnNumber);
    }

    private void processClan(Clan clan) {
        for (Building building: clan.buildings) {
            building.executeQueue();
        }
        for (Person person: clan.people) {
            person.executeQueue();
        }
        int totalScience = 0, totalGold = 0;
        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            totalScience += city.lastYield[2];
            totalGold += city.lastYield[3];
        }
    }

}
