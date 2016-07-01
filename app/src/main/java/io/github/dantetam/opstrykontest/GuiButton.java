package io.github.dantetam.opstrykontest;

/**
 * Created by Dante on 7/1/2016.
 */
public class GuiButton extends GuiFrame {

    private Runnable effect;

    public GuiButton(GuiFrame parent, Runnable runnable) {
        super(parent);
        effect = runnable;
    }

    public void execute() {
        if (effect != null) {
            effect.run();
        }
    }

}
