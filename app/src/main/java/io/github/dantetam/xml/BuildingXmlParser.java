/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.github.dantetam.xml;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.entity.BuildingTree;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.entity.UnitTree;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class BuildingXmlParser {
    private static final String ns = null;

    public static HashMap<String, BuildingType> globalAllTypes;

    public static BuildingTree parseBuildingTree(Clan clan, Context context, int resourceId) {
        if (globalAllTypes == null) {
            globalAllTypes = new HashMap<>();
        }
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        try {
            return parseUnitTree(clan, inputStream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BuildingTree parseUnitTree(Clan clan, InputStream inputStream)
            throws XmlPullParserException, IOException {
        BuildingTree tree = new BuildingTree(clan);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(inputStream, null);
        int stackCounter = -1;
        List<UnitTree.Unit> stack = new ArrayList<>();

        HashMap<String, String> addRequirementsNames = new HashMap<>();
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                //System.out.println("Start tag " + xpp.getName());
                if (xpp.getName().equals("impr")) {
                    String buildingName = xpp.getAttributeValue(null, "name");

                    /*String combatStatsStringy = xpp.getAttributeValue(null, "combatStats");
                    String[] splitCombatStats = combatStatsStringy.split("/");
                    int[] combatStats = new int[splitCombatStats.length];
                    for (int i = 0; i < combatStats.length; i++) {
                        combatStats[i] = Integer.parseInt(splitCombatStats[i]);
                    }*/

                    String yieldStatsStringy = xpp.getAttributeValue(null, "yield");
                    String[] splitYieldStats = yieldStatsStringy.split("/");
                    int[] yieldStats = new int[splitYieldStats.length];
                    for (int i = 0; i < yieldStats.length; i++) {
                        try {
                            yieldStats[i] = Integer.parseInt(splitYieldStats[i]);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            yieldStats[i] = 0;
                        }
                    }

                    int workNeeded = Integer.parseInt(xpp.getAttributeValue(null, "workNeeded"));

                    BuildingType buildingType = new BuildingType(buildingName,
                            new int[]{yieldStats[0], yieldStats[1], yieldStats[2], yieldStats[3], yieldStats[4], yieldStats[5], yieldStats[6]});
                    buildingType.workNeeded = workNeeded;

                    String resourceNeeded = xpp.getAttributeValue(null, "resource");
                    if (resourceNeeded != null) {
                        buildingType.resourceNeeded = resourceNeeded;
                    }

                    String modelName = xpp.getAttributeValue(null, "model");
                    if (modelName != null) {
                        //TODO: Fix this so it randomly splits models
                        if (modelName.contains("/")) {
                            modelName = modelName.split("/")[0];
                        }
                        buildingType.modelName = modelName;
                    }
                    String textureName = xpp.getAttributeValue(null, "texture");
                    if (textureName != null) {
                        //TODO: Fix this so it randomly splits textures
                        if (textureName.contains("/")) {
                            textureName = textureName.split("/")[0];
                        }
                        buildingType.textureName = textureName;
                    }

                    String isWonder = xpp.getAttributeValue(null, "wonder");
                    if (isWonder != null) {
                        buildingType.wonder = true;
                        TechTree.wonders.put(buildingType, true);
                    }

                    tree.buildingTypes.put(buildingName, buildingType);
                    globalAllTypes.put(buildingName, buildingType);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                //System.out.println("End tag " + xpp.getName());
                if (xpp.getName().equals("impr")) {

                }
            } else if (eventType == XmlPullParser.TEXT) {
                //System.out.println("Text "+xpp.getText());
            }
            eventType = xpp.next();
        }

        //System.out.println("End document");
        return tree;
    }

}
