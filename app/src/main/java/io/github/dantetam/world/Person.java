package io.github.dantetam.world;

import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public class Person extends Entity {

    public int age;
    public List<Tech> skills;
    public int actionPoints, maxActionPoints;
    public int health, maxHealth;

    public Person(World world, Clan clan, String name) {
        super(world, clan);
        clan.people.add(this);
        this.name = name;
        skills = new ArrayList<>();
    }

    public void gameMove(Tile t) {
        Tile location = location();
        if (location != null) {
            if (location.dist(t) == 1) {
                if (actionPoints > 0) {
                    actionPoints--;
                    super.move(t);
                    return;
                }
            }
        }
        System.err.println("Invalid game move: ");
        System.err.println(location + "; " + location.dist(t) + "; " + actionPoints);
    }

}
