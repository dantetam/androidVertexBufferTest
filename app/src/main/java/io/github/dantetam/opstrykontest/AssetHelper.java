package io.github.dantetam.opstrykontest;

import android.content.Context;
import android.content.res.AssetManager;
import android.renderscript.ScriptGroup;

import java.io.BufferedReader;
import java.io.File;
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
            compress(truePath, "compressed_" + truePath);
            return ObjLoader.loadObjModelByVertex(assetManager.open(truePath));
        } catch (IOException e) {
            System.err.println("Could not find model named " + path + "; looked for " + truePath);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compress face data from the input into the output.
     * @param assetInputPath The input file in the assets folder (read-only)
     * @param internalOutputPath The output file name in internal data (read & write)
     */
    public void compress(String assetInputPath, String internalOutputPath) {
        File file = new File(context.getFilesDir(), internalOutputPath);

        InputStream inputStream;
        try {
            inputStream = assetManager.open(assetInputPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //Get the uncompressed data
        Object[] totalData = getTotalData(inputStream);
        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (Object obj: (List<Vector3f>) totalData[0]) vertices.add((Vector3f)obj);
        ArrayList<Vector3f> normals = new ArrayList<>();
        for (Object obj: (List<Vector3f>) totalData[1]) normals.add((Vector3f)obj);
        ArrayList<Vector2f> textures = new ArrayList<>();
        for (Object obj: (List<Vector2f>) totalData[2]) textures.add((Vector2f)obj);
        ArrayList<Face> faces = new ArrayList<>();
        for (Object obj: (List<Face>) totalData[3]) faces.add((Face)obj);

        //compressData(vertices, normals, textures, faces);

        try {
            FileOutputStream fos = context.openFileOutput(internalOutputPath, Context.MODE_PRIVATE);
            for (Vector3f v: vertices) {
                String stringy = "v " + v.toString();
                //System.out.println(stringy);
                fos.write(stringy.getBytes());
            }
            for (Vector2f v: textures) {
                String stringy = "vt " + v.toString();
                //System.out.println(stringy);
                fos.write(stringy.getBytes());
            }
            for (Vector3f v: normals) {
                String stringy = "vn " + v.toString();
                //System.out.println(stringy);
                fos.write(stringy.getBytes());
            }
            for (Face f: faces) {
                String stringy = "f " + f.toString();
                //System.out.println(stringy);
                fos.write(stringy.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param vertices,normals,textures,faces A set of non-unique data of an OBJ
     * @return The same data, now with the faces compressed.
     * We simply save the extra duplicates and garbage collect after building the buffer
     */
    private Object[] compressData(
            ArrayList<Vector3f> vertices,
            ArrayList<Vector3f> normals,
            ArrayList<Vector2f> textures,
            ArrayList<Face> faces)
    {
        HashMap<Vector3f, Integer> uniqueNormals = new HashMap<>();
        HashMap<Vector2f, Integer> uniqueTextures = new HashMap<>();
        HashMap<Integer, List<Integer>> redirectNormals = new HashMap<>();
        HashMap<Integer, List<Integer>> redirectTextures = new HashMap<>();
        for (int i = 0; i < normals.size(); i++) {
            Vector3f n = normals.get(i);
            if (!uniqueNormals.containsKey(n)) {
                uniqueNormals.put(n, i);
                redirectNormals.put(i, new ArrayList<Integer>());
            } else {
                int originalIndex = uniqueNormals.get(n);
                redirectNormals.get(originalIndex).add(i);
            }
        }
        for (int i = 0; i < textures.size(); i++) {
            Vector2f n = textures.get(i);
            if (!uniqueTextures.containsKey(n)) {
                uniqueTextures.put(n, i);
                redirectTextures.put(i, new ArrayList<Integer>());
            } else {
                int originalIndex = uniqueTextures.get(n);
                redirectTextures.get(originalIndex).add(i);
                //System.out.println(redirectTextures.get(originalIndex).size());
            }
        }
        for (Map.Entry<Integer, List<Integer>> en: redirectNormals.entrySet()) {
            Integer originalIndex = en.getKey();
            List<Integer> indicesToReplace = en.getValue();
            /*String stringy = "";
            for (int i = 0; i < indicesToReplace.size(); i++) {
                stringy += indicesToReplace.get(i) + " ";
            }
            System.out.println(stringy);*/
            for (Face face: faces) {
                if (indicesToReplace.contains((int)face.v1.z)) {
                    //System.out.println("Replace " + face.v1.z + " with " + originalIndex);
                    //System.out.println(vertices.get((int)face.v1.z).toString() + " " + vertices.get(originalIndex).toString());
                    face.v1.z = originalIndex;
                }
                if (indicesToReplace.contains((int)face.v2.z)) {
                    face.v2.z = originalIndex;
                }
                if (indicesToReplace.contains((int)face.v3.z)) {
                    face.v3.z = originalIndex;
                }
            }
        }
        for (Map.Entry<Integer, List<Integer>> en: redirectTextures.entrySet()) {
            Integer originalIndex = en.getKey();
            List<Integer> indicesToReplace = en.getValue();
            for (Face face: faces) {
                if (indicesToReplace.contains((int)face.v1.y)) {
                    face.v1.y = originalIndex;
                }
                if (indicesToReplace.contains((int)face.v2.y)) {
                    face.v2.y = originalIndex;
                }
                if (indicesToReplace.contains((int)face.v3.y)) {
                    face.v3.y = originalIndex;
                }
            }
        }
        //System.out.println(normals.size() + " *** " + textures.size());
        //TODO: Fix this. Removing duplicate entries shifts everything.
        /*for (Map.Entry<Integer, List<Integer>> en: redirectNormals.entrySet()) {
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
        }*/
        //System.out.println(normals.size() + " *** " + textures.size());
        /*ArrayList<Object>[] temp = (ArrayList<Object>[]) new Object[4];
        temp[0] = vertices; temp[1] = normals;
        temp[2] = textures; temp[3] = faces;*/
        return new Object[]{vertices, normals, textures, faces};
    }

    /**
     *
     * @param inputStream The source of the input, from context resources or internal storage
     * @return A collection of the OBJ data, in vertices, normals, tex coords, and faces.
     * Not parsed for uniqueness, see compressData(...)
     */
    public Object[] getTotalData(InputStream inputStream) {
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

        return compressData(vertices, normals, textures, faces);

        //return (ArrayList<Object>[]) new Object[]{vertices, normals, textures, faces};
    }

    /*
    This is a wrapper class for 3 Vector3f instances which represent a triangle in vtn data.
     */
    public static class Face {
        public Vector3f v1, v2, v3;
        public Face(float a, float b, float c, float d, float e, float f, float g, float h, float i) {
            v1 = new Vector3f(a,b,c);
            v2 = new Vector3f(d,e,f);
            v3 = new Vector3f(g,h,i);
        }
        public String toString() {
            return (int)v1.x + "/" + (int)v1.y + "/" + (int)v1.z + " " +
                    (int)v2.x + "/" + (int)v2.y + "/" + (int)v2.z + " " +
                    (int)v3.x + "/" + (int)v3.y + "/" + (int)v3.z;
        }
    }

}
