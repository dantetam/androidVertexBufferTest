package io.github.dantetam.world.ai;

/**
 * Created by Dante on 9/9/2016.
 */
public enum RelationModifier {
    ALLIED ("You are formally allied.", 50),

    OPEN_BORDERS ("You have open borders.", 10),

    AT_WAR ("You are at war!", -80),
    AGGRESSIVE_WAR ("You declared an aggressive war on them!", -20),
    DENOUNCE ("They denounced you!", -30),
    DENOUNCED ("You denounced them!", -30),

    COOPERATIVE ("They may wish to cooperate with you.", 30),
    JEALOUS ("They desire your lands and power.", -40),
    LACK_OF_TRUST ("They do not completely trust you.", -30),
    HOSTILE ("They are hostile with you!", -40),

    WAS_AT_WAR ("You were at war.", -10);
    public String description; public int score;
    RelationModifier(String desc, int s) {
        description = desc;
        score = s;
    }
    public String toString() {
        return description;
    }
}
