package io.github.dantetam.opstrykontest;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import io.github.dantetam.world.Building;
import io.github.dantetam.world.ClanFactory;
import io.github.dantetam.world.Tile;

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
    public AssetHelper assetHelper;

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

	private ListModel mCubes;
    private MapModel improvements;
    private Lines mLines;
    private Solid testMarker;
    private Solid selectedUnitMarker;
    private MapModel tilesUnits;
    private MapModel borderMarker;
    private ListModel tileYieldRep;

    private Solid testDuplicate;

	public Camera camera;
    public MousePicker mousePicker;

    public WorldHandler worldHandler;
    public WorldSystem worldSystem;
    public static final int WORLD_LENGTH = 10;

    public ChunkHelper chunkHelper;

    private int WIDTH = 0, HEIGHT = 0;

    public static int frames = 0;
    public static int debounceFrames = 0;
    public boolean buildingWorldFinished = false;
	/**
	 * Initialize the model data. Initialize other necessary classes.
	 */
	public LessonSevenRenderer(final LessonSevenActivity lessonSevenActivity, final GLSurfaceView glSurfaceView) {
        ClanFactory.init();

		mLessonSevenActivity = lessonSevenActivity;
        BitmapHelper.init(mLessonSevenActivity);

        assetManager = mLessonSevenActivity.getAssets();
        assetHelper = new AssetHelper(lessonSevenActivity, assetManager);
		mGlSurfaceView = (LessonSevenGLSurfaceView)glSurfaceView;
        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        FileParser.mActivity = mLessonSevenActivity;

        camera = new Camera();
        camera.moveTo(5f, 6f, 7.5f);
        camera.pointTo(5f, 1f, 6f);

        mousePicker = new MousePicker(mProjectionMatrix, camera, getWidth(), getHeight());
        updatePerspectiveMatrix(getWidth(), getHeight());

        chunkHelper = new ChunkHelper();

        worldHandler = new WorldHandler(mLessonSevenActivity, this, mousePicker, assetHelper, chunkHelper, WORLD_LENGTH, WORLD_LENGTH);

        chunkHelper.init(worldHandler.world);

        ColorTextureHelper.init(mLessonSevenActivity);

        worldSystem = new WorldSystem(worldHandler);

        mGlSurfaceView.init(mLessonSevenActivity, mousePicker, worldSystem.playerClan);

        //worldSystem.initClan(worldHandler.world.cl);
        //testMarker = worldHandler.testMarker(mAndroidDataHandle, mousePicker);
        //world = new World(WORLD_LENGTH, WORLD_LENGTH);
        //worldGenerator = new WorldGenerator(world);
        //worldGenerator.init();
	}

    /*
    Initialize more data. Clean up the screen, move the camera, link shaders, load a few test textures.
     */
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
		mLastRequestedCubeFactor = mActualCubeFactor = WORLD_LENGTH;
		
		// Set the background clear color to black.
		//GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClearColor(0.0f, 140f/255f, 1.0f, 1.0f);

		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //GLES20.glEnable(GLES20.GL_STENCIL_TEST);

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
        frames++;
        if (debounceFrames > 0) {
            debounceFrames--;
        }

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		//GLES20.glClearColor(0f/255f, 140f/255f, 255f/255f, 255f/255f);
		mViewMatrix = camera.getViewMatrix();

		//for (int i = 0; i < mCubes.parts.size(); i++) {
        if (mCubes == null || improvements == null || mCubes.parts.size() == 0) {
            mCubes = worldHandler.worldRep();
            tilesUnits = worldHandler.updateTileUnits();
            improvements = worldHandler.tileImprovementRep();
            mLines = new Lines(mWhiteTextureHandle, worldHandler.tesselatedHexes[0], worldHandler.tesselatedHexes[1], worldHandler.tesselatedHexes[2]);
            //mCubes.add(mLines);
            tileYieldRep = worldHandler.updateTileYieldRep();

            mousePicker.passInTileVertices(worldHandler.storedTileVertexPositions);

            float[][] objData = ObjLoader.loadObjModelByVertex(mLessonSevenActivity, R.raw.hexagonflat);
            testDuplicate = ObjLoader.loadSolid(TextureHelper.loadTexture("usb_android", mLessonSevenActivity, R.drawable.usb_android), null, objData);

            return;
        }
        mCubes = worldHandler.worldRep();
        tilesUnits = worldHandler.updateTileUnits();
        improvements = worldHandler.tileImprovementRep();
        testMarker = worldHandler.selectedMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255));
        selectedUnitMarker = worldHandler.selectedUnitMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255));
        borderMarker = worldHandler.tileTerritoryRep();
        ///highlights = worldHandler.tileHighlightRep();
        worldHandler.tileHighlightRep();
        worldHandler.totalWorldRepresentation();

        //TODO: Turn highlights into a combined VBO, like biome representation
        //TODO: Convert to IBOs next?

        mGlSurfaceView.update();

        if (!buildingWorldFinished) {
            buildingWorldFinished = true;
            mLessonSevenActivity.runOnUiThread(new Thread() {
                public void run() {
                    Animation anim = AnimationUtils.loadAnimation(mLessonSevenActivity, R.anim.splash_alpha);
                    anim.reset();
                    ImageView splashScreen = (ImageView) mLessonSevenActivity.findViewById(R.id.splash_screen_main);
                    splashScreen.clearAnimation();
                    splashScreen.startAnimation(anim);
                    //mLessonSevenActivity.findViewById(R.id.splash_screen_main).setVisibility(View.INVISIBLE);
                    try {
                        sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        splashScreen.setVisibility(View.INVISIBLE);
                    }
                }
            });
            getUserInterfaceReady();
            System.out.println("Done loading.");
        }

        renderModel(mCubes);
        renderModel(tilesUnits);
        renderModel(improvements);

        renderSolid(testMarker);
        renderSolid(selectedUnitMarker);
        renderModel(borderMarker);

        if (mousePicker.getSelectedTile() != null && mousePicker.getSelectedTile().improvement != null) {
            if (mousePicker.getSelectedTile().improvement.buildingType == Building.BuildingType.ENCAMPMENT) {
                tileYieldRep = worldHandler.updateTileYieldRep();
                renderModel(tileYieldRep);
            }
        }

        //renderSolidClones(testDuplicate);
        //renderModel(highlights);

        renderModel(worldHandler.tileHighlightOwnerStored);
        //renderModel(worldHandler.tileHighlightInfluenceStored);

        mousePicker.updateAfterFrame();

        //System.out.println(mousePicker.getSelectedTile() + " " + mousePicker.getSelectedEntity());
	}

    private void renderModel(BaseModel model) {
        for (RenderEntity renderEntity: model.parts()) {
            renderSolid(renderEntity);
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
        Matrix.translateM(mModelMatrix, 0, 0, 0, 0);
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

        // Tell the texture uniform sampler to use this texture in the
        // shader by binding to texture unit 0.

        GLES20.glUniform1i(mTextureUniformHandle, 0);

        //---
        solid.renderAll(solid.renderMode);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void getUserInterfaceReady() {
        mLessonSevenActivity.runOnUiThread(new Runnable() {
            public void run() {
                mLessonSevenActivity.findViewById(R.id.main_menu).setVisibility(View.VISIBLE);
                mLessonSevenActivity.findViewById(R.id.turn_menu).setVisibility(View.VISIBLE);
            }
        });
    }

}
