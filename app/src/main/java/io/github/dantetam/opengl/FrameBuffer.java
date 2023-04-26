package io.github.dantetam.opengl;

import android.opengl.GLES20;

import io.github.dantetam.opstrykontest.OpenGLRenderer;

public class FrameBuffer {

    private OpenGLRenderer mRenderer;

    public int width;
    public int height;

    public int fboId, fboTextureHandle;
    public int renderBufferId;

    /*
    protected static final int REFRACTION_WIDTH = 1280;
    private static final int REFRACTION_HEIGHT = 720;

    public int reflectionFrameBuffer;
    public int reflectionTexture;
    public int reflectionDepthBuffer;

    public int refractionFrameBuffer;
    public int refractionTexture;
    public int refractionDepthTexture;
    */

    public FrameBuffer(OpenGLRenderer mRenderer) {//call when loading the game
        this.mRenderer = mRenderer;
        width = mRenderer.getWidth();
        height = mRenderer.getHeight();
        initFrameBuffer();
    }

    public void startRender() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glViewport(0, 0, width, height);
    }

    public void stopRender() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void initFrameBuffer() {
        int[] temp = new int[1];
        //generate fbo id
        GLES20.glGenFramebuffers(1, temp, 0);
        fboId = temp[0];
        //generate texture
        GLES20.glGenTextures(1, temp, 0);
        fboTextureHandle = temp[0];
        //generate render buffer
        GLES20.glGenRenderbuffers(1, temp, 0);
        renderBufferId = temp[0];

        //Bind Frame buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        //Bind texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureHandle);
        //Define texture parameters
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //Bind render buffer and define buffer dimension
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        //Attach texture FBO color attachment
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureHandle, 0);
        //Attach render buffer to depth attachment
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId);
        //we are done, reset

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}

