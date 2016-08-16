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

package io.github.dantetam.opstrykontest;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.BuildingTree;
import io.github.dantetam.world.BuildingType;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.PersonType;
import io.github.dantetam.world.UnitTree;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class BuildingXmlParser {
    private static final String ns = null;

    public static BuildingTree parseBuildingTree(Clan clan, Context context, int resourceId) {
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
                if (xpp.getName().equals("unit")) {
                    String buildingName = xpp.getAttributeValue(null, "name");

                    String combatStatsStringy = xpp.getAttributeValue(null, "combatStats");
                    String[] splitCombatStats = combatStatsStringy.split("/");
                    int[] combatStats = new int[splitCombatStats.length];
                    for (int i = 0; i < combatStats.length; i++) {
                        combatStats[i] = Integer.parseInt(splitCombatStats[i]);
                    }

                    String yieldStatsStringy = xpp.getAttributeValue(null, "yield");
                    String[] splitYieldStats = yieldStatsStringy.split("/");
                    int[] yieldStats = new int[splitYieldStats.length];
                    for (int i = 0; i < yieldStats.length; i++) {
                        yieldStats[i] = Integer.parseInt(splitCombatStats[i]);
                    }

                    int workNeeded = Integer.parseInt(xpp.getAttributeValue(null, "workNeeded"));

                    BuildingType buildingType = new BuildingType(buildingName,
                            yieldStats[0], yieldStats[1], yieldStats[2], yieldStats[3], yieldStats[4], yieldStats[5]);
                    buildingType.workNeeded = workNeeded;
                    tree.buildingTypes.put(buildingName, buildingType);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                //System.out.println("End tag " + xpp.getName());
                if (xpp.getName().equals("unit")) {

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
