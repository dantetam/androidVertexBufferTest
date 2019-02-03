package io.github.dantetam.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Dante on 6/20/2016.
 */
public abstract class RenderEntity {

    /** Size of the position data in elements. */
    static final int POSITION_DATA_SIZE = 3;

    /** Size of the normal data in elements. */
    static final int NORMAL_DATA_SIZE = 3;

    /** Size of the texture coordinate data in elements. */
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;

    /** How many bytes per float. */
    static final int BYTES_PER_FLOAT = 4;

    /** This will be used to pass in model position information. */
    public int mPositionHandle;

    /** This will be used to pass in model normal information. */
    public int mNormalHandle;

    /** This will be used to pass in model texture coordinate information. */
    public int mTextureCoordinateHandle;

    public Texture texture;

    public final float[] position = new float[3];
    public final float[] size = new float[3];
    public final float[] rotation = new float[4];
    public final float[] color = new float[4];

    public boolean hasName = false;
    public boolean alphaEnabled = false;

    public final int renderMode = GLES20.GL_TRIANGLES;

    public abstract void renderAll();
    public abstract void renderAll(int mode);
    //abstract void render(int indexBlock);
    public abstract void release();

    /**
     * Generated an interleaved VBO from the data, which contains n number of vertices, normal, and tex coords
     * We defined interleaved to be [vertex1, normal1, tex1, vertex2, normal2, tex2, ... vertexn, normaln, textn)]
     * @param cubePositions A set of vertices in triangles aligned in sets of POSITION_DATA_SIZE
     * @param cubeNormals A set of normals (not normalized) aligned in sets of NORMAL_DATA_SIZE
     * @param cubeTextureCoordinates A set of tex coords aligned in sets of TEXTURE_COORDINATE_DATA_SIZE
     * @param generatedCubeFactor A factor to multiply the data by. This will generate generatedCubeFactor^2 copies of the given data.
     * @return the new correctly sized, interleaved buffer which contains all the data.
     */
    FloatBuffer getInterleavedBuffer(float[] cubePositions, float[] cubeNormals, float[] cubeTextureCoordinates, int generatedCubeFactor) {
        final int cubeDataLength = cubePositions.length
                + (cubeNormals.length * generatedCubeFactor * generatedCubeFactor)
                + (cubeTextureCoordinates.length * generatedCubeFactor * generatedCubeFactor);

        final FloatBuffer cubeBuffer = ByteBuffer.allocateDirect(cubeDataLength * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < generatedCubeFactor * generatedCubeFactor; i++) {
            int cubePositionOffset = 0;
            int cubeNormalOffset = 0;
            int cubeTextureOffset = 0;
            for (int v = 0; v < cubePositions.length / POSITION_DATA_SIZE; v++) {
                cubeBuffer.put(cubePositions, cubePositionOffset, POSITION_DATA_SIZE);
                cubePositionOffset += POSITION_DATA_SIZE;
                cubeBuffer.put(cubeNormals, cubeNormalOffset, NORMAL_DATA_SIZE);
                cubeNormalOffset += NORMAL_DATA_SIZE;
                cubeBuffer.put(cubeTextureCoordinates, cubeTextureOffset, TEXTURE_COORDINATE_DATA_SIZE);
                cubeTextureOffset += TEXTURE_COORDINATE_DATA_SIZE;
            }
        }
        cubeBuffer.position(0);

        return cubeBuffer;
    }

    public void move(float a, float b, float c) {
        position[0] = a; position[1] = b; position[2] = c;
    }

    public void scale(float a, float b, float c) {
        size[0] = a; size[1] = b; size[2] = c;
    }

    public void rotate(float angle, float a, float b, float c) {
        rotation[0] = angle; rotation[1] = a; rotation[2] = b; rotation[3] = c;
    }

    public void color(float[] t) {
        if (t.length == 3)
            color(t[0], t[1], t[2], 1.0f);
        else if (t.length == 4)
            color(t[0], t[1], t[2], t[3]);
        else
            throw new IllegalArgumentException("Color argument is not of correct length");
    }
    public void color(float a, float b, float c, float d) {
        color[0] = a; color[1] = b; color[2] = c; color[3] = d;
    }

    public float angle() {
        return rotation[0];
    }
    public void rotateAngle(float f) {
        rotation[0] = f;
    }

}
