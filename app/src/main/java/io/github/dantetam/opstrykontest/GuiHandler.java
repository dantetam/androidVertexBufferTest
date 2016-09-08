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
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;

/**
 * Created by Dante on 9/8/2016.
 */
public class GuiHandler {

    private LessonSevenActivity mActivity;
    private LessonSevenRenderer mRenderer;

    public HashMap<City, TextView> cityTitleGui = new HashMap<>();
    public HashMap<City, PercentRelativeLayout> cityQueueGui = new HashMap<>();
    public HashMap<City, PercentRelativeLayout> cityFoodGui = new HashMap<>();

    public GuiHandler(LessonSevenActivity activity, LessonSevenRenderer renderer) {
        mActivity = activity;
        mRenderer = renderer;

        cityTitleGui = new HashMap<>();
        cityQueueGui = new HashMap<>();
        cityFoodGui = new HashMap<>();
    }

    public void updateGui(MousePicker mousePicker) {
        //System.out.println(storedCityPosition.toString() + " " + guiPosition.toString());
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);
        for (Clan clan: mRenderer.worldHandler.world.getClans()) {
            for (City city: clan.cities) {
                View cityView = cityTitleGui.get(city);
                if (cityView != null) {
                    Vector3f storedCityPosition = mRenderer.worldHandler.storedTileVertexPositions.get(city.location());
                    Vector2f guiPosition = mousePicker.calculateGraphicsScreenPos(storedCityPosition.x, storedCityPosition.z);
                    cityView.setX(guiPosition.x - cityView.getWidth() / 2);
                    cityView.setY(guiPosition.y - cityView.getHeight() / 2);
                } else {
                    //System.out.println("<<<<");
                    createClanCityTitle(clan, city);
                }

                PercentRelativeLayout cityQueue = cityQueueGui.get(city);
                if (cityView != null && cityQueue != null) {
                    Vector3f storedCityPosition = mRenderer.worldHandler.storedTileVertexPositions.get(city.location());
                    Vector2f guiPosition = mousePicker.calculateGraphicsScreenPos(storedCityPosition.x, storedCityPosition.z);
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
                    Vector2f guiPosition = mousePicker.calculateGraphicsScreenPos(storedCityPosition.x, storedCityPosition.z);
                    cityFood.setX(guiPosition.x - cityView.getWidth() / 2 - cityFood.getWidth());
                    cityFood.setY(guiPosition.y - cityView.getHeight() / 2);
                    //System.out.println((guiPosition.x + cityQueue.getWidth() / 2) + " ");
                } else {
                    //System.out.println("<<<<>>>>");
                    createClanCityFood(clan, city);
                }
            }
        }
    }

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
        stringy += " " + city.population;
        textView.setText(stringy);

        textView.setTextColor(ColorTextureHelper.intFromColorVector(clan.color));

        textView.setLayoutParams(param);
        guiLayout.addView(textView);

        cityTitleGui.put(city, textView);
    }

    private void createClanCityQueue(Clan clan, City city) {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);

        PercentRelativeLayout.LayoutParams param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //param.getPercentLayoutInfo().startMarginPercent = 0.2f;
        param.getPercentLayoutInfo().widthPercent = 0.025f;
        param.getPercentLayoutInfo().heightPercent = 0.07f;

        PercentRelativeLayout percentFrame = new PercentRelativeLayout(mActivity);
        percentFrame.setBackgroundColor(Color.WHITE);

        percentFrame.setLayoutParams(param);
        guiLayout.addView(percentFrame);

        cityQueueGui.put(city, percentFrame);
    }

    private void createClanCityFood(Clan clan, City city) {
        PercentRelativeLayout guiLayout = (PercentRelativeLayout) mActivity.findViewById(R.id.gui_display);

        PercentRelativeLayout.LayoutParams param = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.getPercentLayoutInfo().widthPercent = 0.025f;
        param.getPercentLayoutInfo().heightPercent = 0.07f;
        PercentRelativeLayout percentFrame = new PercentRelativeLayout(mActivity);
        percentFrame.setBackgroundColor(Color.WHITE);
        percentFrame.setLayoutParams(param);
        guiLayout.addView(percentFrame);

        TextView textView = new Button(mActivity);
        PercentRelativeLayout.LayoutParams foodBarParam = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        foodBarParam.getPercentLayoutInfo().widthPercent = param.getPercentLayoutInfo().widthPercent;
        foodBarParam.getPercentLayoutInfo().heightPercent = param.getPercentLayoutInfo().heightPercent * 0.5f;
        percentFrame.setGravity(Gravity.TOP);
        textView.setBackgroundColor(Color.GREEN);
        textView.setLayoutParams(foodBarParam);
        percentFrame.addView(textView);

        cityFoodGui.put(city, percentFrame);
    }

}
