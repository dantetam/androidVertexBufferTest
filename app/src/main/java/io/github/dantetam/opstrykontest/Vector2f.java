package io.github.dantetam.opstrykontest;

/**
 * Created by Dante on 6/29/2016.
 * Some helper classes to conveniently wrap two and three floats
 */
public class Vector2f {
    public float x,y;
    public Vector2f(float a, float b) {x = a; y = b;}
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2f)) return false;
        Vector2f v = (Vector2f) obj;
        return x == v.x && y == v.y;
    }
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + (int)(x * 1000);
        hash = hash * 31 + (int)(y * 1000);
        return hash;
    }
    public String toString() {
        return x + " " + y;
    }
}
