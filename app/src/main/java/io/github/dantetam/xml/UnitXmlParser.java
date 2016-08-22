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
import java.util.List;

import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.UnitTree;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class UnitXmlParser {
    private static final String ns = null;

    public static UnitTree parseUnitTree(Clan clan, Context context, int resourceId) {
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

    public static UnitTree parseUnitTree(Clan clan, InputStream inputStream)
            throws XmlPullParserException, IOException {
        UnitTree tree = new UnitTree(clan);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(inputStream, null);
        int stackCounter = -1;
        List<UnitTree.Unit> stack = new ArrayList<>();

        //HashMap<String, String> addRequirementsNames = new HashMap<>();
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                //System.out.println("Start tag " + xpp.getName());
                if (xpp.getName().equals("unit") || xpp.getName().equals("unitroot")) {
                    String unitName = xpp.getAttributeValue(null, "name");

                    String combatStatsStringy = xpp.getAttributeValue(null, "combatStats");
                    String[] splitCombatStats = combatStatsStringy.split("/");
                    int[] combatStats = new int[splitCombatStats.length];
                    for (int i = 0; i < combatStats.length; i++) {
                        combatStats[i] = Integer.parseInt(splitCombatStats[i]);
                    }

                    String normalStatsStringy = xpp.getAttributeValue(null, "normalStats");
                    String[] splitNormalStats = normalStatsStringy.split("/");
                    int[] normalStats = new int[splitNormalStats.length];
                    for (int i = 0; i < normalStats.length; i++) {
                        normalStats[i] = Integer.parseInt(splitNormalStats[i]);
                    }

                    int workNeeded = Integer.parseInt(xpp.getAttributeValue(null, "workNeeded"));
                    //System.out.println(techName + " " + workNeeded);
                    PersonType personType = new PersonType(unitName,
                            normalStats[0], normalStats[0], normalStats[1], normalStats[1], //normalStats[2], normalStats[3],
                            combatStats[0], combatStats[1], combatStats[2], combatStats[3], combatStats[4]);
                    personType.workNeeded = workNeeded;

                    UnitTree.Unit newUnit = new UnitTree.Unit(personType);
                    if (xpp.getName().equals("unitroot")) {
                        tree.root = newUnit;
                    }
                    stack.add(newUnit);
                    if (stackCounter >= 0) {
                        stack.get(stackCounter).unlockedUnits.add(newUnit);
                    }
                    stackCounter++;

                    String techNeeded = xpp.getAttributeValue(null, "tech"); //TODO: Use this data

                    String resourceNeeded = xpp.getAttributeValue(null, "resource");
                    if (resourceNeeded != null) {
                        personType.resourceNeeded = resourceNeeded;
                    }

                    tree.personTypes.put(unitName, personType);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                //System.out.println("End tag " + xpp.getName());
                if (xpp.getName().equals("unit") || xpp.getName().equals("unitroot")) {
                    stackCounter--;
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
