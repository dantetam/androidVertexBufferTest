package io.github.dantetam.opstrykontest;

/**
 * Created by Dante on 6/30/2016.
 */
public class Vector4f {
    public float x,y,z,w;
    public Vector4f(float a, float b, float c, float d) {
        x = a; y = b; z = c; w = d;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector4f)) return false;
        Vector4f v = (Vector4f) obj;
        return x == v.x && y == v.y && z == v.z && w == v.w;
    }
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + (int)(x * 31);
        hash = hash * 31 + (int)(y * 31);
        hash = hash * 31 + (int)(z * 31);
        hash = hash * 31 + (int)(w * 31);
        return hash;
    }
    public String toString() {
        return x + " " + y + " " + z + " " + w;
    }
    public float dist(Vector4f v) {
        return (float)Math.sqrt(Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2) + Math.pow(z - v.z, 2) + Math.pow(w - v.w, 2));
    }
    public void scale(float f) {
        x *= f; y *= f; z *= f; w *= f;
    }
    public Vector4f scaled(float f) {
        Vector4f temp = new Vector4f(x, y, z, w);
        temp.scale(f);
        return temp;
    }
    public float magnitude() {
        return (float)Math.sqrt(x*x + y*y + z*z + w*w);
    }
    public Vector4f normalized() {
        float m = magnitude();
        return new Vector4f(x/m, y/m, z/m, w/m);
    }
}
