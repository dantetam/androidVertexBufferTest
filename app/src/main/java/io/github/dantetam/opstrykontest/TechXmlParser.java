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
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.BuildingType;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.ItemType;
import io.github.dantetam.world.Person;
import io.github.dantetam.world.Tech;
import io.github.dantetam.world.TechTree;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class TechXmlParser {
    private static final String ns = null;

    public static TechTree parseTest(Clan clan, Context context, int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        try {
            return parseTest(clan, inputStream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    This method parses an XML document, line by line. It individually searches tags,
    where <techroot>...</techroot> marks the first technology, and all its children
    are future techs linked to the parent tech.

    This builds the tech tree with the help of a stack, where a <tech> tag
    pushes a new tech to the stack and sets the parent if it exists, and a </tech>
    tag pops a tech off the stack. The stackCounter int represents distance from
    the tech root, where -1 indicates no tech has been parsed.
     */
    public static TechTree parseTest(Clan clan, InputStream inputStream)
            throws XmlPullParserException, IOException {
        TechTree tree = new TechTree(clan);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        //xpp.setInput( new StringReader ( "<foo>Hello World!</foo>" ) );
        //xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        xpp.setInput(inputStream, null);
        int stackCounter = -1;
        List<Tech> stack = new ArrayList<>();
        HashMap<String, Tech> techMap = new HashMap<>();
        HashMap<String, String> addRequirementsNames = new HashMap<>();
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                //System.out.println("Start tag " + xpp.getName());
                if (xpp.getName().equals("tech") || xpp.getName().equals("techroot")) {
                    String techName = xpp.getAttributeValue(null, "name");
                    int workNeeded = Integer.parseInt(xpp.getAttributeValue(null, "workNeeded"));
                    //System.out.println(techName + " " + workNeeded);
                    Tech newTech = new Tech(techName, 0, workNeeded);
                    if (xpp.getName().equals("techroot")) {
                        tree.root = newTech;
                    }
                    stack.add(newTech);
                    if (stackCounter >= 0) {
                        stack.get(stackCounter).unlockedTechs.add(newTech);
                    }
                    stackCounter++;

                    String unlockBuilding = xpp.getAttributeValue(null, "building");
                    String unlockResource = xpp.getAttributeValue(null, "resource");
                    String unlockUnit = xpp.getAttributeValue(null, "unit");
                    String unlockSpecialAbility = xpp.getAttributeValue(null, "specialAbility");

                    if (unlockBuilding != null) {
                        newTech.unlockedBuildings.add(BuildingType.fromString(unlockBuilding));
                    }
                    if (unlockResource != null) {
                        newTech.harvestableResources.add(ItemType.fromString(unlockResource));
                    }
                    if (unlockUnit != null) {
                        newTech.unlockedUnits.add(Person.PersonType.fromString(unlockUnit));
                    }
                    if (unlockSpecialAbility != null) {
                        newTech.unlockedSpecialAbilities.add(unlockSpecialAbility);
                    }

                    techMap.put(techName, newTech);
                    //A forward declaration for requirements?
                    String extraRequirement = xpp.getAttributeValue(null, "requirement");
                    if (extraRequirement != null) {
                        addRequirementsNames.put(techName, extraRequirement);
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                //System.out.println("End tag " + xpp.getName());
                if (xpp.getName().equals("tech") || xpp.getName().equals("techroot")) {
                    stack.remove(stack.size() - 1);
                    stackCounter--;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                //System.out.println("Text "+xpp.getText());
            }
            eventType = xpp.next();
        }

        for (Map.Entry<String, String> entry: addRequirementsNames.entrySet()) {
            Tech subject = techMap.get(entry.getKey());
            Tech requirement = techMap.get(entry.getValue());
            subject.extraReqs.add(requirement);
        }
        //System.out.println("End document");
        return tree;
    }

}
