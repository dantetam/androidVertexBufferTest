package io.github.dantetam.opstrykontest;

import android.graphics.Color;
import android.support.percent.PercentRelativeLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import io.github.dantetam.android.ColorTextureHelper;
import io.github.dantetam.opengl.MousePicker;
import io.github.dantetam.utilmath.Vector2f;
import io.github.dantetam.utilmath.Vector3f;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tile;

/**
 * Created by Dante on 9/8/2016.
 */
public class GuiHandler {

    private LessonSevenActivity mActivity;
    private LessonSevenRenderer mRenderer;

    public HashMap<City, TextView> cityTitleGui = new HashMap<>();
    public HashMap<City, PercentRelativeLayout> cityQueueGui = new HashMap<>();
    public HashMap<City, PercentRelativeLayout> cityFoodGui = new HashMap<>();
    public HashMap<Entity, PercentRelativeLayout> entityHealthGui = new HashMap<>();

    public GuiHandler(LessonSevenActivity activity, LessonSevenRenderer renderer) {
        mActivity = activity;
        mRenderer = renderer;

        cityTitleGui = new HashMap<>();
        cityQueueGui = new HashMap<>();
        cityFoodGui = new HashMap<>();
    }

    /*public class ChainPositionView {
        public TextView view;
        public Object chain;
        public Vector2f offset;
    }*/

    public void forceUpdate() {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);
        guiLayout.removeAllViews();
        cityTitleGui.clear();
        cityQueueGui.clear();
        cityFoodGui.clear();
        entityHealthGui.clear();
    }

    public void updateGui(MousePicker mousePicker) {
        //System.out.println(storedCityPosition.toString() + " " + guiPosition.toString());
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);
        HashMap<Tile, Vector3f> wPos = mRenderer.worldHandler.storedTileVertexPositions;
        for (Clan clan: mRenderer.worldHandler.world.getClans()) {
            for (City city: clan.cities) {
                View cityView = cityTitleGui.get(city);
                if (cityView != null) {
                    Vector3f storedCityPosition = wPos.get(city.location());
                    Vector2f guiPosition = mousePicker.calcScrPos(storedCityPosition.x, storedCityPosition.z);
                    cityView.setX(guiPosition.x - cityView.getWidth() / 2);
                    cityView.setY(guiPosition.y - cityView.getHeight() / 2);
                } else {
                    //System.out.println("<<<<");
                    createClanCityTitle(clan, city);
                }

                PercentRelativeLayout cityQueue = cityQueueGui.get(city);
                if (cityView != null && cityQueue != null) {
                    Vector3f storedCityPosition = mRenderer.worldHandler.storedTileVertexPositions.get(city.location());
                    Vector2f guiPosition = mousePicker.calcScrPos(storedCityPosition.x, storedCityPosition.z);
                    cityQueue.setX(guiPosition.x + cityView.getWidth() / 2);
                    cityQueue.setY(guiPosition.y - cityView.getHeight() / 2);
                    //System.out.println((guiPosition.x + cityQueue.getWidth() / 2) + " ");
                } else {
                    //System.out.println("<<<<>>>>");
                    createClanCityQueue(clan, city);
                }

                PercentRelativeLayout cityFood = cityFoodGui.get(city);
                if (cityView != null && cityFood != null) {
                    Vector3f storedCityPosition = mRenderer.worldHandler.storedTileVertexPositions.get(city.location());
                    Vector2f guiPosition = mousePicker.calcScrPos(storedCityPosition.x, storedCityPosition.z);
                    cityFood.setX(guiPosition.x - cityView.getWidth() / 2 - cityFood.getWidth());
                    cityFood.setY(guiPosition.y - cityView.getHeight() / 2);
                    //System.out.println((guiPosition.x + cityQueue.getWidth() / 2) + " ");
                } else {
                    //System.out.println("<<<<>>>>");
                    createClanCityFood(clan, city);
                }
            }
            for (Entity entity: clan.people) {
                PercentRelativeLayout entityHealth = entityHealthGui.get(entity);
                if (entityHealth != null) {
                    Vector3f storedCityPosition = mRenderer.worldHandler.storedTileVertexPositions.get(entity.location());
                    Vector2f guiPosition = mousePicker.calcScrPos(storedCityPosition.x, storedCityPosition.z);
                    entityHealth.setX(guiPosition.x);
                    entityHealth.setY(guiPosition.y);
                    //entityHealth.setX(guiPosition.x - cityView.getWidth() / 2 - cityFood.getWidth());
                    //entityHealth.setY(guiPosition.y - cityView.getHeight() / 2);
                    //System.out.println((guiPosition.x + cityQueue.getWidth() / 2) + " ");
                } else {
                    //System.out.println("<<<<>>>>");
                    createEntityHealthBar(entity);
                }
            }
        }
    }

    /*public void validateGui(HashMap map, ) {

    }*/

    private void createClanCityTitle(Clan clan, City city) {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);

        TextView textView = new Button(mActivity);

        PercentRelativeLayout.LayoutParams param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.getPercentLayoutInfo().widthPercent = 0.15f;
        param.getPercentLayoutInfo().heightPercent = 0.07f;

        String stringy = city.name;
        if (city.isCapital != null) {
            stringy += "*";
        }
        stringy += " " + city.population();
        textView.setText(stringy);

        textView.setTextColor(ColorTextureHelper.intFromColorVector(clan.color));

        textView.setLayoutParams(param);
        guiLayout.addView(textView);

        cityTitleGui.put(city, textView);
    }

    private void createClanCityQueue(Clan clan, City city) {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);

        float percent = 0;
        if (city.actionsQueue.size() > 0) {
            Action action = city.actionsQueue.get(0);
            if (action.data instanceof Entity) {
                percent = (float)((Entity) action.data).completionPercentage();
            }
        }

        PercentRelativeLayout.LayoutParams param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //param.getPercentLayoutInfo().startMarginPercent = 0.2f;
        param.getPercentLayoutInfo().widthPercent = 0.025f;
        param.getPercentLayoutInfo().heightPercent = 0.07f;
        PercentRelativeLayout percentFrame = new PercentRelativeLayout(mActivity);
        percentFrame.setBackgroundColor(Color.BLUE);
        percentFrame.setLayoutParams(param);
        guiLayout.addView(percentFrame);

        param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.getPercentLayoutInfo().widthPercent = 1f;
        param.getPercentLayoutInfo().heightPercent = 1f;
        //param.getPercentLayoutInfo().leftMarginPercent = 0f;
        TextView newView = new TextView(mActivity);
        newView.setBackgroundColor(Color.TRANSPARENT);
        newView.setLayoutParams(param);
        Entity queued = city.getQueuedEntity();
        if (queued != null) {
            float turns = 0;
            if (queued instanceof Person) {
                turns = (float) ((Person) queued).personType.workNeeded / (float) city.lastYield[0];
            } else if (queued instanceof Building) {
                turns = (float) ((Building) queued).buildingType.workNeeded / (float) city.lastYield[0];
            } else {
                turns = 99;
            }
            newView.setText((int) turns + "");
        }
        else {
            newView.setText("-");
        }
        newView.setTextColor(Color.WHITE);
        percentFrame.addView(newView);

        percentFrame.setGravity(Gravity.TOP);
        TextView textView = percentBarColorAndText(percent, Color.RED, "");
        percentFrame.addView(textView);

        newView.bringToFront();

        cityQueueGui.put(city, percentFrame);
    }

    private void createClanCityFood(Clan clan, City city) {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);

        PercentRelativeLayout.LayoutParams param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.getPercentLayoutInfo().widthPercent = 0.025f;
        param.getPercentLayoutInfo().heightPercent = 0.07f;
        PercentRelativeLayout percentFrame = new PercentRelativeLayout(mActivity);
        percentFrame.setBackgroundColor(Color.BLUE);
        percentFrame.setLayoutParams(param);
        guiLayout.addView(percentFrame);

        param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.getPercentLayoutInfo().widthPercent = 1f;
        param.getPercentLayoutInfo().heightPercent = 1f;
        //param.getPercentLayoutInfo().leftMarginPercent = 0f;
        TextView newView = new TextView(mActivity);
        newView.setBackgroundColor(Color.TRANSPARENT);
        newView.setLayoutParams(param);
        if (city.lastYield[0] != 0) {
            float turns = (float) city.foodNeededForGrowth / (float) city.lastYield[0];
            newView.setText((int)turns + "");
        }
        else {
            newView.setText("-");
        }
        newView.setTextColor(Color.WHITE);
        percentFrame.addView(newView);

        float percent = Math.min((float) city.foodStoredForGrowth / (float) city.foodNeededForGrowth, 1);

        TextView textView = percentBarColorAndText(percent, Color.GREEN, "");
        percentFrame.addView(textView);

        newView.bringToFront();

        cityFoodGui.put(city, percentFrame);
    }

    private void createEntityHealthBar(Entity entity) {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);

        PercentRelativeLayout.LayoutParams param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.getPercentLayoutInfo().widthPercent = 0.025f;
        param.getPercentLayoutInfo().heightPercent = 0.07f;
        PercentRelativeLayout percentFrame = new PercentRelativeLayout(mActivity);
        percentFrame.setBackgroundColor(Color.BLUE);
        percentFrame.setLayoutParams(param);
        guiLayout.addView(percentFrame);

        float percent = Math.min((float) entity.health / (float) entity.maxHealth, 1);

        TextView textView = percentBarColorAndText(percent, Color.GREEN, "");
        percentFrame.addView(textView);

        entityHealthGui.put(entity, percentFrame);
    }

    private TextView percentBarColorAndText(float percent, int color, String text) {
        TextView textView = new Button(mActivity);
        PercentRelativeLayout.LayoutParams foodBarParam = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //foodBarParam.getPercentLayoutInfo().widthPercent = param.getPercentLayoutInfo().widthPercent;
        //foodBarParam.getPercentLayoutInfo().heightPercent = param.getPercentLayoutInfo().heightPercent * 0.5f;
        foodBarParam.getPercentLayoutInfo().widthPercent = 1f;
        foodBarParam.getPercentLayoutInfo().heightPercent = percent;
        foodBarParam.getPercentLayoutInfo().topMarginPercent = 1 - percent;

        textView.setBackgroundColor(color);
        textView.setLayoutParams(foodBarParam);
        textView.bringToFront();

        textView.setText(text);

        return textView;
    }

}
