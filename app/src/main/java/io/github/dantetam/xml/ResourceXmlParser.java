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

import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.entity.UnitTree;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class ResourceXmlParser {
    private static final String ns = null;

    //TODO: Impl. this class.

    public static void parseResourceTree(TechTree tree, Context context, int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        try {
            parseResourceTree(tree, inputStream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseResourceTree(TechTree tree, InputStream inputStream)
            throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(inputStream, null);

        //HashMap<String, String> addRequirementsNames = new HashMap<>();
        int eventType = xpp.getEventType();

        if (tree.itemTypes == null) {
            tree.itemTypes = new HashMap<>();
        }

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                //System.out.println("Start tag " + xpp.getName());
                if (xpp.getName().equals("resource")) {
                    String resourceName = xpp.getAttributeValue(null, "name");

                    String normalStatsStringy = xpp.getAttributeValue(null, "yield");
                    String[] splitNormalStats = normalStatsStringy.split("/");
                    int[] normalStats = new int[splitNormalStats.length];
                    for (int i = 0; i < normalStats.length; i++) {
                        normalStats[i] = Integer.parseInt(splitNormalStats[i]);
                    }

                    ItemType itemType = new ItemType(resourceName,
                            normalStats[0], normalStats[1], normalStats[2], normalStats[3], normalStats[4], normalStats[5]);

                    String modelName = xpp.getAttributeValue(null, "model");
                    if (modelName != null) {
                        //TODO: Fix this so it randomly splits models
                        if (modelName.contains("/")) {
                            modelName = modelName.split("/")[0];
                        }
                        itemType.modelName = modelName;
                    }
                    String textureName = xpp.getAttributeValue(null, "texture");
                    if (textureName != null) {
                        //TODO: Fix this so it randomly splits textures
                        if (textureName.contains("/")) {
                            textureName = textureName.split("/")[0];
                        }
                        itemType.textureName = textureName;
                    }

                    tree.itemTypes.put(resourceName, itemType);
                }
            } else if (eventType == XmlPullParser.END_TAG) {

            } else if (eventType == XmlPullParser.TEXT) {

            }
            eventType = xpp.next();
        }
    }

}
