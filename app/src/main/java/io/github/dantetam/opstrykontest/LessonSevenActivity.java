package io.github.dantetam.opstrykontest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import java.util.List;
import java.util.Set;

import io.github.dantetam.world.Action;
import io.github.dantetam.world.Building;
import io.github.dantetam.world.BuildingAction;
import io.github.dantetam.world.BuildingFactory;
import io.github.dantetam.world.BuildingType;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.Entity;
import io.github.dantetam.world.Person;
import io.github.dantetam.world.PersonAction;
import io.github.dantetam.world.PersonFactory;
import io.github.dantetam.world.Tile;

public class LessonSevenActivity extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener
{
	/** Hold a reference to our GLSurfaceView */
	private LessonSevenGLSurfaceView mGLSurfaceView;
	public LessonSevenRenderer mRenderer;

    private GestureDetectorCompat mDetector;

    private LinearLayout orientationChanger;

    private PopupMenu mainMenu;
    private PopupMenu worldGenMenu;
    private PopupMenu unitSelectionMenu;
    private PopupMenu actionSelectionMenu;
    private PopupMenu buildSelectionMenu;
    private PopupMenu queueSelectionMenu;
    private PopupMenu moduleSelectionMenu;

    private Clan playerClan;

    /*public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }*/

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        //ActionBar actionBar = getActionBar();
        //actionBar.hide();

        /*final LessonSevenActivity mActivity = this;
        System.out.println("Start");
        runOnUiThread(new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Set splash");
                    setContentView(R.layout.splash_screen);
                    Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.splash_alpha);
                    anim.reset();
                    LinearLayout splashLayout = (LinearLayout) findViewById(R.id.splash_layout);
                    splashLayout.clearAnimation();
                    splashLayout.startAnimation(anim);
                }
            }
        });
        System.out.println("Start2");
        runOnUiThread(new Thread() {
            public void run() {
                try {
                    sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Set invisible");
                    ImageView imageView = (ImageView) findViewById(R.id.logo);
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        });

        System.out.println("Start3");*/

        setContentView(R.layout.screen_view_menu);

        findViewById(R.id.splash_screen_main).bringToFront();

		mGLSurfaceView = (LessonSevenGLSurfaceView) findViewById(R.id.gl_surface_view);

		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);

			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			// Set the renderer to our demo renderer, defined below.
			mRenderer = new LessonSevenRenderer(this, mGLSurfaceView);
			mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);
		} else {
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        registerForContextMenu(mGLSurfaceView);

        playerClan = mRenderer.worldSystem.playerClan;
    }

	@Override
	protected void onResume() {
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
		mGLSurfaceView.onResume();
	}

    @Override
	protected void onPause() {
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		mGLSurfaceView.onPause();
	}

    public final String DEBUG_TAG = "Debug (Gesture): ";

    private View newWorldMenu;
    public void onClickNewWorld(View v) {
        newWorldMenu = v;

        mainMenu = new PopupMenu(this, v);
        MenuInflater inflater = mainMenu.getMenuInflater();
        inflater.inflate(R.menu.main_menu, mainMenu.getMenu());
        mainMenu.show();
    }

    /*public void onClickNewWorldOptions(View v) {
        worldGenMenu = new ContextMenu(this, v);
        MenuInflater inflater = mainMenu.getMenuInflater();
        inflater.inflate(R.menu.main_menu, mainMenu.getMenu());
        worldGenMenu.show();
    }*/
    public boolean onClickNewWorldOptions(MenuItem item) {
        worldGenMenu = new PopupMenu(this, newWorldMenu);
        MenuInflater inflater = worldGenMenu.getMenuInflater();
        inflater.inflate(R.menu.new_world_gen_options, worldGenMenu.getMenu());
        worldGenMenu.show();
        return true;
    }

    public boolean onClickNewWorldOption1(MenuItem item) {
        return true;
    }

    public boolean onClickNewWorldOption2(MenuItem item) {
        return true;
    }

    public boolean onClickNewWorldOption3(MenuItem item) {
        return true;
    }

    public void onClickNextTurnMenu(View v) {
        PopupMenu tempMenu = new PopupMenu(this, v);
        MenuInflater inflater = tempMenu.getMenuInflater();
        inflater.inflate(R.menu.next_turn_menu, tempMenu.getMenu());
        tempMenu.show();
    }

    public boolean onClickNextTurnButton(MenuItem item) {
        mRenderer.worldSystem.turn();
        return true;
    }

    public void onClickActionsMenu(View v) {
        actionSelectionMenu = new PopupMenu(this, v);
        MenuInflater inflater = actionSelectionMenu.getMenuInflater();
        inflater.inflate(R.menu.action_selection_menu, actionSelectionMenu.getMenu());
        onCreateActionSelectionMenu(v, actionSelectionMenu.getMenu());
        actionSelectionMenu.show();
    }

    public void onClickUnitMenu(View v) {
        unitSelectionMenu = new PopupMenu(this, v);
        MenuInflater inflater = unitSelectionMenu.getMenuInflater();
        inflater.inflate(R.menu.unit_selection_menu, unitSelectionMenu.getMenu());
        onCreateUnitSelectionMenu(unitSelectionMenu.getMenu());
        unitSelectionMenu.show();
    }

    public void onClickQueueMenu(View v) {
        queueSelectionMenu = new PopupMenu(this, v);
        MenuInflater inflater = queueSelectionMenu.getMenuInflater();
        inflater.inflate(R.menu.queue_selection_menu, queueSelectionMenu.getMenu());
        onCreateQueueSelectionMenu(queueSelectionMenu.getMenu());
        queueSelectionMenu.show();
    }

    public boolean onCreateUnitSelectionMenu(Menu menu) {
        Tile selected = mRenderer.mousePicker.getSelectedTile();
        if (selected == null) {
            selected = mRenderer.mousePicker.getSelectedEntity().location();
        }
        if (selected != null) {
            if (selected.occupants.size() == 0) {
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "No units to select");
            }
            else {
                for (int i = 0; i < selected.occupants.size(); i++) {
                    final Entity en = selected.occupants.get(i);
                    String extra = playerClan.equals(en.clan) ? " " + en.actionPoints + "/" + en.maxActionPoints + " AP" : "";
                    MenuItem menuItem = menu.add(Menu.NONE, 1, Menu.NONE, en.name + " (" + en.clan.name + ")" + extra);
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            mRenderer.mousePicker.changeSelectedTile(null);
                            mRenderer.mousePicker.changeSelectedUnit(en);
                            //System.out.println(mRenderer.mousePicker.getSelectedEntity());
                            return false;
                        }
                    });
                }
            }
        }
        return true;
    }

    public void onClickModuleMenu(View v) {
        moduleSelectionMenu = new PopupMenu(this, v);
        MenuInflater inflater = moduleSelectionMenu.getMenuInflater();
        inflater.inflate(R.menu.build_module_menu, moduleSelectionMenu.getMenu());
        onCreateBuildModuleMenu(moduleSelectionMenu.getMenu());
        moduleSelectionMenu.show();
    }

    public boolean onCreateBuildModuleMenu(Menu menu) {
        final Tile selected = mRenderer.mousePicker.getSelectedTile();
        final Building selectedImprovement = selected != null ? selected.improvement : null;

        if (selectedImprovement != null) {
            if (selectedImprovement.modules.length == 0) {
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Cannot add modules.");
            }
            else {
                for (int i = 0; i < selectedImprovement.modules.length; i++) {
                    Building module = selectedImprovement.modules[i];
                    String stringy;
                    if (module == null) {
                        stringy = "Build improvement";
                    }
                    else {
                        stringy = module.name;
                    }
                    SubMenu moduleSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, i, stringy);

                    if (module == null) {
                        List<BuildingType> allowedBuildings = selectedImprovement.clan.techTree.allowedBuildingsAndModules.get(selectedImprovement.buildingType); //Well, that's all she wrote
                        for (final BuildingType buildingType: allowedBuildings) {
                            MenuItem menuItem = moduleSubMenu.add(Menu.NONE, 1, Menu.NONE, buildingType.name);
                            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    Building newBuilding = BuildingFactory.newModule(selectedImprovement.world, selectedImprovement.clan, buildingType, selected, 0, selectedImprovement);
                                    newBuilding.actionsQueue.clear();
                                    newBuilding.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, newBuilding));
                                    return false;
                                }
                            });
                        }
                    }
                }
                SubMenu unitSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Build unit");
                Set<Person> allowedPeople = selectedImprovement.clan.techTree.allowedUnits.keySet();
                for (final Person person: allowedPeople) {
                    MenuItem menuItem = unitSubMenu.add(Menu.NONE, 1, Menu.NONE, person.name);
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            //Building newBuilding = BuildingFactory.newModule(selectedImprovement.world, selectedImprovement.clan, buildingType, selected, 0, selectedImprovement);
                            //newBuilding.actionsQueue.clear();
                            //newBuilding.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, newBuilding));
                            Person newPerson = PersonFactory.newPerson(person.personType, selectedImprovement.world, selectedImprovement.clan);
                            selectedImprovement.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_PERSON, newPerson));
                            return false;
                        }
                    });
                }
            }
        }
        return true;
    }


    public boolean onCreateBuildSelectionMenu(Menu menu) {
        MenuItem menuItem = menu.add(Menu.NONE, 1, Menu.NONE, "Build Option 1");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                mRenderer.mousePicker.changeSelectedAction("Build/Encampment");
                //System.out.println(mRenderer.mousePicker.getSelectedEntity());
                return false;
            }
        });
        return true;
    }

    public boolean onCreateQueueSelectionMenu(Menu menu) {
        final Entity selectedEntity = mRenderer.mousePicker.getSelectedEntity();
        if (selectedEntity != null) {
            if (selectedEntity.actionsQueue.size() == 0) {
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Nothing in queue");
            }
            else {
                for (int i = 0; i < selectedEntity.actionsQueue.size(); i++) {
                    Action action = selectedEntity.actionsQueue.get(i);
                    MenuItem menuItem = menu.add(Menu.NONE, 1, Menu.NONE, action.toString());
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public int indexCondition = 0;
                        public boolean onMenuItemClick(MenuItem item) {
                            selectedEntity.actionsQueue.remove(indexCondition);
                            //System.out.println(mRenderer.mousePicker.getSelectedEntity());
                            return false;
                        }

                        public boolean equals(Object o) {
                            if (o instanceof Integer) {
                                indexCondition = (Integer) o;
                            }
                            return super.equals(o);
                        }
                    });
                    menuItem.equals(new Integer(i));
                }
            }
        }
        return true;
    }

    public void onClickUnitSelectedButton(View v) {
        unitSelectionMenu = new PopupMenu(this, v);
        MenuInflater inflater = unitSelectionMenu.getMenuInflater();
        inflater.inflate(R.menu.unit_selection_menu, unitSelectionMenu.getMenu());
        onCreateUnitSelectionMenu(unitSelectionMenu.getMenu());
        unitSelectionMenu.show();
    }

    public boolean onCreateActionSelectionMenu(final View chainView, Menu menu) {
        Entity entity = mRenderer.mousePicker.getSelectedEntity();
        final LessonSevenActivity mActivity = this;
        if (entity != null) {
            MenuItem menuItem = menu.add(Menu.NONE, 1, Menu.NONE, "Move");
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    mRenderer.mousePicker.changeSelectedAction("Move");
                    Button button = (Button) chainView;
                    button.setText("Move Unit");
                    return false;
                }
            });

            MenuItem menuItem2 = menu.add(Menu.NONE, 2, Menu.NONE, "Build");
            menuItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    mRenderer.mousePicker.changeSelectedAction("Build");
                    Button button = (Button) chainView;
                    button.setText("Build");

                    buildSelectionMenu = new PopupMenu(mActivity, chainView);
                    MenuInflater inflater = buildSelectionMenu.getMenuInflater();
                    inflater.inflate(R.menu.build_selection_menu, buildSelectionMenu.getMenu());
                    onCreateBuildSelectionMenu(buildSelectionMenu.getMenu());
                    buildSelectionMenu.show();

                    return false;
                }
            });
        }
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                System.out.println("1");
                return true;
            case 2:
                System.out.println("2");
                return true;
            case 3:
                System.out.println("3");
                return true;
            default:
                return false;
        }
    }*/

    @Override
    public boolean onDown(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        System.out.println(DEBUG_TAG + "; onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        System.out.println(DEBUG_TAG + "; onScroll: " + e1.toString() + e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        System.out.println(DEBUG_TAG + "; onSingleTapConfirmed: " + event.toString());
        return true;
    }

}