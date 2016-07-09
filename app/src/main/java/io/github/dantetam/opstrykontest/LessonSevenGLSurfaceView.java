package io.github.dantetam.opstrykontest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import io.github.dantetam.world.Clan;
import io.github.dantetam.world.Entity;
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
                //selectedEntityMenu.setText(mousePicker.getSelectedEntity().name);
                selectedEntityMenu.setText("Actions");
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

            PercentRelativeLayout selectedStatMenu = (PercentRelativeLayout) mActivity.findViewById(R.id.selected_stat_menu);
            selectedStatMenu.setVisibility(
                    selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
            );
            //if (selectedEntityExists || selectedTileExists) generateSelectionStatMenu(selectedStatMenu);
            generateSelectionStatMenu(selectedStatMenu);
        }
    }

    private static final int[] UNIT_SELECTED_MENU_IDS = {
            R.id.selected_stat_1,
            R.id.selected_stat_2,
            R.id.selected_stat_3,
            R.id.selected_stat_4
    };
    public void generateSelectionStatMenu(PercentRelativeLayout selectedStatMenu) {
        boolean selectedTileExists = mousePicker.getSelectedTile() != null;
        boolean selectedEntityExists = mousePicker.getSelectedEntity() != null;

        ArrayList<String> strings = new ArrayList<>();
        if (selectedTileExists) {
            Tile selected = mousePicker.getSelectedTile();
            String affiliation = "";
            Clan owner = selected.world.getTileOwner(selected), influence = selected.world.getTileInfluence(selected);
            if (owner != null) {
                affiliation = owner.name;
            }
            else if (influence != null) {
                affiliation = "(" + influence.name + ")";
            }
            else {
                affiliation = "Free";
            }
            strings.add(affiliation);
            strings.add(Tile.Biome.nameFromInt(selected.biome.type) + ", " + Tile.Terrain.nameFromInt(selected.terrain.type));
            if (selected.improvement == null) {
                strings.add("Can build improvement");
            }
            else {
                strings.add(selected.improvement.name);
            }
            if (selected.resources.size() > 0) {
                String stringy = "";
                for (Tile.Resource resource: selected.resources) {
                    String s = Tile.Resource.nameFromInt(resource.type);
                    if (!s.equals("No resource"))
                        stringy += s + " ";
                }
                if (!stringy.equals(""))
                    strings.add(stringy);
            }
        }
        else if (selectedEntityExists) {
            Entity entity = mousePicker.getSelectedEntity();
            String stringy = entity.name + " (";
            if (entity.clan != null) {
                stringy += entity.clan.name + ")";
            }
            else {
                stringy += "Free)";
            }
            strings.add(stringy);
        }

        for (int id: UNIT_SELECTED_MENU_IDS) {
            Button bt = (Button) selectedStatMenu.findViewById(id);
            bt.setText("");
            bt.setVisibility(View.INVISIBLE);
            bt.setEnabled(false);
        }
        int off = UNIT_SELECTED_MENU_IDS.length - strings.size();
        for (int i = 0; i < strings.size(); i++) {
            Button bt = (Button) selectedStatMenu.findViewById(UNIT_SELECTED_MENU_IDS[i + off]);
            bt.setText(strings.get(i));
            bt.setVisibility(View.VISIBLE);
            bt.setEnabled(true);
            //selectedStatMenu.addView(bt);
        }

                    /*
            <Button
                android:contentDescription="@string/new_world"
                android:id="@+id/test"
                android:layout_width="0dp"
                android:layout_height="0dp"
                android:text="@string/main_menu"
                app:layout_widthPercent="20%"
                app:layout_heightPercent="15%"
                app:layout_marginTopPercent="0%"
                app:layout_marginLeftPercent="0%" />
            */
    }

    // Hides superclass method.
	public void setRenderer(LessonSevenRenderer renderer, float density) 
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
	}
}
