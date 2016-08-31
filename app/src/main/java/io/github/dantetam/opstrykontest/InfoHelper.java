package io.github.dantetam.opstrykontest;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import java.util.Set;

import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.action.BuildingAction;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.factory.BuildingFactory;

/**
 * Created by Dante on 8/29/2016.
 *
 * A utility for creating onLongClick type listeners for the MenuItem and Button views,
 * which generate a popup menu, or a context menu, at the view.
 */
public class InfoHelper {

    private static LessonSevenActivity mActivity;

    public static void init(LessonSevenActivity activity) {
        mActivity = activity;
    }

    /*public static void addInfoOnLongClick(SubMenu item, final String[] info) {
        item.setHeaderTitle(item.() + " (i)");
        item.on(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu tempMenu = new PopupMenu(mActivity, v);
                MenuInflater inflater = tempMenu.getMenuInflater();
                inflater.inflate(R.menu.build_module_menu, tempMenu.getMenu());
                createInfoForView(tempMenu.getMenu(), info);
                tempMenu.show();
                return true;
            }
        });
    }*/

    public static void addInfoOnLongClick(Button button, final String[] info) {
        //System.out.println(button.getText());
        if (!button.getText().toString().contains("(i)")) {
            button.setText(button.getText() + " (i)");
            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu tempMenu = new PopupMenu(mActivity, v);
                    MenuInflater inflater = tempMenu.getMenuInflater();
                    inflater.inflate(R.menu.build_module_menu, tempMenu.getMenu());
                    createInfoForView(tempMenu.getMenu(), info);
                    tempMenu.show();
                    return true;
                }
            });
        }
    }

    private static boolean createInfoForView(Menu menu, String[] info) {
        for (int i = 0; i < info.length; i++)
            menu.add(Menu.NONE, i, Menu.NONE, info[i]);
        return true;
    }

}
