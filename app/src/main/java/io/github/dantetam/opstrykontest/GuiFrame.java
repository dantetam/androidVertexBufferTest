package io.github.dantetam.opstrykontest;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Dante on 7/1/2016.
 */
public class GuiFrame {

    public final float[] position = new float[2];
    public final float[] size = new float[2];
    public final float[] color = new float[4];
    public final float[] rotation = new float[4];

    public GuiFrame parent;
    public List<GuiFrame> children;

    public GuiFrame(GuiFrame parentFrame) {
        parent = parentFrame;
        children = new ArrayList<GuiFrame>();
    }

    public void move(float a, float b) {
        position[0] = a; position[1] = b;
    }
    public void scale(float a, float b) {
        size[0] = a; size[1] = b;
    }
    public void rotate(float angle, float a, float b, float c) {
        rotation[0] = angle; rotation[1] = a; rotation[2] = b; rotation[3] = c;
    }
    public void color(float[] t) {
        if (t.length == 3)
            color(t[0], t[1], t[2], 1.0f);
        else if (t.length == 4)
            color(t[0], t[1], t[2], t[3]);
        else
            throw new IllegalArgumentException("Color argument is not of correct length");
    }
    public void color(float a, float b, float c, float d) {
        color[0] = a; color[1] = b; color[2] = c; color[3] = d;
    }
    public float angle() {
        return rotation[0];
    }
    public void rotateAngle(float f) {
        rotation[0] = f;
    }

}
