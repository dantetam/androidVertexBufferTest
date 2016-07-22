package io.github.dantetam.opstrykontest;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/*
This class is from an old project inspired by ThinMatrix's OpenGL/LWJGL tutorials.
It has been modified to use Android contexts and resources as a source of raw OBJ files,
as opposed to filesystem resources in regular standard Java.

It provides convenience methods for creating solids (VBOs) from resources directly,
and contains two approaches: VBOs, and IBOs. TODO: Switch to index buffer objects.

It provides simple vector class which wrap a few floats at a time.

It parses through the format of a textured OBJ files containing vector normals.
For a review, each OBJ is structured with vertices 1..n,

v 1a 1b 1c
v 2a 2b 2c
...
v na nb nc

as well as vertex texture points also from 1..n, and normalized to 0..1, as such

vt 1a 1b
vt 2a 2b
...

and vertex normals from 1..n,

vn 1a 1b 1c
vn 2a 2b 2c

This data is labelled and then combined through face objects, which have the format

f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3

which represent polygons containing any number of vertices, which each vertex
has an associated texture uv coordinate as well as a normal.

All other data within the OBJ is ignored, for now.
 */
public class ObjLoader {

    /*
    Save data that's already been loaded under certain aliases.
    It is much more efficient to store and retrieve than to initialize hundreds of InputStreams and parsers.
     */
    public static HashMap<String, float[][]> solidData = new HashMap<>();

    private static Solid loadSolid(int textureHandle, String textureName) {
        if (textureName != null && solidData.containsKey(textureName)) {
            //System.out.println("Loading from memory: " + textureName);
            return loadSolid(textureHandle, textureName, solidData.get(textureName));
        }
        //System.out.println("Not loading from memory: " + textureName);
        return null;
    }

    private static Solid loadSolid(int textureHandle, String textureName, InputStream assetInputStream) {
        final InputStreamReader inputStreamReader = new InputStreamReader(assetInputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        float[][] data = AssetHelper.compressIntoFloatData(AssetHelper.getTotalData(assetInputStream));
        return loadSolid(textureHandle, textureName, data);
    }

    /**
     * Parse an OBJ to create a solid
     * @param textureHandle A textureHandle to bind to
     * @param textureName A name to save the solid data to if necessary
     * @param context An activity
     * @param resourceId A resource "handle" such as R.drawable.usb_android (presumably an OBJ)
     * @return A new Solid (a VBO) containing the data contained with the resource
     */
    public static Solid loadSolid(int textureHandle,
                                  String textureName,
                                  final Context context,
                                  final int resourceId)
    {
        if (textureName != null && solidData.containsKey(textureName)) {
            //System.out.println("Loading from memory too: " + textureName);
            return loadSolid(textureHandle, textureName, solidData.get(textureName));
        }
        //System.out.println("Not loading from memory too: " + textureName);
        float[][] data = loadObjModelByVertex(textureName, context, resourceId);
        //solidData.put(textureName, data);
        //Solid solid = new Solid(data[0], data[1], data[2], 1);
        /*for (int t = 0; t < 3; t++) {
            for (int i = 0; i < data[t].length; i++) {
                System.out.print(data[t][i] + " ");
                int f = t == 2 ? 2 : 3;
                if (i % f == f - 1) {
                    System.out.print("; ");
                }
            }
            System.out.println();
        }*/
        return loadSolid(textureHandle, textureName, data);
    }
    /*
    Do not save the texture under the name.
     */
    private static Solid loadSolid(int textureHandle,
                                  final Context context,
                                  final int resourceId)
    {
        return loadSolid(textureHandle, null, context, resourceId);
    }

    /**
     * Parse an OBJ to create a solid, but this time, manually from float[] data,
     * in VNT form.
     * @param textureHandle A textureHandle to bind to
     * @param data
     * @return A new Solid (a VBO) containing the data contained with the resource
     */
    public static Solid loadSolid(int textureHandle, String solidName, float[][] data) {
        Solid solid = new Solid(textureHandle, data[0], data[1], data[2], 1);
        if (solidName != null) {
            if (!solidData.containsKey(solidName)) {
                solidData.put(solidName, data);
            }
        }
        if (solidName != null) {
            System.out.println(solidName + "<<<");
            if (solidName.equals("quad") || solidName.contains("alpha")) {
                solid.setResourceName(solidName);
            }
        }
        solid.numVerticesToRender = data[0].length;
        return solid;
    }

    /**
     * Load float[] data, in VNTI form. Intended for IBOs.
     * This method loads from the specified resource all the data directly and does no other computation.
     * @param context An activity
     * @param resourceId A resource "handle" such as R.drawable.usb_android (presumably an OBJ)
     * @return an array of float[][] containing vertices, normal, texture coords, and indices respectively
     */
    public static float[][] loadObjModelByIndex(final Context context,
        final int resourceId)
    {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);

        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
        ArrayList<Vector2f> textures = new ArrayList<Vector2f>();
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        float[] verticesArray, normalsArray = null, textureArray = null;
        float[] indicesArray;
        try
        {
            while (true)
            {
                line = reader.readLine();
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
                    //All the v, vt, vn lines have been passed, end the loop
                    textureArray = new float[vertices.size()*2];
                    normalsArray = new float[vertices.size()*3];
                    break;
                }
            }

            while (line != null)
            {
                //Make sure a face line is being read
                if (!line.startsWith("f "))
                {
                    line = reader.readLine();
                    continue;
                }
                //A face is in the from f x/y/z a/b/c d/e/f
                //Split into these 4 sections
                //and then split the sections by slashes to get the numbers x, y, z, etc.
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                processVertex(vertex1,indices,textures,normals,textureArray,normalsArray);
                processVertex(vertex2,indices,textures,normals,textureArray,normalsArray);
                processVertex(vertex3,indices,textures,normals,textureArray,normalsArray);
                line = reader.readLine();
            }
            reader.close();

        } catch(Exception e) {e.printStackTrace();}

        verticesArray = new float[vertices.size()*3]; //Convert lists to array
        indicesArray = new float[indices.size()];
        int vertexPointer = 0;
        for (Vector3f vertex : vertices) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }
        return new float[][]{verticesArray, normalsArray, textureArray, indicesArray};
    }

    /**
     * Load float[] data, in VNT form. Intended for VBOs.
     * Note that the vertices are generated already from the faces.
     * This method loads from the specified resource all the vertices, normal, and tex coords.
     * It then compiles them in interleaved groups, where are each face
     * is a set of three triplets of a vertex, a texture and a normal.
     * @param textureName A name "handle" to save under
     * @param context An activity
     * @param resourceId A resource "handle" such as R.drawable.usb_android (presumably an OBJ)
     * @return an array of float[][] containing vertices, normal, and texture coords respectively
     */
    public static float[][] loadObjModelByVertex(String textureName, final Context context,
                                                final int resourceId)
    {
        final InputStream inputStream = context.getResources().openRawResource(resourceId);
        return loadObjModelByVertex(textureName, inputStream);
    }
    /*
    Same as the above method but in this case, we generate the key name
    from the resource's name (e.g. R.raw.usb_android -> "usb_android").
     */
    public static float[][] loadObjModelByVertex(final Context context, final int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(resourceId);
        return loadObjModelByVertex(context.getResources().getResourceEntryName(resourceId), inputStream);
    }
    public static float[][] loadObjModelByVertex(String solidName, final InputStream inputStream)
    {
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        float[][] data = null;
        if (solidData.containsKey(solidName)) {
            data = solidData.get(solidName);
            System.out.println("Loaded from memory obj model: " + solidName);
        }
        else {
            data = readFloatDataByVertex(bufferedReader);
            solidData.put(solidName, data);
            System.out.println("Not loaded, created obj model: " + solidName);
        }
        return data;
    }

    /*private static float[][] readFloatDataByVertex(BufferedReader reader) {
        return AssetHelper.compressIntoFloatData(AssetHelper.getTotalDataFromBuffered(reader));
    }*/

    /*
    This is a convenience method to stop code duplication.
    This reads from any correctly formatted BufferedReader, which is either from
    an InputStream from the activity's resources, or
    an InputStream from the activity's assets
    (the new second approach since Assets can be read by file names).
     */
    private static float[][] readFloatDataByVertex(BufferedReader reader) {
        String nextLine;
        final StringBuilder body = new StringBuilder();

        String line;
        ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
        ArrayList<Vector2f> textures = new ArrayList<Vector2f>();
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>();

        ArrayList<Vector3f> allVertices = new ArrayList<Vector3f>();
        ArrayList<Vector2f> allTextures = new ArrayList<Vector2f>();
        ArrayList<Vector3f> allNormals = new ArrayList<Vector3f>();

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

                    processVertex(vertex1, vertices, textures, normals, allVertices, allTextures, allNormals);
                    processVertex(vertex2, vertices, textures, normals, allVertices, allTextures, allNormals);
                    processVertex(vertex3, vertices, textures, normals, allVertices, allTextures, allNormals);
                }
            }

            /*while (line != null)
            {
                //Make sure a face line is being read
                if (!line.startsWith("f "))
                {
                    line = reader.readLine();
                    continue;
                }
                //A face is in the from f x/y/z a/b/c d/e/f
                //Split into these 4 sections
                //and then split the sections by slashes to get the numbers x, y, z, etc.
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                processVertex(vertex1, vertices, textures, normals, allVertices, allTextures, allNormals);
                processVertex(vertex2, vertices, textures, normals, allVertices, allTextures, allNormals);
                processVertex(vertex3, vertices, textures, normals, allVertices, allTextures, allNormals);

                line = reader.readLine();
            }*/
            reader.close();

        } catch (Exception e) {e.printStackTrace();}

        float[] verticesArray = new float[allVertices.size() * 3];
        float[] textureArray = new float[allVertices.size() * 2];
        float[] normalsArray = new float[allVertices.size() * 3];

        //System.out.println(">>> " + vertices.size() + " " + textures.size() + " " + normals.size());
        //System.out.println(">>> " + allVertices.size() + " " + allTextures.size() + " " + allNormals.size());

        for (int i = 0; i < allVertices.size(); i++) {
            Vector3f v = allVertices.get(i);
            verticesArray[i*3] = v.x;
            verticesArray[i*3 + 1] = v.y;
            verticesArray[i*3 + 2] = v.z;

            Vector2f t = allTextures.get(i);
            textureArray[i*2] = t.x;
            textureArray[i*2 + 1] = t.y;

            Vector3f n = allNormals.get(i);
            normalsArray[i*3] = n.x;
            normalsArray[i*3 + 1] = n.y;
            normalsArray[i*3 + 2] = n.z;
        }

        return new float[][]{verticesArray, normalsArray, textureArray};
    }

    /*
    This is a helper method that specifically takes in a string
    "v/vt/vn" split into its parts, and adds the data correctly into the given lists.
    Since the vertices follow the convention 1..n, we shift to follow 0..n-1
     */
    private static void processVertex(
            String[] vertex,
            ArrayList<Vector3f> vertices,
            ArrayList<Vector2f> textures,
            ArrayList<Vector3f> normals,
            ArrayList<Vector3f> allVertices,
            ArrayList<Vector2f> allTextures,
            ArrayList<Vector3f> allNormals) {
        allVertices.add(vertices.get(Integer.parseInt(vertex[0]) - 1));
        allTextures.add(textures.get(Integer.parseInt(vertex[1]) - 1));
        allNormals.add(normals.get(Integer.parseInt(vertex[2]) - 1));
    }

    /*
    Same as the previous methods but this one is intended for IBOs.
     */
    private static void processVertex(
            String[] vertexData,
            ArrayList<Integer> indices,
            ArrayList<Vector2f> textures,
            ArrayList<Vector3f> normals,
            float[] textureArray,
            float[] normalsArray)
    {
        int currentVertex = Integer.parseInt(vertexData[0]) - 1;
        indices.add(currentVertex);

        Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
        textureArray[currentVertex*2] = currentTex.x;
        textureArray[currentVertex*2 + 1] = 1 - currentTex.y; //Blender and OpenGL convention about xy coordinate system

        Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
        normalsArray[currentVertex*3] = currentNorm.x;
        normalsArray[currentVertex*3 + 1] = currentNorm.y;
        normalsArray[currentVertex*3 + 2] = currentNorm.z;
    }

}