package io.github.dantetam.opstrykontest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.widget.PopupMenu;

import io.github.dantetam.world.Tile;
import io.github.dantetam.world.World;

/**
 * This class implements our custom renderer. Note that the GL10 parameter
 * passed in is unused for OpenGL ES 2.0 renderers -- the static class GLES20 is
 * used instead.
 *
 * This is the main entry point for the OpenGL program, and holds a WorldHandler,
 * which connects graphics and the abstract world representation. This class is directly responsible
 * for most rendering and render calls.
 */
public class LessonSevenRenderer implements GLSurfaceView.Renderer {
	/** Used for debug logs. */
	private static final String TAG = "LessonSevenRenderer";

	private final LessonSevenActivity mLessonSevenActivity;
	private final LessonSevenGLSurfaceView mGlSurfaceView;
	
	/**
	 * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
	 * of being located at the center of the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
	 * it positions things relative to our eye.
	 */
	private float[] mViewMatrix = new float[16];

	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	private float[] mProjectionMatrix = new float[16];
	
	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
	private float[] mMVPMatrix = new float[16];
	
	/** Store the accumulated rotation. */
	private final float[] mAccumulatedRotation = new float[16];
	
	/** Store the current rotation. */
	private final float[] mCurrentRotation = new float[16];
	
	/** A temporary matrix. */
	private float[] mTemporaryMatrix = new float[16];
	
	/** 
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	private float[] mLightModelMatrix = new float[16];

    public AssetManager assetManager;
    private AssetHelper assetHelper;

    /** Pass in data to shaders by OpenGL handles */
    private int mProgramHandle;
    private int mAndroidDataHandle;
    private int mWhiteTextureHandle;

    private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
	private int mLightPosHandle;
    private int mCameraPosHandle;
	private int mTextureUniformHandle;
	
	/** Additional info for cube generation. */
	private int mLastRequestedCubeFactor;
	private int mActualCubeFactor;

	/** Size of the position data in elements. */
	static final int POSITION_DATA_SIZE = 3;

	/** Size of the normal data in elements. */
	static final int NORMAL_DATA_SIZE = 3;

	/** Size of the texture coordinate data in elements. */
	static final int TEXTURE_COORDINATE_DATA_SIZE = 2;

	/** How many bytes per float. */
	static final int BYTES_PER_FLOAT = 4;

	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	// These still work without volatile, but refreshes are not guaranteed to happen.					
	public volatile float mDeltaX;					
	public volatile float mDeltaY;	
	
	/** Thread executor for generating cube data in the background. */
	private final ExecutorService mSingleThreadedExecutor = Executors.newSingleThreadExecutor();

	private Model mCubes;
    private Model improvements;
    private Lines mLines;
    private Solid testMarker;

	public Camera camera;
    public MousePicker mousePicker;

    public WorldHandler worldHandler;
    public static final int WORLD_LENGTH = 8;

    private int WIDTH = 0, HEIGHT = 0;

	/**
	 * Initialize the model data. Initialize other necessary classes.
	 */
	public LessonSevenRenderer(final LessonSevenActivity lessonSevenActivity, final GLSurfaceView glSurfaceView) {
		mLessonSevenActivity = lessonSevenActivity;
        assetManager = mLessonSevenActivity.getAssets();
        assetHelper = new AssetHelper(lessonSevenActivity, assetManager);
		mGlSurfaceView = (LessonSevenGLSurfaceView)glSurfaceView;
        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        camera = new Camera();
        camera.moveTo(5f, 10f, 10f);
        camera.pointTo(5f, 5f, 5f);

        mousePicker = new MousePicker(mProjectionMatrix, camera, getWidth(), getHeight());
        updatePerspectiveMatrix(getWidth(), getHeight());
        mGlSurfaceView.init(mLessonSevenActivity, mousePicker);

        worldHandler = new WorldHandler(mLessonSevenActivity, mousePicker, assetHelper, WORLD_LENGTH, WORLD_LENGTH);
        ColorTextureHelper.init(mLessonSevenActivity);

        //testMarker = worldHandler.testMarker(mAndroidDataHandle, mousePicker);
        //world = new World(WORLD_LENGTH, WORLD_LENGTH);
        //worldGenerator = new WorldGenerator(world);
        //worldGenerator.init();
	}

	private void generateCubes(int cubeFactor) {
		mSingleThreadedExecutor.submit(new GenDataRunnable(cubeFactor));
	}

    /*
    We use this class as a convenient way to impose lambda style statements
    on some objects, and we guarantee the existence of the method allowed(Object obj).
     */
    static abstract class Condition {
        abstract boolean allowed(Object obj);
        void init(int i) {}
        void init() {}
    }

	class GenDataRunnable implements Runnable {
		final int mRequestedCubeFactor;
		
		GenDataRunnable(int requestedCubeFactor) {
			mRequestedCubeFactor = requestedCubeFactor;
		}
		
		@Override
		public void run() {			
			try {
				final float[] cubePositionData = new float[108 * mRequestedCubeFactor * mRequestedCubeFactor];
				int cubePositionDataOffset = 0;

				final int segments = mRequestedCubeFactor + (mRequestedCubeFactor - 1);
                //final int segments = mRequestedCubeFactor - 1;

                final float minPosition = -mRequestedCubeFactor;
                final float maxPosition = mRequestedCubeFactor;
                final float positionRange = maxPosition - minPosition;

				for (int x = 0; x < mRequestedCubeFactor; x++) {
					//for (int y = 0; y < mRequestedCubeFactor; y++) {
						for (int z = 0; z < mRequestedCubeFactor; z++) {
                            Tile tile = worldHandler.world.getTile(x,z);

							final float x1 = minPosition + ((positionRange / segments) * (x * 2));
							final float x2 = minPosition + ((positionRange / segments) * ((x * 2) + 2));

							final float y1 = minPosition + ((positionRange / segments) * (1 * 2));
							final float y2 = minPosition + ((positionRange / segments) * ((1 * 2) + 1)) + tile.elevation;

							final float z1 = minPosition + ((positionRange / segments) * (z * 2));
							final float z2 = minPosition + ((positionRange / segments) * ((z * 2) + 2));

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
						}
					//}
				}
				
				// Run on the GL thread -- the same thread the other members of the renderer run in.
				mGlSurfaceView.queueEvent(new Runnable() {
					@Override
					public void run() {												
						if (mCubes != null) {
							mCubes.release();
							mCubes = null;
						}
						
						// Not supposed to manually call this, but Dalvik sometimes needs some additional prodding to clean up the heap.
						System.gc();
						
						try {
							mCubes = new Model();
							//for (int i = 0; i < mRequestedCubeFactor * mRequestedCubeFactor * mRequestedCubeFactor; i++) {
								//mCubes.add(new Solid(cubePositionData, cubeNormalData, cubeTextureCoordinateData, 1));
							//}
                            //mCubes.add(new Solid(cubePositionData, cubeNormalData, cubeTextureCoordinateData, mRequestedCubeFactor));
                            //mCubes.add(ObjLoader.loadSolid(mLessonSevenActivity, R.raw.teapot));

                            mActualCubeFactor = mRequestedCubeFactor;
						} catch (OutOfMemoryError err) {
							if (mCubes != null) {
								mCubes.release();
								mCubes = null;
							}
							
							// Not supposed to manually call this, but Dalvik sometimes needs some additional prodding to clean up the heap.
							System.gc();
							
							mLessonSevenActivity.runOnUiThread(new Runnable() {							
								@Override
								public void run() {
//									Toast.makeText(mLessonSevenActivity, "Out of memory; Dalvik takes a while to clean up the memory. Please try again.\nExternal bytes allocated=" + dalvik.system.VMRuntime.getRuntime().getExternalBytesAllocated(), Toast.LENGTH_LONG).show();								
								}
							});										
						}																	
					}				
				});
			} catch (OutOfMemoryError e) {
				// Not supposed to manually call this, but Dalvik sometimes needs some additional prodding to clean up the heap.
				System.gc();
				
				mLessonSevenActivity.runOnUiThread(new Runnable() {							
					@Override
					public void run() {
//						Toast.makeText(mLessonSevenActivity, "Out of memory; Dalvik takes a while to clean up the memory. Please try again.\nExternal bytes allocated=" + dalvik.system.VMRuntime.getRuntime().getExternalBytesAllocated(), Toast.LENGTH_LONG).show();								
					}
				});
			}			
		}
	}

	public void decreaseCubeCount() {
		if (mLastRequestedCubeFactor > 1) {
			generateCubes(--mLastRequestedCubeFactor);
		}
	}

	public void increaseCubeCount() {
		if (mLastRequestedCubeFactor < 16) {
			generateCubes(++mLastRequestedCubeFactor);
		}
	}	

    /*
    Initialize more data. Clean up the screen, move the camera, link shaders, load a few test textures.
     */
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
		//mCubes = new Model();

		mLastRequestedCubeFactor = mActualCubeFactor = WORLD_LENGTH;
		generateCubes(mActualCubeFactor);
		
		// Set the background clear color to black.
		//GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClearColor(0.0f, 140f/255f, 1.0f, 1.0f);

		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //GLES20.glEnable(GLES20.GL_STENCIL_TEST);

        // Position the eye in front of the origin.
		/*final float eyeX = 4.0f;
		final float eyeY = 4.0f;
		final float eyeZ = 4.0f;

		// We are looking toward the distance
		final float lookX = 2.0f;
		final float lookY = 2.0f;
		final float lookZ = 2.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;*/

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		mViewMatrix = camera.getViewMatrix();
		//Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		final String vertexShader = RawResourceReader.readTextFileFromRawResource(mLessonSevenActivity, R.raw.lesson_seven_vertex_shader);   		
 		final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mLessonSevenActivity, R.raw.lesson_seven_fragment_shader);
 				
		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		
		
		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
				new String[] {"a_Position", "a_Normal", "a_TexCoordinate"});
        
		// Load the texture
		mAndroidDataHandle = TextureHelper.loadTexture("usb_android", mLessonSevenActivity, R.drawable.usb_android);
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);			
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mAndroidDataHandle);		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);		
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mAndroidDataHandle);		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mWhiteTextureHandle = ColorTextureHelper.loadColor(255, 255, 255, 255);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWhiteTextureHandle);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWhiteTextureHandle);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mAccumulatedRotation, 0);        
	}	
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		updatePerspectiveMatrix(width, height);
	}

    public void updatePerspectiveMatrix(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 1000.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        mousePicker.projMatrix = mProjectionMatrix;
    }

    public int getWidth() {
        if (WIDTH == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            mLessonSevenActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            WIDTH = metrics.widthPixels;
        }
        return WIDTH;
    }
    public int getHeight() {
        if (HEIGHT == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            mLessonSevenActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            HEIGHT = metrics.heightPixels;
        }
        return HEIGHT;
    }

	/*
	This method is the grand render method. It follows these steps:
	Clear the screen
	Generate world if necessary
	Loop through all RenderEntity stored in the world representation
	For each VBO rendered,
	    Find the locations of its linked variables
	    Pass in matrices of data such as the MVP matrix
	    Bind the VBO's texture
	    Render the VBO through its special render method
	 */
	@Override
	public void onDrawFrame(GL10 glUnused) 
	{		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		//GLES20.glClearColor(0f/255f, 140f/255f, 255f/255f, 255f/255f);
		mViewMatrix = camera.getViewMatrix();

		//for (int i = 0; i < mCubes.parts.size(); i++) {
        if (mCubes == null || improvements == null || mCubes.parts.size() == 0) {
            mCubes = worldHandler.worldRep();
            improvements = worldHandler.tileImprovementRep();
            mLines = new Lines(mWhiteTextureHandle, worldHandler.tesselatedHexes[0], worldHandler.tesselatedHexes[1], worldHandler.tesselatedHexes[2]);
            mCubes.add(mLines);

            mousePicker.passInTileVertices(worldHandler.storedTileVertexPositions);
            return;
        }
        mCubes = worldHandler.worldRep();
        testMarker = worldHandler.selectedMarkerRep(R.drawable.usb_android);

        mGlSurfaceView.update();

        renderModel(mCubes);
        renderModel(improvements);

        renderSolid(testMarker);

        mousePicker.updateAfterFrame();

        //System.out.println(mousePicker.getSelectedTile() + " " + mousePicker.getSelectedEntity());
	}

    private void renderModel(Model model) {
        for (int i = 0; i < model.parts.size(); i++) {
            RenderEntity solid = model.parts.get(i);
            renderSolid(solid);
        }
    }

    private void renderSolid(RenderEntity solid) {
        if (solid == null) return;
        //RenderEntity solid = model.parts.get(i);
        //int x = (i / (mActualCubeFactor * mActualCubeFactor)) % mActualCubeFactor;
        //int y = (i / mActualCubeFactor) % mActualCubeFactor;
        //int z = i % mActualCubeFactor;
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mCameraPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_CameraPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        solid.mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        solid.mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        solid.mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Calculate position of the light. Push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -1.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        // Draw a cube.
        // Translate the cube into the screen.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, 0f);
        //Matrix.translateM(mModelMatrix, 0, x, y*2, z);

        // Set a matrix that contains the current rotation.
        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
        mDeltaX = 0.0f;
        mDeltaY = 0.0f;

        // Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

        // Rotate the cube taking the overall rotation into account.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);

        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.scaleM(mModelMatrix, 0, 1f, 1f, 1f);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        GLES20.glUniform3f(mCameraPosHandle, camera.eyeX, camera.eyeY, camera.eyeZ);

        // Pass in the texture information
        // Set the active texture unit to texture unit 0.
        //GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mAndroidDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, solid.textureHandle);
        //System.out.println(mAndroidDataHandle + " " + solid.textureHandle);

            /*GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, solid.textureHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, solid.textureHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);*/

        // Tell the texture uniform sampler to use this texture in the
        // shader by binding to texture unit 0.

        GLES20.glUniform1i(mTextureUniformHandle, 0);

        //---
        solid.renderAll(solid.renderMode);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

}
