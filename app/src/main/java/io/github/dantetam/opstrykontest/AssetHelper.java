package io.github.dantetam.opstrykontest;

import android.content.Context;
import android.content.res.AssetManager;
import android.renderscript.ScriptGroup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.Pathfinder;

/**
 * Created by Dante on 6/28/2016.
 * This provides an interface between classes which require access to AssetManager,
 * and the Android AssetManager class itself.
 *
 */
public class AssetHelper {

    private Context context;
    private AssetManager assetManager;

    public AssetHelper(Context context, AssetManager assetManager) {
        this.context = context;
        this.assetManager = assetManager;
    }

    public float[][] loadVertexFromAssets(String path) {
        String truePath = path.toLowerCase().replace(' ', '_');
        try {
            return ObjLoader.loadObjModelByVertex(assetManager.open(truePath));
        } catch (IOException e) {
            System.err.println("Could not find model named " + path + "; looked for " + truePath);
            e.printStackTrace();
        }
        return null;
    }

    public void compress(String assetInputPath, String internalOutputPath) {
        InputStream inputStream;
        try {
            inputStream = assetManager.open(assetInputPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //Get the uncompressed data
        ArrayList<Object>[] totalData = getTotalData(inputStream);
        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (Object obj: totalData[0]) vertices.add((Vector3f)obj);
        ArrayList<Vector3f> normals = new ArrayList<>();
        for (Object obj: totalData[1]) normals.add((Vector3f)obj);
        ArrayList<Vector2f> textures = new ArrayList<>();
        for (Object obj: totalData[2]) textures.add((Vector2f)obj);
        ArrayList<Face> faces = new ArrayList<>();
        for (Object obj: totalData[3]) faces.add((Face)obj);

        compressData(vertices, normals, textures, faces);

        try {
            FileOutputStream fos = context.openFileOutput(internalOutputPath, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Object>[] compressData(
            ArrayList<Vector3f> vertices,
            ArrayList<Vector3f> normals,
            ArrayList<Vector2f> textures,
            ArrayList<Face> faces
            ) {
        HashMap<Vector3f, Integer> uniqueNormals = new HashMap<>();
        HashMap<Vector2f, Integer> uniqueTextures = new HashMap<>();
        HashMap<Integer, List<Integer>> redirectNormals = new HashMap<>();
        HashMap<Integer, List<Integer>> redirectTextures = new HashMap<>();
        for (int i = 0; i < normals.size(); i++) {
            Vector3f n = (Vector3f) normals.get(i);
            if (!uniqueNormals.containsKey(n)) {
                uniqueNormals.put(n, i);
                redirectNormals.put(i, new ArrayList<Integer>());
            } else {
                int originalIndex = uniqueNormals.get(n);
                redirectNormals.get(originalIndex).add(i);
            }
        }
        for (int i = 0; i < textures.size(); i++) {
            Vector2f n = (Vector2f) textures.get(i);
            if (!uniqueTextures.containsKey(n)) {
                uniqueTextures.put(n, i);
                redirectTextures.put(i, new ArrayList<Integer>());
            } else {
                int originalIndex = uniqueTextures.get(n);
                redirectTextures.get(originalIndex).add(i);
            }
        }
        for (Map.Entry<Integer, List<Integer>> en: redirectNormals.entrySet()) {
            Integer originalIndex = en.getKey();
            List<Integer> indicesToReplace = en.getValue();
            for (Face face: faces) {
                if (indicesToReplace.contains(face.v1.z)) {
                    face.v1.z = originalIndex;
                }
                if (indicesToReplace.contains(face.v2.z)) {
                    face.v2.z = originalIndex;
                }
                if (indicesToReplace.contains(face.v3.z)) {
                    face.v3.z = originalIndex;
                }
            }
        }
        for (Map.Entry<Integer, List<Integer>> en: redirectTextures.entrySet()) {
            Integer originalIndex = en.getKey();
            List<Integer> indicesToReplace = en.getValue();
            for (Face face: faces) {
                if (indicesToReplace.contains(face.v1.y)) {
                    face.v1.y = originalIndex;
                }
                if (indicesToReplace.contains(face.v2.y)) {
                    face.v2.y = originalIndex;
                }
                if (indicesToReplace.contains(face.v3.y)) {
                    face.v3.y = originalIndex;
                }
            }
        }
        for (Map.Entry<Integer, List<Integer>> en: redirectNormals.entrySet()) {
            List<Integer> indicesToRemove = en.getValue();
            for (int i = normals.size() - 1; i >= 0; i--) {
                if (indicesToRemove.contains(i)) {
                    normals.remove(i);
                }
            }
        }
        for (Map.Entry<Integer, List<Integer>> en: redirectTextures.entrySet()) {
            List<Integer> indicesToRemove = en.getValue();
            for (int i = textures.size() - 1; i >= 0; i--) {
                if (indicesToRemove.contains(i)) {
                    textures.remove(i);
                }
            }
        }
        return (ArrayList<Object>[]) new Object[]{vertices, normals, textures, faces};
    }

    public ArrayList<Object>[] getTotalData(InputStream inputStream) {
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader reader = new BufferedReader(inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        String line;
        ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
        ArrayList<Vector2f> textures = new ArrayList<Vector2f>();
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
        ArrayList<Face> faces = new ArrayList<Face>();

        try
        {
            while (true)
            {
                line = reader.readLine();
                if (line == null) break;
                String[] currentLine = line.split(" ");
                if (line.startsWith("v ")) //vertex position
                {
                    Vector3f vertex = new Vector3f(
                            Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3])
                    );
                    vertices.add(vertex);
                }
                else if (line.startsWith("vt ")) //texture coordinate
                {
                    Vector2f texture = new Vector2f(
                            Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2])
                    );
                    textures.add(texture);
                }
                else if (line.startsWith("vn ")) //normal
                {
                    Vector3f vertex = new Vector3f(
                            Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3])
                    );
                    normals.add(vertex);
                }
                else if (line.startsWith("f ")) //face object
                {
                    String[] vertex1 = currentLine[1].split("/");
                    String[] vertex2 = currentLine[2].split("/");
                    String[] vertex3 = currentLine[3].split("/");
                    Face face = new Face(
                            Float.parseFloat(vertex1[0]),
                            Float.parseFloat(vertex1[1]),
                            Float.parseFloat(vertex1[2]),
                            Float.parseFloat(vertex2[0]),
                            Float.parseFloat(vertex2[1]),
                            Float.parseFloat(vertex2[2]),
                            Float.parseFloat(vertex3[0]),
                            Float.parseFloat(vertex3[1]),
                            Float.parseFloat(vertex3[2])
                    );
                    faces.add(face);
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (ArrayList<Object>[]) new Object[]{vertices, normals, textures, faces};
    }

    public static class Face {
        public Vector3f v1, v2, v3;
        public Face(float a, float b, float c, float d, float e, float f, float g, float h, float i) {
            v1 = new Vector3f(a,b,c);
            v2 = new Vector3f(d,e,f);
            v3 = new Vector3f(g,h,i);
        }
    }

}
