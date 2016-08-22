package io.github.dantetam.utilmath;

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
        hash = hash * 31 + (int)(x * 127);
        hash = hash * 31 + (int)(y * 127);
        return hash;
    }
    public float dist(Vector2f v) {
        return (float)Math.sqrt(Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2));
    }
    public String toString() {
        return x + " " + y;
    }
    public void scale(float f) {
        x *= f; y *= f;
    }
    public float magnitude() {
        return (float)Math.sqrt(x*x + y*y);
    }
    public Vector2f normalized() {
        float m = magnitude();
        return new Vector2f(x/m, y/m);
    }
}
