package io.github.dantetam.opengl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import io.github.dantetam.android.ShapeBuilder;

/**
 * Created by Dante on 6/20/2016.
 */
public class Solid extends RenderEntity {
    public int mCubeBufferIdx = -1;

    public String resourceName;
    public void setResourceName(String name) {
        resourceName = name;
        hasName = true;
    }

    public int strokeTextureHandle;

    public int generatedCubeFactor;

    public final float[] position = new float[3];
    public final float[] size = new float[3];
    public final float[] rotation = new float[4];
    public final float[] color = new float[4];

    public final int renderMode = GLES20.GL_TRIANGLES;

    /*public Solid() {
        this(textureHandle, defaultCubePos(), defaultCubeNormals(), defaultCubeTexture(), 1);
    }*/

    private static float[] defaultCubePos() {
        final float[] cubePositionData = new float[108];
        int cubePositionDataOffset = 0;

        final float minPosition = -1.0f;
        final float maxPosition = 1.0f;
        final float positionRange = maxPosition - minPosition;

        final float x = 0, y = 0, z = 0;

        final float x1 = minPosition + ((positionRange) * (x * 2));
        final float x2 = minPosition + ((positionRange) * ((x * 2) + 1));

        final float y1 = minPosition + ((positionRange) * (y * 2));
        final float y2 = minPosition + ((positionRange) * ((y * 2) + 1));

        final float z1 = minPosition + ((positionRange) * (z * 2));
        final float z2 = minPosition + ((positionRange) * ((z * 2) + 1));

        // Define points for a cube.
        // X, Y, Z
        final float[] p1p = { x1, y2, z2 };
        final float[] p2p = { x2, y2, z2 };
        final float[] p3p = { x1, y1, z2 };
        final float[] p4p = { x2, y1, z2 };
        final float[] p5p = { x1, y2, z1 };
        final float[] p6p = { x2, y2, z1 };
        final float[] p7p = { x1, y1, z1 };
        final float[] p8p = { x2, y1, z1 };

        final float[] thisCubePositionData = ShapeBuilder.generateCubeData(p1p, p2p, p3p, p4p, p5p, p6p, p7p, p8p,
                p1p.length);

        System.arraycopy(thisCubePositionData, 0, cubePositionData, cubePositionDataOffset, thisCubePositionData.length);
        cubePositionDataOffset += thisCubePositionData.length;
        return cubePositionData;
    }

    private static float[] defaultCubeNormals() {
        final float[] cubeNormalData =
                {
                        // Front face
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,

                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,

                        // Left face
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,

                        // Top face
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,

                        // Bottom face
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f
                };
        return cubeNormalData;
    }

    private static float[] defaultCubeTexture() {
        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        final float[] cubeTextureCoordinateData =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Right face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };
        return cubeTextureCoordinateData;
    }

    public Solid(Texture texture, float[] cubePositions, float[] cubeNormals, float[] cubeTextureCoordinates, int cubeFactor) {
        this(texture, 0, cubePositions, cubeNormals, cubeTextureCoordinates, cubeFactor);
    }

    public Solid(Texture texture, int textureAtlasIndex, float[] cubePositions, float[] cubeNormals, float[] cubeTextureCoordinates, int cubeFactor) {
        this.texture = texture;
        this.textureAtlasIndex = textureAtlasIndex;

        FloatBuffer cubeBuffer = getInterleavedBuffer(cubePositions, cubeNormals, cubeTextureCoordinates, cubeFactor);
        generatedCubeFactor = cubeFactor;

        numVerticesToRender = generatedCubeFactor * generatedCubeFactor * 36;

        // Second, copy these buffers into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeBuffer.capacity() * BYTES_PER_FLOAT, cubeBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mCubeBufferIdx = buffers[0];

        cubeBuffer.limit(0);
        cubeBuffer = null;
    }

    public void renderAll() {
        renderAll(GLES20.GL_TRIANGLES);
    }

    public int numVerticesToRender;
    public void renderAll(int mode) {
        final int stride = (POSITION_DATA_SIZE + NORMAL_DATA_SIZE + TEXTURE_COORDINATE_DATA_SIZE) * BYTES_PER_FLOAT;

        // Pass in the position information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeBufferIdx);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, stride, 0);

        // Pass in the normal information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeBufferIdx);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, stride, POSITION_DATA_SIZE * BYTES_PER_FLOAT);

        // Pass in the texture information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeBufferIdx);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false,
                stride, (POSITION_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the cubes.
        GLES20.glDrawArrays(mode, 0, numVerticesToRender); //36 vertices in a cube, of size 3 (GLES20.GL_TRIANGLES)
    }

    public void render(int blockIndex) {

        final int stride = (POSITION_DATA_SIZE + NORMAL_DATA_SIZE + TEXTURE_COORDINATE_DATA_SIZE) * BYTES_PER_FLOAT;

        // Pass in the position information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeBufferIdx);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, stride, 0);

        // Pass in the normal information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeBufferIdx);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, stride, POSITION_DATA_SIZE * BYTES_PER_FLOAT);

        // Pass in the texture information
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubeBufferIdx);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false,
                stride, (POSITION_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT);

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw the cubes.
        GLES20.glDrawArrays(GLES20.GL_LINES, blockIndex*36, 36); //36 vertices in a cube, of size 3 (GLES20.GL_TRIANGLES)
    }

    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { mCubeBufferIdx };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
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
