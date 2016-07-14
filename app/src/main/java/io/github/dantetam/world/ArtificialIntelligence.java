package io.github.dantetam.world;

/**
 * Created by Dante on 7/13/2016.
 */
public class ArtificialIntelligence {

    public World world;

    public ArtificialIntelligence(World w) {
        world = w;
    }

    public void computerClanActions(Clan c) {
        for (Person person: c.people) {
            while (person.actionPoints > 0) {
                person.gameMove(world.randomNeighbor(person.location));
            }
        }
    }

}
