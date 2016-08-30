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
 */
public class InfoHelper {

    private LessonSevenActivity mActivity;

    public InfoHelper(LessonSevenActivity activity) {
        mActivity = activity;
    }

    public void addInfo(int viewId, String[] info) {
        View view = mActivity.findViewById(viewId);
        if (view instanceof MenuItem) {
            MenuItem item = ((MenuItem) view);
            item.setTitle(item.getTitle() + " (i)");
        }
        else if (view instanceof Button) {
            Button button = ((Button) view);
            button.setText(button.getText() + " (i)");
        }
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu tempMenu = new PopupMenu(mActivity, v);
                MenuInflater inflater = tempMenu.getMenuInflater();
                inflater.inflate(R.menu.build_module_menu, tempMenu.getMenu());
                onCreateBuildModuleMenu(tempMenu.getMenu());
                tempMenu.show();
                return true;
            }
        });
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

                    String displayName = buildingType.name + " " + yieldString;
                    MenuItem menuItem = improvementSubMenu.add(Menu.NONE, i + 1, Menu.NONE, displayName);
                    menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            Building newBuilding = BuildingFactory.newModule(selectedImprovement.world, selectedImprovement.clan, buildingType, selected, 0, selectedImprovement);
                            newBuilding.actionsQueue.clear();
                            newBuilding.actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, newBuilding));
                            return false;
                        }
                    });
                    i++;
                }

                int[] yield = (int[]) (city.gameYield()[0]);

                SubMenu unitSubMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Build unit");
                Set<PersonType> allowedPeople = selectedImprovement.clan.techTree.allowedUnits.keySet();
                for (final PersonType personType : allowedPeople) {
                    //System.out.println(personType.name + " " + allowedPeople.size());
                    String stringy = personType.name;
                    int turnsCalculated = (int) Math.ceil((double) personType.workNeeded / (double) yield[1]);
                    stringy += " " + turnsCalculated + " turns";
                    MenuItem menuItem = unitSubMenu.add(Menu.NONE, 0, Menu.NONE, stringy);
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

}
