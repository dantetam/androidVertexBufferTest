package io.github.dantetam.opstrykontest;

import android.opengl.Matrix;

/**
 * Created by Dante on 6/30/2016.
 */

//Also courtesy of ThinMatrix.

public class MousePicker {

    public Vector3f currentRay;
    public Vector3f rayCastHit;
    public static float constant = 1.0f;

    private final float[] projMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] transformMatrix = new float[16];
    public Camera camera;

    public MousePicker(Matrix p, Camera c) {
        camera = c;
        viewMatrix = createViewMatrix(c);
        //viewMatrix = Maths.createViewMatrix(camera);
    }

    public void update(float mouseX, float mouseY)
    {
        viewMatrix = Maths.createViewMatrix(camera);
        currentRay = calculateMouseRay(mouseX, mouseY);

        rayCastHit = new Vector3f(
                camera.position.x - camera.position.y/currentRay.y*currentRay.x,
                0,
                camera.position.z - camera.position.y/currentRay.y*currentRay.z
        );
        rayCastHit.scale(constant);
    }

    private Vector3f calculateMouseRay(float mouseX, float mouseY)
    {
        //float mouseX = Mouse.getX(), mouseY = Mouse.getY();
        float normalX = 2f*mouseX/DisplayManager.width - 1f;
        float normalY = 2f*mouseY/DisplayManager.height - 1f;
        Vector2f normalized = new Vector2f(normalX, normalY);
        Vector4f clip = new Vector4f(normalized.x, normalized.y, -1f, 1f);
        Vector4f eye = Matrix4f.transform(Matrix4f.invert(projMatrix, null), clip, null);
        eye.z = -1f; eye.w = 0f;
        Vector4f temp = Matrix4f.transform(Matrix4f.invert(viewMatrix, null), eye, null);
        Vector3f rayWorld = new Vector3f(temp.x, temp.y, temp.z);
        return (Vector3f)rayWorld.normalise();
    }

    //Reverse of the transformation in the previous function. Although that was the reverse,
    //so I guess this is the "normal" forward directed transformation?
    public Vector2f calculateScreenPos(float posX, float posZ)
    {
        //Create a new transformation matrix for the different position
        float[] transformMatrix = createTransformMatrix(new Vector3f(posX, 0, posZ), 0, 0, 0, 1);

        Vector4f worldPosition = Matrix4f.transform(transformMatrix, new Vector4f(posX, 0, posZ, 1.0f), null);

        //equivalent: glPosition = projectionMatrix * (viewMatrix * worldPosition);
        Vector4f glPosition = Matrix4f.transform(projMatrix, Matrix4f.transform(viewMatrix, worldPosition, null), null);
        Vector2f normalized = new Vector2f(glPosition.x, glPosition.y);

        //Reverse: y = 2x/width - 1, reverse's inverse: (width/2)(y + 1) = x
        return new Vector2f((normalized.x + 1f)*DisplayManager.width/2f,(normalized.y + 1f)*DisplayManager.height/2f);
    }

    /**
     * Matrix library methods
     */

    public static float[] createTransformMatrix(Vector3f translation, float rx, float ry, float rz, float scale)
    {
        float[] matrix = new float[16];

        Matrix.setIdentityM(matrix, 0);

        //Writing matrix twice accesses 'matrix' and rewrites the new 'transformed' matrix in 'matrix'
        Matrix.translateM(matrix, 0, translation.x, translation.y, translation.z);

        //Rotate it by angles around the axes
        Matrix.rotateM(matrix, 0, rx, 1, 0, 0);
        Matrix.rotateM(matrix, 0, ry, 0, 1, 0);
        Matrix.rotateM(matrix, 0, rz, 0, 0, 1);

        Matrix.scaleM(matrix, 0, scale, scale, scale);
        return matrix;
    }

    //For 2D GUIs
    /*public static float[] createTransformationMatrix(Vector2f translation, Vector2f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
        return matrix;
    }*/

    //Rotate in the opposite direction with respect to a camera's orientation
    public static float[] createViewMatrix(Camera camera)
    {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        //Rotate first and then translate; opposite of normal translation process
        Matrix4f.rotate((float)Math.toRadians(camera.pitch), new Vector3f(1,0,0), matrix, matrix);
        Matrix4f.rotate((float)Math.toRadians(camera.yaw), new Vector3f(0,1,0), matrix, matrix);
        Vector3f negative = new Vector3f(-camera.position.x, -camera.position.y, -camera.position.z);
        Matrix4f.translate(negative, matrix, matrix);
        return matrix;
    }

}
