/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package com.luffyjet.webviewjavascriptbridge;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class ConfigXmlParser {
    private ArrayList<HandlerEntry> pluginEntries = new ArrayList<HandlerEntry>();
    boolean insideFeature = false;
    String service = "", pluginClass = "", paramType = "";
    boolean onload = false;

    public ArrayList<HandlerEntry> getHandlerEntries() {
        return pluginEntries;
    }

    public void parse(Context action) {
        // First checking the class namespace for config.xml
        int id = action.getResources().getIdentifier("ccpconfig", "xml", action.getClass().getPackage().getName());
        if (id == 0) {
            // If we couldn't find config.xml there, we'll look in the namespace from AndroidManifest.xml
            id = action.getResources().getIdentifier("ccpconfig", "xml", action.getPackageName());
            if (id == 0) {
                return;
            }
        }
        parse(action.getResources().getXml(id));
    }

    public void parse(XmlPullParser xml) {
        int eventType = -1;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                handleStartTag(xml);
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                handleEndTag(xml);
            }
            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleStartTag(XmlPullParser xml) {
        String strNode = xml.getName();
        if (strNode.equals("feature")) {
            //Check for supported feature sets  aka. plugins (Accelerometer, Geolocation, etc)
            //Set the bit for reading params
            insideFeature = true;
            service = xml.getAttributeValue(null, "name");
        }
        else if (insideFeature && strNode.equals("param")) {
            paramType = xml.getAttributeValue(null, "name");
            if (paramType.equals("service")) // check if it is using the older service param
                service = xml.getAttributeValue(null, "value");
            else if (paramType.equals("package") || paramType.equals("android-package"))
                pluginClass = xml.getAttributeValue(null,"value");
            else if (paramType.equals("onload"))
                onload = "true".equals(xml.getAttributeValue(null, "value"));
        }
    }

    public void handleEndTag(XmlPullParser xml) {
        String strNode = xml.getName();
        if (strNode.equals("feature")) {
            pluginEntries.add(new HandlerEntry(service, pluginClass, onload));

            service = "";
            pluginClass = "";
            insideFeature = false;
            onload = false;
        }
    }
}
