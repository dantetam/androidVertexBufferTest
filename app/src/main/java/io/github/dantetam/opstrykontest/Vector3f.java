package io.github.dantetam.opstrykontest;

/**
 * Created by Dante on 6/29/2016.
 * Some helper classes to conveniently wrap two and three floats
 */
public class Vector3f {
    public float x,y,z;
    public Vector3f(float a, float b, float c) {x = a; y = b; z = c;}
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3f)) return false;
        Vector3f v = (Vector3f) obj;
        return x == v.x && y == v.y && z == v.z;
    }
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + (int)(x * 100);
        hash = hash * 31 + (int)(y * 100);
        hash = hash * 31 + (int)(z * 100);
        return hash;
    }
    public String toString() {
        return x + " " + y + " " + z;
    }
    public void scale(float f) {
        x *= f; y *= f; z *= f;
    }
    public float magnitude() {
        return (float)Math.sqrt(x*x + y*y + z*z);
    }
    public Vector3f normalize() {
        float m = magnitude();
        return new Vector3f(x/m, y/m, z/m);
    }
}
