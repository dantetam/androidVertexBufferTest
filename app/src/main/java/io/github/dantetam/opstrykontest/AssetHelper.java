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

    public static final int POSITION_DATA_SIZE = 3;
    public static final int NORMAL_DATA_SIZE = 3;
    public static final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    public static final int BYTES_PER_FLOAT = 4;

    public AssetHelper(Context context, AssetManager assetManager) {
        this.context = context;
        this.assetManager = assetManager;
    }

    public float[][] loadVertexFromAssets(String path) {
        String truePath = path.toLowerCase().replace(' ', '_');
        float[][] data = compressIntoFloatData(truePath);
        /*try {
            return compressIntoFloatData(truePath);
            //compress(truePath, "compressed_" + truePath);
            //return ObjLoader.solidData.get()
            //return ObjLoader.loadObjModelByVertex(path, assetManager.open(truePath));
        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;*/
        return data;
    }

    /**
     * Compress face data from the input into the output.
     * @param assetInputPath The input file in the assets folder (read-only)
     */
    public float[][] compressIntoFloatData(String assetInputPath) {
        InputStream inputStream;
        try {
            inputStream = assetManager.open(assetInputPath);
        } catch (IOException e) {
            System.err.println("Could not find model in path (assets): " + assetInputPath);
            e.printStackTrace();
            return null;
        }

        //Get the compressed data
        //ObjResult totalData = getTotalData(inputStream);
        //return compressIntoFloatData(totalData);

        return ObjLoader.loadObjModelByVertex(assetInputPath, inputStream);
    }
    public static float[][] compressIntoFloatData(ObjResult totalData) {
        ArrayList<Vector3f> vertices = totalData.vertices;
        ArrayList<Vector3f> normals = totalData.normals;
        ArrayList<Vector2f> textures = totalData.textures;
        ArrayList<Face> faces = totalData.faces;

        //These help offset the data because of the OBJ's 1-index convention.
        vertices.add(new Vector3f(0,0,0));
        normals.add(new Vector3f(0,0,0));
        textures.add(new Vector2f(0,0));

        float[][] newData = new float[3][];
        newData[0] = new float[faces.size() * POSITION_DATA_SIZE * 3];
        newData[1] = new float[faces.size() * NORMAL_DATA_SIZE * 3];
        newData[2] = new float[faces.size() * TEXTURE_COORDINATE_DATA_SIZE * 3];
        for (int i = 0; i < faces.size(); i++) {
            Face f = faces.get(i);
            Vector3f v1 = f.v1, v2 = f.v2, v3 = f.v3; // v/t/n v/t/n v/t/n
            System.out.println(f.toString());
            //System.out.println(v1.x + " " + vertices.size());
            newData[0][POSITION_DATA_SIZE*3*i] = vertices.get((int)v1.x).x;
            newData[0][POSITION_DATA_SIZE*3*i+1] = vertices.get((int)v1.x).y;
            newData[0][POSITION_DATA_SIZE*3*i+2] = vertices.get((int)v1.x).z;
            newData[0][POSITION_DATA_SIZE*3*i+3] = vertices.get((int)v2.x).x;
            newData[0][POSITION_DATA_SIZE*3*i+4] = vertices.get((int)v2.x).y;
            newData[0][POSITION_DATA_SIZE*3*i+5] = vertices.get((int)v2.x).z;
            newData[0][POSITION_DATA_SIZE*3*i+6] = vertices.get((int)v3.x).x;
            newData[0][POSITION_DATA_SIZE*3*i+7] = vertices.get((int)v3.x).y;
            newData[0][POSITION_DATA_SIZE*3*i+8] = vertices.get((int)v3.x).z;

            newData[1][NORMAL_DATA_SIZE*3*i] = normals.get((int)v1.z).x;
            newData[1][NORMAL_DATA_SIZE*3*i+1] = normals.get((int)v1.z).y;
            newData[1][NORMAL_DATA_SIZE*3*i+2] = normals.get((int)v1.z).z;
            newData[1][NORMAL_DATA_SIZE*3*i+3] = normals.get((int)v2.z).x;
            newData[1][NORMAL_DATA_SIZE*3*i+4] = normals.get((int)v2.z).y;
            newData[1][NORMAL_DATA_SIZE*3*i+5] = normals.get((int)v2.z).z;
            newData[1][NORMAL_DATA_SIZE*3*i+6] = normals.get((int)v3.z).x;
            newData[1][NORMAL_DATA_SIZE*3*i+7] = normals.get((int)v3.z).y;
            newData[1][NORMAL_DATA_SIZE*3*i+8] = normals.get((int)v3.z).z;

            newData[2][TEXTURE_COORDINATE_DATA_SIZE*3*i] = textures.get((int)v1.y).x;
            newData[2][TEXTURE_COORDINATE_DATA_SIZE*3*i+1] = textures.get((int)v1.y).y;
            newData[2][TEXTURE_COORDINATE_DATA_SIZE*3*i+2] = textures.get((int)v2.y).x;
            newData[2][TEXTURE_COORDINATE_DATA_SIZE*3*i+3] = textures.get((int)v2.y).y;
            newData[2][TEXTURE_COORDINATE_DATA_SIZE*3*i+4] = textures.get((int)v3.y).x;
            newData[2][TEXTURE_COORDINATE_DATA_SIZE*3*i+5] = textures.get((int)v3.y).y;
        }
        return newData;
    }

    /*public void compress(String assetInputPath, String internalOutputPath) {
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
    }*/

    /**
     * @param vertices,normals,textures,faces A set of non-unique data of an OBJ
     * @return The same data, now with the faces compressed.
     * We simply save the extra duplicates and garbage collect after building the buffer
     */
    private static ObjResult compressData(
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
        return new ObjResult(vertices, normals, textures, faces);
    }

    /**
     *
     * @param inputStream The source of the input, from context resources or internal storage
     * @return A collection of the OBJ data, in vertices, normals, tex coords, and faces.
     * Not parsed for uniqueness, see compressData(...)
     */
    public static ObjResult getTotalData(InputStream inputStream) {
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader reader = new BufferedReader(inputStreamReader);

        return getTotalDataFromBuffered(reader);
        //return (ArrayList<Object>[]) new Object[]{vertices, normals, textures, faces};
    }
    public static ObjResult getTotalDataFromBuffered(BufferedReader reader) {
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
        return new ObjResult(vertices, normals, textures, faces);
        //return compressData(vertices, normals, textures, faces);
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

    /*
    This is a wrapper class for compression method results.
     */
    public static class ObjResult {
        public ArrayList<Vector3f> vertices;
        public ArrayList<Vector3f> normals;
        public ArrayList<Vector2f> textures;
        public ArrayList<Face> faces;
        public ObjResult(ArrayList<Vector3f> a, ArrayList<Vector3f> b, ArrayList<Vector2f> c, ArrayList<Face> d) {
            vertices = a;
            normals = b;
            textures = c;
            faces = d;
        }
    }

}
