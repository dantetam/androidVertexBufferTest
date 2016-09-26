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
import io.github.dantetam.world.entity.IdeologyTree;
import io.github.dantetam.world.entity.Tech;

/**
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class IdeologyXmlParser {
    private static final String ns = null;

    public static IdeologyTree parseAllClans(Context context, int resourceId) {
        final InputStream clanStream = context.getResources().openRawResource(
                resourceId);
        try {
            return parseIdeologyTree(clanStream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static IdeologyTree parseIdeologyTree(InputStream inputStream)
            throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        //xpp.setInput( new StringReader ( "<foo>Hello World!</foo>" ) );
        //xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        xpp.setInput(inputStream, null);
        int stackCounter = -1;
        List<Tech> stack = new ArrayList<>();

        IdeologyTree tree = new IdeologyTree();

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
                //System.out.println("Start tag " + xpp.getName());

            } else if (eventType == XmlPullParser.END_TAG) {
                //System.out.println("End tag " + xpp.getName());

            } else if (eventType == XmlPullParser.TEXT) {
                //System.out.println("Text "+xpp.getText());
            }
            eventType = xpp.next();
        }

        return tree;
    }

}
