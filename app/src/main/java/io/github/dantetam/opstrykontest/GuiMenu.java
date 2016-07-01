package io.github.dantetam.opstrykontest;

/**
 * Created by Dante on 7/1/2016.
 */
public class GuiMenu {

    public GuiFrame root;
    public LessonSevenRenderer activity;

    public GuiMenu(LessonSevenRenderer activity) {
        this.activity = activity;
    }

    public boolean within(float mouseX, float mouseY) {
        return within(root, mouseX, mouseY);
    }

    public boolean within(GuiFrame element, float mouseX, float mouseY) {
        float normX = mouseX / activity.getWidth();
        float normY = mouseY / activity.getHeight();

    }

}
