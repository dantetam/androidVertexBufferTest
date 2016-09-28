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

import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Tech;
import io.github.dantetam.world.entity.World;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class ClanXmlParser {
    private static final String ns = null;

    public HashMap<String, Clan> clans;
    public HashMap<String, CityState> cityStates;
    public String[] clanKeys;
    public String[] cityStateKeys;

    public void parseAllClans(Context context, int resourceId) {
        final InputStream clanStream = context.getResources().openRawResource(
                resourceId);
        try {
            parseAllClans(clanStream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseAllClans(InputStream inputStream)
            throws XmlPullParserException, IOException {
        clans = new HashMap<>();
        cityStates = new HashMap<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        //xpp.setInput( new StringReader ( "<foo>Hello World!</foo>" ) );
        //xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        xpp.setInput(inputStream, null);
        int stackCounter = -1;
        List<Tech> stack = new ArrayList<>();

        Clan inspect = null;

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                //System.out.println("Start tag " + xpp.getName());
                String startTag = xpp.getName();
                if (startTag.equals("clan")) {
                    //System.out.println("Start tag " + xpp.getName());

                    String clanName = xpp.getAttributeValue(null, "name");
                    inspect = new Clan(clanName);
                    clans.put(clanName, inspect);

                    String adjective = xpp.getAttributeValue(null, "adjective");
                    inspect.adjective = adjective;

                    String leaderName = xpp.getAttributeValue(null, "ruler");
                    inspect.ai.leaderName = leaderName;
                }
                else if (startTag.equals("citynames")) {
                    inspect.cityNames = new ArrayList<>();
                }
                else if (startTag.equals("cityname")) {
                    String name = xpp.getAttributeValue(null, "name");
                    inspect.cityNames.add(name);
                }
                else if (startTag.equals("citystate")) {
                    String clanName = xpp.getAttributeValue(null, "name");
                    inspect = new CityState(clanName);
                    cityStates.put(clanName, (CityState) inspect);

                    String adjective = xpp.getAttributeValue(null, "adjective");
                    inspect.adjective = adjective;

                    String leaderName = xpp.getAttributeValue(null, "ruler");
                    inspect.ai.leaderName = leaderName;

                    inspect.cityNames = new ArrayList<>();
                    inspect.cityNames.add(clanName);
                }
                else if (startTag.equals("ability")) {
                    if (inspect.ai.abilityOne == null) {
                        inspect.ai.abilityOne = xpp.getAttributeValue(null, "name");
                    }
                    else if (inspect.ai.abilityTwo == null) {
                        inspect.ai.abilityTwo = xpp.getAttributeValue(null, "name");
                    }
                }
                else if (startTag.equals("personalityflavor")) {
                    inspect.ai.personality.put(xpp.getAttributeValue(null, "name"), Integer.parseInt(xpp.getAttributeValue(null, "value")));
                }
                else if (startTag.equals("strategyflavor")) {
                    inspect.ai.strategy.put(xpp.getAttributeValue(null, "name"), Integer.parseInt(xpp.getAttributeValue(null, "value")));
                }
                else if (startTag.equals("tacticsflavor")) {
                    inspect.ai.tactics.put(xpp.getAttributeValue(null, "name"), Integer.parseInt(xpp.getAttributeValue(null, "value")));
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                //System.out.println("End tag " + xpp.getName());
                if (xpp.getName().equals("clan")) {
                    inspect = null;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                //System.out.println("Text "+xpp.getText());
            }
            eventType = xpp.next();
        }

        clanKeys = new String[clans.keySet().size()];
        clanKeys = clans.keySet().toArray(clanKeys);

        cityStateKeys = new String[cityStates.keySet().size()];
        cityStateKeys = cityStates.keySet().toArray(cityStateKeys);
    }

}
