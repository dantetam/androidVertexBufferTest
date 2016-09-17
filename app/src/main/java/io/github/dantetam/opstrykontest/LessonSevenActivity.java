package io.github.dantetam.opstrykontest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.opengl.EGLConfig;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.dantetam.android.MultiTextureHelper;
import io.github.dantetam.world.action.Ability;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.ai.RelationModifier;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.action.BuildingAction;
import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.factory.BuildingFactory;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Item;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.Recipe;
import io.github.dantetam.world.entity.Tech;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.entity.Tile;

public class LessonSevenActivity extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    /**
     * Hold a reference to our GLSurfaceView
     */
    public final LessonSevenActivity mActivity = this; //For use in final classes
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
    private PopupMenu infoSelectionMenu;

    private Clan playerClan;

    public AutomaticTurn turnStyle = AutomaticTurn.AUTOMATIC;

    public enum AutomaticTurn {
        AUTOMATIC, //Automatically move to new units when completed action
        ON_PRESS_TURN, //Move to the next unit when the user clicks the next turn button
        NEVER //Never use this feature, the player
    }

    /*public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View decorView = getWindow().getDecorView();
        // Hide the status bar.
        final int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // TODO: The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
                    decorView.setSystemUiVisibility(uiOptions);
                } else {
                    // TODO: The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                    if (actionBar != null) actionBar.hide();
                }
            }
        });

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

        mDetector = new GestureDetectorCompat(this, this);
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

        LinearLayout diplomacyMenu = (LinearLayout) findViewById(R.id.clan_button);
        if (diplomacyMenu != null) {
            diplomacyMenu.setVisibility(View.INVISIBLE);
            diplomacyMenu.setBackgroundColor(Color.TRANSPARENT);
        }
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

    public boolean onClickClanMenu(MenuItem item) {
        ScrollView clanScrollView = (ScrollView) findViewById(R.id.clan_menu_scroll);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.clan_menu_main);

        clanScrollView.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.setBackgroundColor(Color.TRANSPARENT);
        List<Clan> clans = mRenderer.worldHandler.world.getClans();

        ScrollView diploScrollView = (ScrollView) findViewById(R.id.diplomacy_menu_talk);
        LinearLayout clanMenu = (LinearLayout) findViewById(R.id.diplomacy_menu_talk_main);
        diploScrollView.setVisibility(View.INVISIBLE);
        clanMenu.setVisibility(View.INVISIBLE);

        //mLessonSevenActivity.setContentView(R.layout.test_custom_gamescreen);

        linearLayout.removeAllViews();

        TextView clanTitle = new TextView(this);
        clanTitle.setHeight(120);
        clanTitle.setText("Civilizations");
        linearLayout.addView(clanTitle);
        int i = 0;
        for (; i < clans.size(); i++) {
            final Clan clan = clans.get(i);
            if (!(clan instanceof CityState)) {
                Button clanView = new Button(this);
                clanView.setHeight(120);

                if (clan.equals(playerClan)) {
                    clanView.setText(clan.ai.leaderName + " of the " + clan.name + " (You)");
                } else {
                    String opinion = mRenderer.worldSystem.relations.get(clan).getOpinionString(playerClan);
                    clanView.setText(clan.ai.leaderName + " of the " + clan.name + " (" + opinion + ")");
                    if (mRenderer.worldSystem.atWar(playerClan, clan)) {
                        clanView.setText(clanView.getText() + " (WAR!)");
                    }
                    clanView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View clanMenu = findViewById(R.id.clan_menu_main);
                            clanMenu.setVisibility(View.INVISIBLE);
                            clanMenu.setBackgroundColor(Color.TRANSPARENT);
                            clanMenu = findViewById(R.id.clan_menu_scroll);
                            clanMenu.setVisibility(View.INVISIBLE);
                            clanMenu.setBackgroundColor(Color.TRANSPARENT);
                            onClickDiplomacyMenuCivilization(clan);
                        }
                    });
                }

                linearLayout.addView(clanView);
            }
            else {
                break;
            }
        }
        TextView cityStateTitle = new TextView(this);
        cityStateTitle.setHeight(120);
        cityStateTitle.setText("City States");
        linearLayout.addView(cityStateTitle);
        for (; i < clans.size(); i++) {
            final CityState clan = (CityState) clans.get(i);
            Button clanView = new Button(this);
            clanView.setHeight(120);

            String opinion = mRenderer.worldSystem.relations.get(clan).getOpinionString(playerClan);
            clanView.setText(clan.ai.leaderName + " of " + clan.name + " (" + opinion + ")");
            if (mRenderer.worldSystem.atWar(playerClan, clan)) {
                clanView.setText(clanView.getText() + " (WAR!)");
            }
            clanView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View clanMenu = findViewById(R.id.clan_menu_main);
                    clanMenu.setVisibility(View.INVISIBLE);
                    clanMenu.setBackgroundColor(Color.TRANSPARENT);
                    clanMenu = findViewById(R.id.clan_menu_scroll);
                    clanMenu.setVisibility(View.INVISIBLE);
                    clanMenu.setBackgroundColor(Color.TRANSPARENT);
                    onClickDiplomacyMenuCityState(clan);
                }
            });

            linearLayout.addView(clanView);
        }

        return true;
    }

    public void onClickDiplomacyMenuCivilization(final Clan c) {
        ScrollView scrollView = (ScrollView) findViewById(R.id.diplomacy_menu_talk);
        LinearLayout clanMenu = (LinearLayout) findViewById(R.id.diplomacy_menu_talk_main);
        scrollView.setVisibility(View.VISIBLE);
        clanMenu.setVisibility(View.VISIBLE);

        //((Button) findViewById(R.id.clan_title)).setText(c.ai.leaderName + " of the " + c.name);

        clanMenu.removeAllViews();

        Button clanView = new Button(this);
        clanView.setHeight(120);
        String opinion = mRenderer.worldSystem.relations.get(c).getOpinionString(playerClan);
        clanView.setText(c.ai.leaderName + " of the " + c.name + " (" + opinion + ")");
        clanMenu.addView(clanView);

        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu tempMenu = new PopupMenu(mActivity, v);
                MenuInflater inflater = tempMenu.getMenuInflater();
                inflater.inflate(R.menu.unit_selection_menu, tempMenu.getMenu());
                onCreateDiplomacyHistory(tempMenu.getMenu(), mRenderer.worldSystem.relations.get(c).getRelationModsForClan(playerClan));
                tempMenu.show();
            }
        });

        if (mRenderer.worldSystem.atWar(playerClan, c)) {
            clanView.setText(clanView.getText() + " (WAR!)");

            clanView = new Button(this);
            clanView.setHeight(120);
            clanView.setText("We have no business with bloodthirsty tyrants such as yourself.");
            clanMenu.addView(clanView);
        }
        else {
            clanView = new Button(this);
            clanView.setHeight(120);
            clanView.setText("Hello, what brings you here today?");
            clanMenu.addView(clanView);

            final Button warButton = new Button(this);
            warButton.setHeight(120);
            warButton.setText("< DECLARE WAR. >");
            warButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warButton.setText("< CONFIRM WAR. >");
                    warButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRenderer.worldSystem.declareWar(playerClan, c);
                            //mRenderer.worldSystem.declareWar(c, playerClan);
                            diplomacyMenuMessage(c, "I hope your people can forgive such a terrible mistake.", "We're sorry this has caused a divide between us.", true);
                            //onClickEndDiplomacyMenu(v);
                            //onClickDiplomacyMenu(c);
                        }
                    });
                }
            });
            clanMenu.addView(warButton);

            final Button denounceButton = new Button(this);
            denounceButton.setHeight(120);
            denounceButton.setText("< DENOUNCE. >");
            denounceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mRenderer.worldSystem.containsMod(playerClan, c, RelationModifier.DENOUNCE)) {
                        denounceButton.setText("< CONFIRM DENOUNCE. >");
                        denounceButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mRenderer.worldSystem.denounce(playerClan, c);
                                diplomacyMenuMessage(c, "Your lies hold no weight here.", "We're sorry this has caused a divide between us.", true);
                                //onClickEndDiplomacyMenu(v);
                                //onClickDiplomacyMenu(c);
                            }
                        });
                    }
                }
            });
            clanMenu.addView(denounceButton);

            Button tradeButton = new Button(this);
            tradeButton.setHeight(120);
            tradeButton.setText("< Trade >");
            tradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tradeMenuMessage(c, "What is your offer?");
                }
            });
            clanMenu.addView(tradeButton);
        }
        clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText("< End communication. >");
        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEndDiplomacyMenu(v);
            }
        });
        clanMenu.addView(clanView);
    }

    public void onClickDiplomacyMenuCityState(final CityState c) {
        ScrollView scrollView = (ScrollView) findViewById(R.id.diplomacy_menu_talk);
        LinearLayout clanMenu = (LinearLayout) findViewById(R.id.diplomacy_menu_talk_main);
        scrollView.setVisibility(View.VISIBLE);
        clanMenu.setVisibility(View.VISIBLE);

        //((Button) findViewById(R.id.clan_title)).setText(c.ai.leaderName + " of the " + c.name);

        clanMenu.removeAllViews();

        Button clanView = new Button(this);
        clanView.setHeight(120);
        String opinion = mRenderer.worldSystem.relations.get(c).getOpinionString(playerClan);
        clanView.setText(c.ai.leaderName + " of " + c.name + " (" + opinion + ")");
        clanMenu.addView(clanView);

        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu tempMenu = new PopupMenu(mActivity, v);
                MenuInflater inflater = tempMenu.getMenuInflater();
                inflater.inflate(R.menu.unit_selection_menu, tempMenu.getMenu());
                onCreateDiplomacyHistory(tempMenu.getMenu(), mRenderer.worldSystem.relations.get(c).getRelationModsForClan(playerClan));
                tempMenu.show();
            }
        });

        if (mRenderer.worldSystem.atWar(playerClan, c)) {
            clanView.setText(clanView.getText() + " (WAR!)");

            clanView = new Button(this);
            clanView.setHeight(120);
            clanView.setText("We have no business with bloodthirsty tyrants such as yourself.");
            clanMenu.addView(clanView);
        }
        else {
            clanView = new Button(this);
            clanView.setHeight(120);
            clanView.setText("Hello, what brings you here today?");
            clanMenu.addView(clanView);

            final Button warButton = new Button(this);
            warButton.setHeight(120);
            warButton.setText("< DECLARE WAR. >");
            warButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warButton.setText("< CONFIRM WAR. >");
                    warButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRenderer.worldSystem.declareWar(playerClan, c);
                            //mRenderer.worldSystem.declareWar(c, playerClan);
                            diplomacyMenuMessage(c, "I hope your people can forgive such a terrible mistake.", "We're sorry this has caused a divide between us.", true);
                            //onClickEndDiplomacyMenu(v);
                            //onClickDiplomacyMenu(c);
                        }
                    });
                }
            });
            clanMenu.addView(warButton);
        }
        clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText("< End communication. >");
        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEndDiplomacyMenu(v);
            }
        });
        clanMenu.addView(clanView);
    }

    public void tradeMenuMessage(final Clan c, String tradeMessage) {
        ScrollView clanMenu = (ScrollView) findViewById(R.id.diplomacy_menu_talk);
        LinearLayout diploMainDialogue = (LinearLayout) findViewById(R.id.diplomacy_menu_talk_main);

        clanMenu.setVisibility(View.INVISIBLE);
        diploMainDialogue.setVisibility(View.INVISIBLE);

        final LinearLayout mainTrade = (LinearLayout) findViewById(R.id.diplomacy_menu_trade);
        final LinearLayout theirProposals = (LinearLayout) findViewById(R.id.diplomacy_menu_trade_enemy_proposal);
        final LinearLayout yourProposals = (LinearLayout) findViewById(R.id.diplomacy_menu_trade_your_proposal);

        final LinearLayout theirOffer = (LinearLayout) findViewById(R.id.diplomacy_menu_trade_their_current_offer);
        final LinearLayout yourOffer = (LinearLayout) findViewById(R.id.diplomacy_menu_trade_your_current_offer);

        mainTrade.removeAllViews();
        theirProposals.removeAllViews();
        yourProposals.removeAllViews();

        mainTrade.setVisibility(View.VISIBLE);
        theirProposals.setVisibility(View.VISIBLE);
        yourProposals.setVisibility(View.VISIBLE);

        theirOffer.setVisibility(View.VISIBLE);
        yourOffer.setVisibility(View.VISIBLE);

        findOffers(c, playerClan, theirProposals);
        findOffers(playerClan, c, yourProposals);

        Button clanView = new Button(this);
        clanView.setHeight(120);
        String opinion = mRenderer.worldSystem.relations.get(c).getOpinionString(playerClan);
        clanView.setText(c.ai.leaderName + " of the " + c.name + " (" + opinion + ")");
        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu tempMenu = new PopupMenu(mActivity, v);
                MenuInflater inflater = tempMenu.getMenuInflater();
                inflater.inflate(R.menu.unit_selection_menu, tempMenu.getMenu());
                onCreateDiplomacyHistory(tempMenu.getMenu(), mRenderer.worldSystem.relations.get(c).getRelationModsForClan(playerClan));
                tempMenu.show();
            }
        });
        mainTrade.addView(clanView);

        clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText(tradeMessage);
        mainTrade.addView(clanView);

        clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText("Never mind.");
        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainTrade.setVisibility(View.INVISIBLE);
                theirProposals.setVisibility(View.INVISIBLE);
                yourProposals.setVisibility(View.INVISIBLE);
                theirOffer.setVisibility(View.INVISIBLE);
                yourOffer.setVisibility(View.INVISIBLE);
                /*if (c instanceof CityState) {
                    onClickDiplomacyMenuCityState((CityState) c);
                }
                else {
                    onClickDiplomacyMenuCivilization(c);
                }*/
                onClickDiplomacyMenuCivilization(c);
            }
        });
        mainTrade.addView(clanView);
    }

    public void findOffers(Clan clan, Clan offering, LinearLayout layout) {
        Button clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText(clan.totalGold + " gold");
        layout.addView(clanView);

        for (City city: clan.cities) {
            clanView = new Button(this);
            clanView.setHeight(120);
            clanView.setText(city.name + " " + city.population() + " " + city.cityTiles.size());
            layout.addView(clanView);
        }

        for (Clan otherClan: mRenderer.worldSystem.world.getClans()) {
            if (!otherClan.equals(offering) && !otherClan.equals(clan)) {
                if (!mRenderer.worldSystem.atWar(clan, otherClan)) {
                    clanView = new Button(this);
                    clanView.setHeight(120);
                    clanView.setText("Declare WAR on " + otherClan.name);
                    layout.addView(clanView);
                }
                else {
                    clanView = new Button(this);
                    clanView.setHeight(120);
                    clanView.setText("Make PEACE with " + otherClan.name);
                    layout.addView(clanView);
                }
            }
        }
    }

    public void diplomacyMenuMessage(final Clan c, String message, String response, boolean endComms) {
        ScrollView scrollView = (ScrollView) findViewById(R.id.diplomacy_menu_talk);
        LinearLayout clanMenu = (LinearLayout) findViewById(R.id.diplomacy_menu_talk_main);
        scrollView.setVisibility(View.VISIBLE);
        clanMenu.setVisibility(View.VISIBLE);

        //((Button) findViewById(R.id.clan_title)).setText(c.ai.leaderName + " of the " + c.name);

        clanMenu.removeAllViews();

        Button clanView = new Button(this);
        clanView.setHeight(120);
        String opinion = mRenderer.worldSystem.relations.get(c).getOpinionString(playerClan);
        clanView.setText(c.ai.leaderName + " of the " + c.name + " (" + opinion + ")");
        if (mRenderer.worldSystem.atWar(playerClan, c)) {
            clanView.setText(clanView.getText() + " (WAR!)");
        }
        clanMenu.addView(clanView);

        clanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu tempMenu = new PopupMenu(mActivity, v);
                MenuInflater inflater = tempMenu.getMenuInflater();
                inflater.inflate(R.menu.unit_selection_menu, tempMenu.getMenu());
                onCreateDiplomacyHistory(tempMenu.getMenu(), mRenderer.worldSystem.relations.get(c).getRelationModsForClan(playerClan));
                tempMenu.show();
            }
        });

        clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText(message);
        clanMenu.addView(clanView);

        clanView = new Button(this);
        clanView.setHeight(120);
        clanView.setText(response);
        if (endComms) {
            clanView.setText(clanView.getText() + " < End communication. >");
            clanView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickEndDiplomacyMenu(v);
                }
            });
        }
        else {
            clanView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (c instanceof CityState) {
                        onClickDiplomacyMenuCityState((CityState) c);
                    }
                    else {
                        onClickDiplomacyMenuCivilization(c);
                    }
                }
            });
        }
        clanMenu.addView(clanView);
    }

    public boolean onCreateDiplomacyHistory(Menu menu, List<String> modifiers) {
        if (modifiers.size() == 0) {
            MenuItem menuItem = menu.add(Menu.NONE, 1, Menu.NONE, "There is no history between your nations.");
        }
        for (String stringy: modifiers) {
            //WorldSystem.RelationModifier modifier = modifiers.get(i);
            MenuItem menuItem = menu.add(Menu.NONE, 1, Menu.NONE, stringy);
        }
        return true;
    }

    public void onClickEndDiplomacyMenu(View v) {
        ScrollView scrollView = (ScrollView) findViewById(R.id.diplomacy_menu_talk);
        LinearLayout clanMenu = (LinearLayout) findViewById(R.id.diplomacy_menu_talk_main);
        scrollView.setVisibility(View.INVISIBLE);
        clanMenu.setVisibility(View.INVISIBLE);
    }

    public void onClickNextTurnMenu(View v) {
        Entity en = mRenderer.findNextUnit();
        if (en != null) {
            ((Button) v).setText("UNIT NEEDS ORDERS");
            //((MenuItem) findViewById(R.id.next_turn_button)).setTitle("UNIT NEEDS ORDERS");

            mRenderer.debounceFrames = 10;

            mRenderer.moveCameraInFramesAfter = 1;
            mRenderer.nextUnit = en;
        } else if (playerClan.techTree.researchingTechQueue.size() == 0) {
            mRenderer.mousePicker.changeSelectedTile(null);
            mRenderer.mousePicker.changeSelectedUnit(null);
            ((Button) v).setText("CHOOSE RESEARCH");
            if (findViewById(R.id.tech_tree_screen).getVisibility() == View.INVISIBLE)
                onClickTechMenu(findViewById(R.id.tech_menu));
            mGLSurfaceView.update();
        } else {
            ((Button) v).setText("NEXT TURN");
            //((MenuItem) findViewById(R.id.next_turn_button)).setTitle("NEXT TURN");
            PopupMenu tempMenu = new PopupMenu(this, v);
            MenuInflater inflater = tempMenu.getMenuInflater();
            inflater.inflate(R.menu.next_turn_menu, tempMenu.getMenu());
            tempMenu.show();
        }
    }

    public boolean onClickNextTurnButton(MenuItem item) {
        Entity en = mRenderer.findNextUnit();
        /*if (en != null && (turnStyle == AutomaticTurn.AUTOMATIC || turnStyle == AutomaticTurn.ON_PRESS_TURN)) {
            mRenderer.moveCameraInFramesAfter = 1;
            mRenderer.nextUnit = en;
        }
        else*/
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
            if (mRenderer.mousePicker.getSelectedEntity() == null) {
                return true;
            }
            selected = mRenderer.mousePicker.getSelectedEntity().location();
        }
        if (selected != null) {
            if (selected.occupants.size() == 0) {
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "No units to select");
            } else {
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
            if (selectedImprovement instanceof City) {
                final City city = (City) selectedImprovement;

                SubMenu improvementSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Build improvement");

                Set<BuildingType> allowedBuildings = selectedImprovement.clan.techTree.allowedBuildings.keySet();
                int i = 0;
                for (final BuildingType buildingType : allowedBuildings) {
                    int[] yield = buildingType.getYield();
                    String yieldString = "";
                    if (yield[0] > 0) {
                        yieldString += "+" + yield[0] + "F";
                    }
                    if (yield[1] > 0) {
                        yieldString += ", +" + yield[1] + "P";
                    }
                    if (yield[2] > 0) {
                        yieldString += ", +" + yield[2] + "S";
                    }
                    if (yield[3] > 0) {
                        yieldString += ", +" + yield[3] + "C";
                    }
                    if (yield[4] > 0) {
                        yieldString += ", +" + yield[0] + " :)";
                    }
                    if (yield[5] > 0) {
                        yieldString += ", +" + yield[1] + "H";
                    }
                    if (yield[6] > 0) {
                        yieldString += ", +" + yield[2] + "Cul";
                    }

                    int[] cityYield = (int[]) (city.gameYield()[0]);
                    int turnsCalculated = (int) Math.ceil((double) buildingType.workNeeded / (double) cityYield[1]);
                    yieldString += " " + turnsCalculated + " turns";

                    String displayName = buildingType.name + " " + yieldString;
                    MenuItem menuItem = improvementSubMenu.add(Menu.NONE, i + 1, Menu.NONE, displayName);
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            Building newBuilding = BuildingFactory.newModule(selectedImprovement.world, selectedImprovement.clan, buildingType, selected, 0, selectedImprovement);
                            city.actionsQueue.clear();
                            city.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, newBuilding));
                            return false;
                        }
                    });

                    i++;
                }
            /*for (int i = 0; i < selectedImprovement.modules.size(); i++) {
                Building module = selectedImprovement.modules.get(i);
                String stringy;
                if (module == null) {
                    stringy = "Build improvement";
                    SubMenu moduleSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, i, stringy);

                    List<BuildingType> allowedBuildings = selectedImprovement.clan.techTree.allowedBuildingsAndModules.get(selectedImprovement.buildingType); //Well, that's all she wrote
                    for (final BuildingType buildingType: allowedBuildings) {
                        MenuItem menuItem = moduleSubMenu.add(Menu.NONE, i+1, Menu.NONE, buildingType.name);
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
                else {
                    stringy = module.name;

                    if (module.completionPercentage() < 1) {
                        stringy += " (" + (int) (module.completionPercentage() * 100) + "% Completed)";
                        //SubMenu moduleSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, i, stringy);
                    }
                    SubMenu moduleSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, i, stringy);
                    List<String> strings = new ArrayList<>();
                    int[] yield = module.getYieldWithModules();
                    String yieldString = "";
                    if (yield[0] > 0) {
                        yieldString += "+" + yield[0] + "Food";
                    }
                    if (yield[1] > 0) {
                        yieldString += ", +" + yield[1] + "Prod.";
                    }
                    if (yield[2] > 0) {
                        yieldString += ", +" + yield[2] + "Sci.";
                    }
                    if (yield[3] > 0) {
                        yieldString += ", +" + yield[3] + "Cap.";
                    }
                    if (!yieldString.equals(""))
                        strings.add(yieldString);
                    for (Recipe recipe: module.recipes) {
                        strings.add(recipe.toString());
                    }
                    for (String texty: strings) {
                        MenuItem menuItem = moduleSubMenu.add(Menu.NONE, 0, Menu.NONE, texty);
                    }
                }
            }*/

                int[] yield = (int[]) (city.gameYield()[0]);

                SubMenu unitSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Build unit");
                Set<PersonType> allowedPeople = selectedImprovement.clan.techTree.allowedUnits.keySet();
                for (final PersonType personType : allowedPeople) {
                    //System.out.println(personType.name + " " + allowedPeople.size());
                    String yieldString = personType.name;

                    if (personType.atk > 0) {
                        yieldString += " " + personType.atk + "A";
                    }
                    if (personType.def > 0) {
                        yieldString += ", " + personType.def + "D";
                    }
                    if (personType.maneuver > 0) {
                        yieldString += ", " + personType.maneuver + "M";
                    }
                    if (personType.fire > 0) {
                        yieldString += ", " + personType.fire + "F";
                    }
                    if (personType.shock > 0) {
                        yieldString += ", " + personType.shock + "S";
                    }
                    if (personType.maxH > 0) {
                        yieldString += ", " + personType.maxH + "HP";
                    }

                    int turnsCalculated = (int) Math.ceil((double) personType.workNeeded / (double) yield[1]);
                    yieldString += " " + turnsCalculated + " turns";

                    MenuItem menuItem = unitSubMenu.add(Menu.NONE, 0, Menu.NONE, yieldString);
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            //Building newBuilding = BuildingFactory.newModule(selectedImprovement.world, selectedImprovement.clan, buildingType, selected, 0, selectedImprovement);
                            //newBuilding.actionsQueue.clear();
                            //newBuilding.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, newBuilding));
                            //Person newPerson = PersonFactory.newPerson(personType, selectedImprovement.world, selectedImprovement.clan, 0.0);
                            //TODO: Use city method (subtract resources) selectedImprovement.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_UNIT, newPerson));
                            city.queueActionBuildUnit(personType);
                            return false;
                        }
                    });
                }
            }
        }
        return true;
    }

    public void onClickInfoMenu(View v) {
        infoSelectionMenu = new PopupMenu(this, v);
        MenuInflater inflater = infoSelectionMenu.getMenuInflater();
        inflater.inflate(R.menu.queue_selection_menu, infoSelectionMenu.getMenu());
        onCreateSelectionStatMenu(infoSelectionMenu.getMenu());
        infoSelectionMenu.show();
    }

    public boolean onCreateSelectionStatMenu(Menu menu) {
        final boolean selectedTileExists = mRenderer.mousePicker.getSelectedTile() != null;
        final boolean selectedEntityExists = mRenderer.mousePicker.getSelectedEntity() != null;

        LinkedHashMap<String, String> tooltips = new LinkedHashMap<>();

        String affiliation = "";
        if (selectedTileExists) {
            Tile selected = mRenderer.mousePicker.getSelectedTile();
            Clan owner = selected.world.getTileOwner(selected), influence = selected.world.getTileInfluence(selected);
            if (owner != null) {
                affiliation = owner.name;
            } else if (influence != null) {
                affiliation = "(" + influence.name + ")";
            } else {
                affiliation = "Free";
            }
            tooltips.put("text1", affiliation);

            String locationInfo = "";
            if (Debug.enabled) {
                locationInfo = " " + selected.toString();
            }
            tooltips.put("text2", Tile.Biome.nameFromInt(selected.biome.type) + ", " + Tile.Terrain.nameFromInt(selected.terrain.type) + locationInfo);
            if (selected.improvement == null) {
                tooltips.put("text3", "Can build improvement");
            } else {
                int p = (int) (selected.improvement.completionPercentage() * 100d);
                String extra = p < 1 ? " (" + p + "% Completed)" : "";
                tooltips.put("text3", selected.improvement.name + extra);
            }
            if (selected.resources.size() > 0) {
                String stringy = "";
                for (Item resource : selected.resources) {
                    String s = resource.name;
                    if (!s.equals("No resource"))
                        stringy += s + " ";
                }
                if (!stringy.equals(""))
                    tooltips.put("text4", stringy);
            }
            /*if (selected.improvement != null) {
                String items = selected.improvement.getInventory().size() + "/" + selected.improvement.inventorySpace + " Items";
                tooltips.put("text5", items);
            }*/
            int[] yieldData = City.evalTile(selected);
            String yield = "Yield: " + yieldData[0] + "F, " + yieldData[1] + "P, " + yieldData[2] + "S, " + yieldData[3] + "C";
            tooltips.put("text6", yield);
        } else if (selectedEntityExists) {
            Entity entity = mRenderer.mousePicker.getSelectedEntity();
            String stringy = entity.name + " (";
            if (entity.clan != null) {
                stringy += entity.clan.name + ")";
            } else {
                stringy += "Free)";
            }
            if (entity instanceof Person) {
                Person person = (Person) entity;
                stringy += " " + person.actionPoints + "/" + person.maxActionPoints + " AP";
            }
            tooltips.put("text1", stringy);

            /*if (entity != null) {
                String items = entity.getInventory().size() + "/" + entity.inventorySpace + " Items";
                tooltips.put("text5", items);
            }*/
        }

        int i = 0;
        for (Map.Entry<String, String> en : tooltips.entrySet()) {
            SubMenu subMenu = menu.addSubMenu(Menu.NONE, i, Menu.NONE, en.getValue());
            final String finalAffiliation = affiliation;
            MenuItem title = subMenu.add(Menu.NONE, i, Menu.NONE, en.getValue());
            if (en.getKey().equals("text1")) {
                String clanStringy = "";
                if (selectedTileExists || selectedEntityExists) {
                    if (finalAffiliation.equals("Free")) {
                        clanStringy = "This land has no influence.";
                    } else if (finalAffiliation.contains("(")) {
                        clanStringy = "The most influential clan.";
                    } else {
                        clanStringy = "The current owner.";
                    }
                }
                MenuItem menuItem = subMenu.add(Menu.NONE, i, Menu.NONE, clanStringy);
            } else if (en.getKey().equals("text2")) {
                MenuItem menuItem = subMenu.add(Menu.NONE, i, Menu.NONE, "The biome (climate) and terrain type (shape).");
            } else if (en.getKey().equals("text3")) {
                MenuItem menuItem = subMenu.add(Menu.NONE, i, Menu.NONE, "Buildings to increase yields, craft, etc.");
            } else if (en.getKey().equals("text4")) {
                MenuItem menuItem = subMenu.add(Menu.NONE, i, Menu.NONE, "Used for buildings and items to equip units.");
            }
            /*else if (en.getKey().equals("text5")) {
                Entity entity = mRenderer.mousePicker.getSelectedEntity();
                if (entity == null) {
                    entity = mRenderer.mousePicker.getSelectedTile().improvement;
                }
                List<Item> inventory = entity.getInventory();
                //MenuItem title = subMenu.add(Menu.NONE, 0, Menu.NONE, en.getValue());
                for (int j = 0; j < inventory.size(); j++) {
                    MenuItem menuItem = subMenu.add(Menu.NONE, j+1, Menu.NONE, inventory.get(j).toString());
                }
            }*/
            else if (en.getKey().equals("text6")) {
                Building improvement = mRenderer.mousePicker.getSelectedTile().improvement;
                if (improvement != null) {
                    for (Recipe recipe : improvement.recipes) {
                        MenuItem menuItem = subMenu.add(Menu.NONE, i, Menu.NONE, recipe.toString());
                    }
                }
            }
            //selectedStatMenu.addView(bt);
            i++;
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
        Entity entity = mRenderer.mousePicker.getSelectedEntity();
        City city = null;
        Tile selectedTile = mRenderer.mousePicker.getSelectedTile();
        if (selectedTile != null) {
            if (selectedTile.improvement instanceof City) {
                city = (City) selectedTile.improvement;
            }
        }

        final Entity selectedEntity = entity != null ? entity : city;
        if (selectedEntity != null) {
            if (selectedEntity.actionsQueue.size() == 0) {
                menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Nothing in queue");
            } else {
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

            MenuItem menuItem4 = menu.add(Menu.NONE, 1, Menu.NONE, "Fortify");
            menuItem4.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    mRenderer.mousePicker.changeSelectedAction("Fortify");
                    Button button = (Button) chainView;
                    button.setText("Fortify");
                    return false;
                }
            });

            /*MenuItem menuItem3 = menu.add(Menu.NONE, 3, Menu.NONE, "Fight");
            menuItem3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    mRenderer.mousePicker.changeSelectedAction("InitiateCombat");
                    return false;
                }
            });*/

        }
        return true;
    }

    public boolean onCreateCombatSelectionMenu(final View chainView, Menu menu) {
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

            MenuItem menuItem3 = menu.add(Menu.NONE, 3, Menu.NONE, "Fight");
            menuItem3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    mRenderer.mousePicker.changeSelectedAction("InitiateCombat");
                    return false;
                }
            });
        }
        return true;
    }

    public void onClickExitCombatMenu(View v) {
        mRenderer.setCombatMode(false);
    }

    public void onClickNextCombatTurn(View v) {
        mRenderer.worldHandler.world.combatWorld.advanceTurn();
    }

    public void onClickTechMenu(View v) {
        GridLayout techScreen = (GridLayout) findViewById(R.id.tech_tree_screen);
        if (techScreen.getVisibility() == View.VISIBLE) {
            techScreen.setVisibility(View.INVISIBLE);
        } else {
            techScreen.setVisibility(View.VISIBLE);
            //Clear the tech tree and setup a new one
            //TODO: Update this only when techs update (set a listener?)
            updateTechMenu();
        }
    }

    public void updateTechMenu() {
        GridLayout techScreen = (GridLayout) findViewById(R.id.tech_tree_screen);
        techScreen.removeAllViews();

        int playerGlobalScience = WorldSystem.getGlobalScience(playerClan);
        if (playerGlobalScience <= 0)
            playerGlobalScience = 1;

        TechTree tree = playerClan.techTree;
        for (Map.Entry<String, Tech> entry : tree.techMap.entrySet()) {
            final Tech tech = entry.getValue();

            int minX = (int) tree.screenCenterX - tree.sightX;
            int maxX = (int) tree.screenCenterX + tree.sightX;
            int minY = (int) tree.screenCenterY - tree.sightY;
            int maxY = (int) tree.screenCenterY + tree.sightY;

            if (tech.treeOffsetX < minX || tech.treeOffsetY < minY || tech.treeOffsetX > maxX || tech.treeOffsetY > maxY) {
                continue;
            }

            int adjCoordX = tech.treeOffsetX - minX;
            int adjCoordY = (tree.sightY*2 + 1) - (tech.treeOffsetY - minY);

            if (adjCoordX < 0 || adjCoordX >= techScreen.getColumnCount() || adjCoordY < 0 || adjCoordY >= techScreen.getRowCount()) {
                continue;
            }

            Button textView = new Button(this);
            String stringy = tech.name;

            int estimatedTurns = (tech.researchNeeded - tech.researchCompleted) / playerGlobalScience;

            int foundInResearchingIndex = -1;
            int i = 0;
            for (Tech t: playerClan.techTree.researchingTechQueue) {
                if (tech.equals(t)) {
                    foundInResearchingIndex = i;
                    break;
                }
                i++;
            }

            if (foundInResearchingIndex != -1) {
                textView.setBackgroundColor(Color.MAGENTA);
                stringy += " (" + estimatedTurns + ")";
                stringy = (foundInResearchingIndex + 1) + ". " + stringy;
                //textView.setTextColor(Color.BLACK);
            } else if (tech.researched()) {
                textView.setBackgroundColor(Color.BLUE);
                textView.setTextColor(Color.WHITE);
            } else if (tech.researchable()) {
                stringy += " (" + estimatedTurns + ")";
                textView.setBackgroundColor(Color.GREEN);
            }

            String unlocked = "";
            for (PersonType personType : tech.unlockedUnits) {
                unlocked += personType.name + " ";
            }
            for (BuildingType buildingType : tech.unlockedBuildings) {
                unlocked += buildingType.name + " ";
            }
            for (ItemType itemType: tech.revealResources) {
                unlocked += itemType.name + " ";
            }
            for (ItemType itemType: tech.harvestableResources) {
                unlocked += itemType.name + " ";
            }
            for (Ability ability: tech.unlockedSpecialAbilities) {
                unlocked += ability.name + " ";
            }
            stringy += "\n" + unlocked;

            textView.setText(stringy);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams(
                    GridLayout.spec(adjCoordY, GridLayout.LEFT),
                    GridLayout.spec(adjCoordX, GridLayout.BOTTOM));
            param.setGravity(Gravity.CENTER);
            param.height = 250;
            param.width = 400;
            //param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            //param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            //param.rightMargin = 5;
            //param.topMargin = 5;
            //param.setGravity(Gravity.CENTER);
            //param.columnSpec = GridLayout.spec(c);
            //param.rowSpec = GridLayout.spec(r);
            textView.setLayoutParams(param);
            techScreen.addView(textView);

            /*textView.setFocusable(false);
            textView.setFocusableInTouchMode(false);
            textView.setClickable(false);*/

            if (!tech.researched()) {
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playerClan.techTree.researchingTechQueue.clear();
                        playerClan.techTree.beeline(tech);
                        updateTechMenu();
                    }

                    /*public boolean onTouchEvent(MotionEvent event) {
                        return false;
                    }*/
                });
            }

            List<String> techInfo = new ArrayList<>();

            if (tech.unlockedUnits.size() > 0) {
                String infoStringy = "";
                for (PersonType personType : tech.unlockedUnits) {
                    infoStringy += personType.name + " ";
                }
                techInfo.add(infoStringy);
            }

            if (tech.unlockedBuildings.size() > 0) {
                String buildStringy = "";
                for (BuildingType buildingType : tech.unlockedBuildings) {
                    buildStringy += buildingType.name + " ";
                }
                techInfo.add(buildStringy);
            }

            for (ItemType itemType: tech.revealResources) {
                techInfo.add("Reveals " + itemType.name);
            }
            for (ItemType itemType: tech.harvestableResources) {
                techInfo.add("Access" + itemType.name);
            }
            for (Ability ability: tech.unlockedSpecialAbilities) {
                techInfo.add(ability.desc);
            }

            if (tech.unlockedTechs.size() > 0) {
                String unlockStringy = "Leads to ";
                for (Tech unlockedTech : tech.unlockedTechs) {
                    unlockStringy += unlockedTech.name + " ";
                }
                techInfo.add(unlockStringy);
            }

            InfoHelper.addInfoOnLongClick(textView, techInfo);
        }
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