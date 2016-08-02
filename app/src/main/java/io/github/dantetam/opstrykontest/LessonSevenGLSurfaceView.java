package io.github.dantetam.opstrykontest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.dantetam.world.Action;
import io.github.dantetam.world.Building;
import io.github.dantetam.world.BuildingFactory;
import io.github.dantetam.world.BuildingType;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.Entity;
import io.github.dantetam.world.Item;
import io.github.dantetam.world.Person;
import io.github.dantetam.world.PersonAction;
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

    private Clan playerClan;

    //private Tile selectedTile = null;
        	
	public LessonSevenGLSurfaceView(Context context)
	{
		super(context);
	}
	
	public LessonSevenGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

    public void init(LessonSevenActivity activity, MousePicker mousePicker, Clan playerClan) {
        mActivity = activity;
        this.mousePicker = mousePicker;
        this.playerClan = playerClan;
        mRenderer = mActivity.mRenderer;
        //playerClan = mRenderer.worldSystem.playerClan;
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
                        Tile previousSelectedTile = mousePicker.getSelectedTile();
                        Entity previousSelectedEntity = mousePicker.getSelectedEntity();
                        mousePicker.update(x, y);
                        /*Vector3f v = mousePicker.rayCastHit;
                        mousePicker.getTileClickedOn();*/
                        executeSelectedAction(mousePicker, previousSelectedTile, previousSelectedEntity);
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

    public void executeSelectedAction(MousePicker mousePicker, Tile previousSelectedTile, Entity previousSelectedEntity) {
        String action = mousePicker.getSelectedAction();
        if (action == null || action.equals("")) {
            return; //Default, select the unit and only display its stats.
        }
        if (action.equals("Move")) {
            if (previousSelectedEntity == null) {
                System.err.println("Invalid 'Move' action, no selected entity before click");
                mousePicker.changeSelectedAction("");
                return;
            }
            if (mousePicker.getSelectedTile() == null) {

            }
            else {
                if (previousSelectedEntity instanceof Person) {
                    Person personSelected = (Person) previousSelectedEntity;
                    if (!personSelected.location().equals(mousePicker.getSelectedTile())) {
                        personSelected.gameMovePath(mousePicker.getSelectedTile());
                    }
                }
                //previousSelectedEntity.move(mousePicker.getSelectedTile());
            }
            LessonSevenRenderer.debounceFrames = 10;
            mousePicker.changeSelectedTile(null);
            mousePicker.changeSelectedAction("");
        }
        else if (action.startsWith("Build/")) {
            if (previousSelectedEntity == null) {
                System.err.println("Invalid 'Build' action, no selected entity before click");
                mousePicker.changeSelectedAction("");
                return;
            }
            if (mousePicker.getSelectedTile() == null) {

            }
            else {
                if (previousSelectedEntity instanceof Person) {
                    Person personSelected = (Person) previousSelectedEntity;
                    String buildingToBuild = action.substring(6);
                    Tile buildAt = mousePicker.getSelectedTile();
                    Building newBuilding = BuildingFactory.newBuilding(previousSelectedEntity.world, previousSelectedEntity.clan, BuildingType.fromString(buildingToBuild), buildAt, 0);

                    if (!buildAt.equals(personSelected.location())) {
                        personSelected.gameMovePath(buildAt);
                    }
                    personSelected.actionsQueue.add(new PersonAction(Action.ActionType.BUILD, newBuilding));
                    personSelected.executeQueue();
                }
            }
            LessonSevenRenderer.debounceFrames = 10;
            mousePicker.changeSelectedTile(null);
            mousePicker.changeSelectedAction("");
        }
        else if (action.equals("InitiateCombat")) {
            mousePicker.changeSelectedTile(null);
            mousePicker.changeSelectedAction("");
            mRenderer.setCombatMode(true);
        }
        else {
            System.err.println("Invalid action identifier: " + action);
            mousePicker.changeSelectedAction("");
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
        if (mRenderer.getCombatMode()) {
            Button quickSummaryMenu = (Button) mActivity.findViewById(R.id.quick_summary_view);
            quickSummaryMenu.setVisibility(View.INVISIBLE);

            mActivity.findViewById(R.id.build_menu).setVisibility(View.INVISIBLE);

            Button selectedEntityMenu = (Button) mActivity.findViewById(R.id.selected_unit_menu);
            selectedEntityMenu.setVisibility(View.INVISIBLE);

            Button unitMenu = (Button) mActivity.findViewById(R.id.unit_menu);
            unitMenu.setVisibility(View.INVISIBLE);

            Button queueMenu = (Button) mActivity.findViewById(R.id.queue_menu);
            queueMenu.setVisibility(View.INVISIBLE);

            PercentRelativeLayout selectedStatMenu = (PercentRelativeLayout) mActivity.findViewById(R.id.selected_stat_menu);
            selectedStatMenu.setVisibility(View.INVISIBLE);

            Button infoMenu = (Button) mActivity.findViewById(R.id.info_menu);
            infoMenu.setVisibility(View.INVISIBLE);

            mActivity.setContentView(R.layout.combat_view_menu);
        }
        else {
            mActivity.setContentView(R.layout.screen_view_menu);
            if (mousePicker.selectedNeedsUpdating()) {
                mousePicker.nextFrameSelectedNeedsUpdating = false;

                Tile selectedTile = mousePicker.getSelectedTile();
                Entity selectedEntity = mousePicker.getSelectedEntity();
                Building selectedImprovement = null;
                boolean selectedTileExists = selectedTile != null;
                boolean selectedEntityExists = selectedEntity != null;

                if (selectedTileExists) {
                    selectedImprovement = selectedTile.improvement;
                }
                boolean selectedImprovementExists = selectedImprovement != null;

                Button quickSummaryMenu = (Button) mActivity.findViewById(R.id.quick_summary_view);
                quickSummaryMenu.setVisibility(selectedEntityExists || selectedTileExists ? View.VISIBLE : View.INVISIBLE);
                String stringy = "";
                if (selectedTileExists) {
                    Clan owner = selectedTile.world.getTileOwner(selectedTile), influence = selectedTile.world.getTileInfluence(selectedTile);
                    String affiliation = "";
                    if (owner != null) {
                        affiliation = owner.name;
                    } else if (influence != null) {
                        affiliation = "(" + influence.name + ")";
                    } else {
                        affiliation = "Free";
                    }
                    stringy += affiliation;
                    stringy += " " + Tile.Biome.nameFromInt(selectedTile.biome.type) + ", " + Tile.Terrain.nameFromInt(selectedTile.terrain.type);
                    if (selectedTile.improvement != null) {
                        stringy += " " + selectedTile.improvement.name;
                    }
                } else if (selectedEntityExists) {
                    stringy += selectedEntity.name;
                    Clan owner = selectedEntity.clan;
                    String affiliation = "";
                    if (owner != null) {
                        affiliation = owner.name;
                    } else {
                        affiliation = "Free";
                    }
                    stringy += " " + affiliation;
                    if (selectedEntity instanceof Person) {
                        Person person = (Person) selectedEntity;
                        stringy += " " + person.actionPoints + "/" + person.maxActionPoints + " AP";
                    }
                }
                quickSummaryMenu.setText(stringy);

                mActivity.findViewById(R.id.build_menu).setVisibility(selectedImprovementExists && playerClan.equals(selectedTile.world.getTileOwner(selectedTile)) ? View.VISIBLE : View.INVISIBLE);

                Button selectedEntityMenu = (Button) mActivity.findViewById(R.id.selected_unit_menu);
                selectedEntityMenu.setVisibility(selectedEntityExists && playerClan.equals(selectedEntity.clan) ? View.VISIBLE : View.INVISIBLE);
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

                Button queueMenu = (Button) mActivity.findViewById(R.id.queue_menu);
                queueMenu.setVisibility(
                        selectedImprovementExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );
                if (selectedImprovementExists) {
                    queueMenu.setText("Queue (" + mousePicker.getSelectedTile().improvement.actionsQueue.size() + ")");
                } else if (selectedEntityExists) {
                    queueMenu.setText("Queue (" + mousePicker.getSelectedEntity().actionsQueue.size() + ")");
                }

                PercentRelativeLayout selectedStatMenu = (PercentRelativeLayout) mActivity.findViewById(R.id.selected_stat_menu);
                selectedStatMenu.setVisibility(
                        selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );
                //if (selectedEntityExists || selectedTileExists) generateSelectionStatMenu(selectedStatMenu);
                //generateSelectionStatMenu(selectedStatMenu);

                Button infoMenu = (Button) mActivity.findViewById(R.id.info_menu);
                infoMenu.setVisibility(
                        selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );
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
