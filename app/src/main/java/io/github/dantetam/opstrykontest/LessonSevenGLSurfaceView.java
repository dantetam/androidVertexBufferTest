package io.github.dantetam.opstrykontest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LessonSevenGLSurfaceView extends GLSurfaceView
        {
	private LessonSevenRenderer mRenderer;
	
	// Offsets for touch events	 
    private float mPreviousX;
    private float mPreviousY;
    
    private float mDensity;

    private MousePicker mousePicker;
        	
	public LessonSevenGLSurfaceView(Context context)
	{
		super(context);
	}
	
	public LessonSevenGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

    public void init(MousePicker mousePicker) {
        this.mousePicker = mousePicker;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event != null)
		{			
			float x = event.getX();
			float y = event.getY();
			
			if (event.getAction() == MotionEvent.ACTION_MOVE)
			{
				if (mRenderer != null)
				{
					float deltaX = (x - mPreviousX) / mDensity / 2f;
					float deltaY = (y - mPreviousY) / mDensity / 2f;
					
					//mRenderer.mDeltaX += deltaX;
					//mRenderer.mDeltaY += deltaY;
					mRenderer.camera.moveShift(-deltaX/10, 0, -deltaY/10);
					mRenderer.camera.pointShift(-deltaX/10, 0, -deltaY/10);

                    if (mousePicker != null) {
                        mousePicker.update(x, y);
                        Vector3f v = mousePicker.rayCastHit;
                        System.out.println(v.x + " " + v.y + " " + v.z);
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

    // Hides superclass method.
	public void setRenderer(LessonSevenRenderer renderer, float density) 
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
	}
}
