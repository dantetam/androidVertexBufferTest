package io.github.dantetam.opengl;

import android.opengl.Matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.opstrykontest.OpenGLRenderer;
import io.github.dantetam.utilmath.Vector2f;
import io.github.dantetam.utilmath.Vector3f;
import io.github.dantetam.opstrykontest.WorldSystem;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Tile;

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

    public Clan playerClan;

    private int width, height;

    private Tile selectedTile = null;
    private Entity selectedEntity = null;
    private String selectedAction = "";

    public Tile centerTile = null;

    public HashMap<Tile, Vector3f> storedTileVertices = null;

    private boolean selectedNeedsUpdating = false;
    public boolean selectedNeedsUpdating() {return selectedNeedsUpdating;}

    public boolean nextFrameSelectedNeedsUpdating = false;

    public boolean pathNeedsUpdating = false;
    public List<Tile> path;

    //public OpenGLRenderer renderer;

    public MousePicker(float[] p, Camera c, int w, int h) {
        //this.renderer = renderer;
        projMatrix = p;
        camera = c;

        viewMatrix = createViewMatrix(camera);
        width = w;
        height = h;
        //viewMatrix = Maths.createViewMatrix(camera);
    }

    public void updatePath(Tile a, Tile b) {
        path = WorldSystem.worldPathfinder.findPath(a,b);
        pathNeedsUpdating = true;
    }

    public void update(float mouseX, float mouseY, boolean combatMode)
    {
        viewMatrix = createViewMatrix(camera);
        currentRay = calculateMouseRay(mouseX, mouseY);

        rayCastHit = new Vector3f(
                camera.eyeX - camera.eyeY/currentRay.y*currentRay.x,
                0,
                camera.eyeZ - camera.eyeY/currentRay.y*currentRay.z
        );
        rayCastHit.scale(constant);

        if (OpenGLRenderer.debounceFrames > 0) {
            changeSelectedTile(null);
            return;
        }

        //boolean tilesNeedUpdating = false;
        Tile previousSelected = selectedTile;
        Entity previousEntity = selectedEntity;
        Tile newSelected = getTileClickedOn(rayCastHit);

        if (newSelected != null) {
            if (combatMode) {
                if (newSelected.occupants.size() <= 0) {
                    //changeSelectedUnit(null);
                }
                else {
                    changeSelectedUnit(newSelected.occupants.get(0));
                }
            }
            else {
                if (newSelected.occupants.size() <= 0) {
                    //changeSelectedUnit(null);
                    changeSelectedTile(newSelected);
                } else {
                    if (newSelected.occupants.get(0).clan.equals(playerClan))
                        changeSelectedUnit(newSelected.occupants.get(0));
                    else
                        changeSelectedTile(newSelected);
                }
            }
        }
        else {
            changeSelectedTile(null);
        }
        //selectedEntity = null;

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

        Vector3f centerRay = calculateMouseRay(width/2, height/2);
        Vector3f centerRayCastHit = new Vector3f(
                camera.eyeX - camera.eyeY/centerRay.y*centerRay.x,
                0,
                camera.eyeZ - camera.eyeY/centerRay.y*centerRay.z
        );
        Tile newCenterTile = getTileClickedOn(centerRayCastHit);
        if (newCenterTile != null)
            centerTile = getTileClickedOn(centerRayCastHit);

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
        return min <= 5 ? key : null;
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
    //Convert a 2D point on the xy plane into screen coordinates.
    public Vector2f calcScrPos(float posX, float posZ) {
        return calcScrPos(posX, 0, posZ);
    }
    //Convert a 3D point in world coordinates to screen coordinates.
    public Vector2f calcScrPos(float posX, float posY, float posZ) {
        //transform world to clipping coordinates
        float[] point = new float[4];
        Matrix.multiplyMV(point, 0, viewMatrix, 0, new float[]{posX, posY, posZ, 1}, 0);

        Matrix.multiplyMV(point, 0, projMatrix, 0, point, 0);

        point[0] /= point[3];
        point[1] /= point[3];
        point[2] /= point[3];

        int winX = (int) Math.round(((point[0] + 1.0) / 2.0) * width);
        //we calculate -point3D.getY() because the screen Y axis is
        //oriented top->down
        int winY = (int) Math.round(((1.0 - point[1]) / 2.0) * height);
        return new Vector2f(winX, winY);
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
