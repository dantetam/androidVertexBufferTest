package io.github.dantetam.opstrykontest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
                    String buildingToBuild = action.substring(6);
                    Building newBuilding = new Building(previousSelectedEntity.world, previousSelectedEntity.clan, Building.BuildingType.fromString(buildingToBuild));

                    newBuilding.completionPercentage = 0;

                    Person personSelected = (Person) previousSelectedEntity;
                    if (!personSelected.location().equals(mousePicker.getSelectedTile())) {
                        personSelected.gameMovePath(mousePicker.getSelectedTile());
                    }
                    personSelected.actionsQueue.add(new PersonAction(Action.ActionType.BUILD, newBuilding));
                }
            }
            LessonSevenRenderer.debounceFrames = 10;
            mousePicker.changeSelectedTile(null);
            mousePicker.changeSelectedAction("");
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
        if (mousePicker.selectedNeedsUpdating()) {
            mousePicker.nextFrameSelectedNeedsUpdating = false;

            Tile selectedTile = mousePicker.getSelectedTile();
            Entity selectedEntity = mousePicker.getSelectedEntity();
            boolean selectedTileExists = selectedTile != null;
            boolean selectedEntityExists = selectedEntity != null;

            mActivity.findViewById(R.id.build_menu).setVisibility(selectedTileExists ? View.VISIBLE : View.INVISIBLE);

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
        final boolean selectedTileExists = mousePicker.getSelectedTile() != null;
        final boolean selectedEntityExists = mousePicker.getSelectedEntity() != null;

        LinkedHashMap<String, String> strings = new LinkedHashMap<>();

        String affiliation = "";
        if (selectedTileExists) {
            Tile selected = mousePicker.getSelectedTile();
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
            strings.put("text1", affiliation);
            strings.put("text2", Tile.Biome.nameFromInt(selected.biome.type) + ", " + Tile.Terrain.nameFromInt(selected.terrain.type));
            if (selected.improvement == null) {
                strings.put("text3", "Can build improvement");
            }
            else {
                strings.put("text3", selected.improvement.name);
            }
            if (selected.resources.size() > 0) {
                String stringy = "";
                for (Item resource: selected.resources) {
                    String s = resource.name;
                    if (!s.equals("No resource"))
                        stringy += s + " ";
                }
                if (!stringy.equals(""))
                    strings.put("text4", stringy);
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
            if (entity instanceof Person) {
                Person person = (Person) entity;
                stringy += " " + person.actionPoints + "/" + person.maxActionPoints + " AP";
            }
            strings.put("text1", stringy);
        }

        for (int id: UNIT_SELECTED_MENU_IDS) {
            Button bt = (Button) selectedStatMenu.findViewById(id);
            bt.setText("");
            bt.setVisibility(View.INVISIBLE);
            bt.setEnabled(false);
            bt.setOnClickListener(null);
        }
        int off = UNIT_SELECTED_MENU_IDS.length - strings.size();
        int i = 0;
        for (Map.Entry<String, String> en: strings.entrySet()) {
            Button bt = (Button) selectedStatMenu.findViewById(UNIT_SELECTED_MENU_IDS[i + off]);
            bt.setText(en.getValue());
            bt.setVisibility(View.VISIBLE);
            bt.setEnabled(true);
            final String finalAffiliation = affiliation;
            if (en.getKey().equals("text1")) {
                bt.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu unitSelectionMenu = new PopupMenu(mActivity, v);
                        MenuInflater inflater = unitSelectionMenu.getMenuInflater();
                        inflater.inflate(R.menu.resources_tooltip, unitSelectionMenu.getMenu());
                        String clanStringy = "";
                        if (selectedEntityExists) {
                            if (finalAffiliation.equals("Free")) {
                                clanStringy = "This land has no influence.";
                            }
                            else if (finalAffiliation.contains("(")) {
                                clanStringy = "The most influential clan.";
                            }
                            else {
                                clanStringy = "The current owner.";
                            }
                        }
                        MenuItem menuItem = unitSelectionMenu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, clanStringy);
                        unitSelectionMenu.show();
                    }
                });
            }
            else if (en.getKey().equals("text2")) {
                bt.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu unitSelectionMenu = new PopupMenu(mActivity, v);
                        MenuInflater inflater = unitSelectionMenu.getMenuInflater();
                        inflater.inflate(R.menu.resources_tooltip, unitSelectionMenu.getMenu());
                        MenuItem menuItem = unitSelectionMenu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, "The biome (climate) and terrain type (shape).");
                        unitSelectionMenu.show();
                    }
                });
            }
            else if (en.getKey().equals("text3")) {
                bt.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu unitSelectionMenu = new PopupMenu(mActivity, v);
                        MenuInflater inflater = unitSelectionMenu.getMenuInflater();
                        inflater.inflate(R.menu.resources_tooltip, unitSelectionMenu.getMenu());
                        MenuItem menuItem = unitSelectionMenu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, "Buildings to increase yields, craft, etc.");
                        unitSelectionMenu.show();
                    }
                });
            }
            else if (en.getKey().equals("text4")) {
                bt.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu unitSelectionMenu = new PopupMenu(mActivity, v);
                        MenuInflater inflater = unitSelectionMenu.getMenuInflater();
                        inflater.inflate(R.menu.resources_tooltip, unitSelectionMenu.getMenu());
                        MenuItem menuItem = unitSelectionMenu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, "Used for buildings and items to equip units.");
                        unitSelectionMenu.show();
                    }
                });
            }
            //selectedStatMenu.addView(bt);
            i++;
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
