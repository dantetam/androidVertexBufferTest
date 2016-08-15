package io.github.dantetam.opstrykontest;

import android.opengl.Matrix;

/**
 * Created by Dante on 6/12/2016.
 * Represents a camera with its own position, pointing towards another, oriented by the up vector
 */
public class Camera {

    //A view matrix that is recalculated if the data is changed
    private final float[] viewMatrix = new float[16];
    //3 vectors, which align with the Android matrix class
    public float eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ;

    public Camera() {
        upX = 0; upY = 1; upZ = 0;
    }

    public float[] getViewMatrix() {
        return viewMatrix;
    }

    private void setViewMatrix() {
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }



    /**
     * Convenient bulk setter methods. Force update on viewMatrix;
     */
    public void moveTo(float a, float b, float c) {
        eyeX = a; eyeY = b; eyeZ = c;
        setViewMatrix();
    }
    public void moveShift(float a, float b, float c) {
        eyeX += a; eyeY += b; eyeZ += c;
        setViewMatrix();
    }

    public void pointTo(float a, float b, float c) {
        lookX = a; lookY = b; lookZ = c;
        setViewMatrix();
    }
    public void pointShift(float a, float b, float c) {
        lookX += a; lookY += b; lookZ += c;
        setViewMatrix();
    }

    public void orient(float a, float b, float c) {
        upX = a; upY = b; upZ = c;
        setViewMatrix();
    }

}
