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

    public float[] projMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] transformMatrix = new float[16];
    public Camera camera;

    private int width, height;

    public MousePicker(float[] p, Camera c, int w, int h) {
        projMatrix = p;
        camera = c;
        viewMatrix = createViewMatrix(camera);
        width = w;
        height = h;
        //viewMatrix = Maths.createViewMatrix(camera);
    }

    public void update(float mouseX, float mouseY)
    {
        viewMatrix = createViewMatrix(camera);
        currentRay = calculateMouseRay(mouseX, mouseY);

        rayCastHit = new Vector3f(
                camera.eyeX - camera.eyeY/currentRay.y*currentRay.x,
                0,
                camera.eyeZ - camera.eyeY/currentRay.y*currentRay.z
        );
        rayCastHit.scale(constant);
    }

    private Vector3f calculateMouseRay(float mouseX, float mouseY)
    {
        //float mouseX = Mouse.getX(), mouseY = Mouse.getY();
        float normalX = 2f*mouseX/width - 1f;
        float normalY = 2f*mouseY/height - 1f;
        Vector2f normalized = new Vector2f(normalX, normalY);
        float[] clip = {normalized.x, normalized.y, -1f, 1f};

        float[] inverseProj = new float[16];
        Matrix.invertM(inverseProj, 0, projMatrix, 0);
        float[] inverseView = new float[16];
        Matrix.invertM(inverseView, 0, viewMatrix, 0);

        /*for (int i = 0; i < projMatrix.length; i++) {
            System.out.print(projMatrix[i] + " ");
        }
        System.out.println();*/

        //System.out.println("<<<<< " + inverseView.toString());

        float[] eye = new float[4];
        Matrix.multiplyMV(eye, 0, inverseProj, 0, clip, 0);
        eye[2] = -1f; eye[3] = 0f;

        float[] temp = new float[4];
        Matrix.multiplyMV(temp, 0, inverseView, 0, eye, 0);
        Vector3f rayWorld = new Vector3f(temp[0], temp[1], temp[2]);

        return rayWorld.normalized();
    }

    //Reverse of the transformation in the previous function. Although that was the reverse,
    //so I guess this is the "normal" forward directed transformation?
    public Vector2f calculateScreenPos(float posX, float posZ)
    {
        //Create a new transformation matrix for the different position
        float[] transformMatrix = createTransformMatrix(new Vector3f(posX, 0, posZ), 0, 0, 0, 1);

        float[] worldPosition = new float[4];
        Matrix.multiplyMV(worldPosition, 0, transformMatrix, 0, new float[]{posX, 0, posZ, 1.0f}, 0);

        float[] viewTimesWorld = new float[4];
        Matrix.multiplyMV(viewTimesWorld, 0, viewMatrix, 0, worldPosition, 0);

        float[] glPosition = new float[4];

        //equivalent: glPosition = projectionMatrix * (viewMatrix * worldPosition);
        Matrix.multiplyMV(glPosition, 0, projMatrix, 0, viewTimesWorld, 0);
        Vector2f normalized = new Vector2f(glPosition[0], glPosition[1]);

        //Reverse: y = 2x/width - 1, reverse's inverse: (width/2)(y + 1) = x
        return new Vector2f((normalized.x + 1f)*width/2f,(normalized.y + 1f)*height/2f);
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
        float[] inverse = new float[16];
        Matrix.setIdentityM(matrix, 0);
        //Invert the camera view matrix
        Matrix.setLookAtM(matrix, 0, camera.eyeX, camera.eyeY, camera.eyeZ, camera.lookX, camera.lookY, camera.lookZ, 0, 1, 0);
        //Matrix.invertM(inverse, 0, matrix, 0);
        return matrix;
        //return inverse;
    }

    public float[] getMouseRayProjection(float touchX, float touchY, float windowWidth, float windowHeight, float[] view, float[] projection)
    {
        float normalizedX = 2f * touchX/windowWidth - 1f;
        float normalizedY = 1f - 2f*touchY/windowHeight;
        float normalizedZ = 1.0f;

        float[] rayNDC = new float[]{normalizedX, normalizedY, normalizedZ};
        float[] rayClip = new float[]{rayNDC[0], rayNDC[1], -1f, 1f};

        float[] inverseProjection = new float[16];
        Matrix.invertM(inverseProjection, 0, projection, 0);
        float[] rayEye = new float[4];
        Matrix.multiplyMV(rayEye, 0, inverseProjection, 0, rayClip, 0);

        //rayClip = new float[]{rayClip[0], rayClip[1], -1f, 0f};

        float[] inverseView = new float[16];
        Matrix.invertM(inverseView, 0, view, 0);
        float[] rayWorld4D = new float[4];
        Matrix.multiplyMV(rayWorld4D, 0, inverseView, 0, rayEye, 0);
        float[] rayWorld = new float[]{rayWorld4D[0], rayWorld4D[1], rayWorld4D[2]};

        return normalizeVector3(rayWorld);
    }

    public float[] normalizeVector3(float[] vector3)
    {
        float[] normalizedVector = new float[3];
        float magnitude = (float) Math.sqrt((vector3[0] * vector3[0]) + (vector3[1] * vector3[1]) + (vector3[2] * vector3[2]));
        normalizedVector[0] = vector3[0] / magnitude;
        normalizedVector[1] = vector3[1] / magnitude;
        normalizedVector[2] = vector3[2] / magnitude;
        return normalizedVector;
    }

}
