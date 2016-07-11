package io.github.dantetam.opstrykontest;

import android.opengl.Matrix;

import java.util.HashMap;
import java.util.Map;

import io.github.dantetam.world.Entity;
import io.github.dantetam.world.Tile;

/**
 * Created by Dante on 6/30/2016.
 */

//Also courtesy of ThinMatrix.

public class MousePicker {

    private Vector3f currentRay;
    public Vector3f rayCastHit;
    public static float constant = 1.0f;

    public float[] projMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] transformMatrix = new float[16];
    public Camera camera;

    private int width, height;

    private Tile selectedTile = null;
    private Entity selectedEntity = null;
    private String selectedAction = "";

    public HashMap<Tile, Vector3f> storedTileVertices = null;

    private boolean selectedNeedsUpdating = false;
    public boolean selectedNeedsUpdating() {return selectedNeedsUpdating;}

    public boolean nextFrameSelectedNeedsUpdating = false;

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

        //boolean tilesNeedUpdating = false;
        Tile previousSelected = selectedTile;
        Entity previousEntity = selectedEntity;
        getTileClickedOn();
        if (!nextFrameSelectedNeedsUpdating) {
            if (previousSelected != null) {
                nextFrameSelectedNeedsUpdating = !previousSelected.equals(selectedTile);
            } else {
                nextFrameSelectedNeedsUpdating = selectedTile != null;
            }
        }
        if (!nextFrameSelectedNeedsUpdating) {
            if (previousEntity != null) {
                nextFrameSelectedNeedsUpdating = !previousEntity.equals(selectedEntity);
            } else {
                nextFrameSelectedNeedsUpdating = selectedEntity != null;
            }
        }

        if (selectedTile != null) {
            selectedEntity = null;
        }
        /*if (storedTileVertices != null) {
            getTileClickedOn(rayCastHit);
        }*/
    }

    public void changeSelectedAction(String stringy) {
        selectedAction = stringy;
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    public void changeSelectedTile(Tile t) {
        nextFrameSelectedNeedsUpdating = true;
        selectedTile = t;
        selectedEntity = null;
    }
    public Tile getSelectedTile() {
        return selectedTile;
    }

    public void changeSelectedUnit(Entity en) {
        nextFrameSelectedNeedsUpdating = true;
        selectedTile = null;
        selectedEntity = en;
    }
    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public Tile getTileClickedOn() {
        return getTileClickedOn(rayCastHit);
    }
    public Tile getTileClickedOn(Vector3f v) {
        if (storedTileVertices == null) {
            return null;
        }
        Tile key = null;
        float min = -1;
        for (Map.Entry<Tile, Vector3f> en: storedTileVertices.entrySet()) {
            float dist = en.getValue().dist(v);
            if (min == -1 || dist < min) {
                key = en.getKey();
                min = dist;
            }
        }
        //selectedTile = min <= 5 ? key : null;
        changeSelectedTile(min <= 5 ? key : null);
        selectedEntity = null;
        return key;
    }

    public void updateAfterFrame() {
        selectedNeedsUpdating = nextFrameSelectedNeedsUpdating;
    }

    private Vector3f calculateMouseRay(float mouseX, float mouseY)
    {
        //float mouseX = Mouse.getX(), mouseY = Mouse.getY();
        float normalX = 2f*mouseX/width - 1f;
        float normalY = 1f - 2f*mouseY/height; //OpenGL conventions
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

    public void passInTileVertices(HashMap<Tile, Vector3f> map) {
        storedTileVertices = map;
    }

}
