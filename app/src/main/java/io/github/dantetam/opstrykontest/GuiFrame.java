package io.github.dantetam.opstrykontest;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Dante on 7/1/2016.
 */
public class GuiFrame {

    public final float[] position = {-1, -1};
    public final float[] size = {-1, -1};
    public final float[] color = {-1, -1, -1, -1};
    public final float[] rotation = {-1, -1, -1, -1};

    public GuiFrame parent;
    public List<GuiFrame> children;

    public GuiFrame(GuiFrame parentFrame) {
        parent = parentFrame;
        children = new ArrayList<GuiFrame>();
    }

    public boolean withinRaw(float norm1, float norm2) {
        return norm1 >= position[0] && norm1 <= position[0] + size[0] &&
                norm2 >= position[1] && norm2 <= position[1] + size[1];
    }

    /*private List<GuiFrame> getAllParentsInOrder() {
        List<GuiFrame> frames = new ArrayList<>();
        GuiFrame pointer = parent;
        while (true) {
            if (pointer == null) return frames;
            frames.add(pointer);
            pointer = pointer.parent;
        }
    }*/

    public void move(float a, float b) {
        position[0] = a; position[1] = b;
        /*List<GuiFrame> parents = getAllParentsInOrder();
        if (parents.size() > 0) {
            for (int i = parents.size() - 1; i >= 0; i--) {
                GuiFrame parent = parents.get(i);
                position[0] =
            }
        }*/
    }
    public void scale(float a, float b) {
        size[0] = a; size[1] = b;
        /*for (GuiFrame parent: getAllParentsInOrder()) {
            size[0] *= parent.size[0];
            size[1] *= parent.size[1];
        }*/
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
