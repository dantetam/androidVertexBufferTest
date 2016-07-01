package io.github.dantetam.opstrykontest;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by Dante on 6/30/2016.
 * This is a parallel of the solid class for 2d GUIs and quads
 * This class should also attempt to render them as VBOs.
 * These quads can have neighboring and nested elements within.
 */
public class GuiTexture extends RenderEntity {

    public int mCubeBufferIdx = -1;

    public final float[] position = new float[2];
    public final float[] scale = new float[2];
    public final float[] rotation = new float[4];
    public final float[] color = new float[4];

    public final int renderMode = GLES20.GL_TRIANGLES;

    public GuiTexture(int textureHandle) {

    }

    public Solid(int textureHandle, float[] cubePositions, float[] cubeNormals, float[] cubeTextureCoordinates, int cubeFactor) {
        this.textureHandle = textureHandle;

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

    public void release() {
        // Delete buffers from OpenGL's memory
        final int[] buffersToDelete = new int[] { mCubeBufferIdx };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

    public void move(float a, float b) {
        position[0] = a; position[1] = b;
    }

    public void scale(float a, float b) {
        scale[0] = a; scale[1] = b;
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
