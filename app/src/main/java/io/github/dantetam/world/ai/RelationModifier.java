package io.github.dantetam.world.ai;

/**
 * Created by Dante on 9/9/2016.
 */
public enum RelationModifier {
    ALLIED ("You are formally allied.", 50),

    AT_WAR ("You are at war!", -80),
    DENOUNCE ("You denounced them!", -30),

    WAS_AT_WAR ("You were at war.", -20);
    public String description; public int score;
    RelationModifier(String desc, int s) {
        description = desc;
        score = s;
    }
    public String toString() {
        return description;
    }
}
