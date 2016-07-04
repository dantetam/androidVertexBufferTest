package io.github.dantetam.opstrykontest;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import io.github.dantetam.world.Tile;

public class LessonSevenGLSurfaceView extends GLSurfaceView
{
    private LessonSevenActivity mActivity;
	private LessonSevenRenderer mRenderer;
	
	// Offsets for touch events	 
    private float mPreviousX = -1;
    private float mPreviousY = -1;
    
    private float mDensity;

    private MousePicker mousePicker;

    //private Tile selectedTile = null;
        	
	public LessonSevenGLSurfaceView(Context context)
	{
		super(context);
	}
	
	public LessonSevenGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

    public void init(LessonSevenActivity activity, MousePicker mousePicker) {
        mActivity = activity;
        this.mousePicker = mousePicker;
    }

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event != null)
		{
            //System.out.println(actionToString(MotionEventCompat.getActionMasked(event)));
			float x = event.getX();
			float y = event.getY();
			
			if (event.getAction() == MotionEvent.ACTION_MOVE)
			{
				if (mRenderer != null)
				{
					float deltaX = (x - mPreviousX) / mDensity / 2f;
					float deltaY = (y - mPreviousY) / mDensity / 2f;
                    /*if (mPreviousX == -1 || mPreviousY == -1) {
                        deltaX = 0; deltaY = 0;
                    }*/
					
					//mRenderer.mDeltaX += deltaX;
					//mRenderer.mDeltaY += deltaY;
					mRenderer.camera.moveShift(-deltaX/10, 0, -deltaY/10);
					mRenderer.camera.pointShift(-deltaX/10, 0, -deltaY/10);

                    if (mousePicker != null) {
                        mousePicker.update(x, y);
                        /*Vector3f v = mousePicker.rayCastHit;
                        mousePicker.getTileClickedOn();*/
                    }
				}
			}	
			
			mPreviousX = x;
			mPreviousY = y;
			
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}		
	}

    public void update() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMenu();
            }
        });
    }

    private void updateMenu() {
        if (mousePicker.selectedNeedsUpdating()) {
            mousePicker.nextFrameSelectedNeedsUpdating = false;

            boolean selectedTileExists = mousePicker.getSelectedTile() != null;
            boolean selectedEntityExists = mousePicker.getSelectedEntity() != null;

            mActivity.findViewById(R.id.build_menu).setVisibility(selectedTileExists ? View.VISIBLE : View.INVISIBLE);

            Button selectedEntityMenu = (Button) mActivity.findViewById(R.id.selected_unit_menu);
            selectedEntityMenu.setVisibility(selectedEntityExists ? View.VISIBLE : View.INVISIBLE);
            if (selectedEntityExists) {
                selectedEntityMenu.setText(mousePicker.getSelectedEntity().name);
            }

            Button unitMenu = (Button) mActivity.findViewById(R.id.unit_menu);
            unitMenu.setVisibility(
                    selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
            );
            if (selectedTileExists) {
                unitMenu.setText("Units (" + mousePicker.getSelectedTile().occupants.size() + ")");
            } else if (selectedEntityExists) {
                unitMenu.setText("Units (" + mousePicker.getSelectedEntity().location().occupants.size() + ")");
            }
        }
    }

    // Hides superclass method.
	public void setRenderer(LessonSevenRenderer renderer, float density) 
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
	}
}
