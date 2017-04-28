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
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 *
 * Manager Hanlders
 *
 */
public class HandlerManager {
    private static final String TAG = HandlerManager.class.getSimpleName();

    private static final int SLOW_EXEC_WARNING_THRESHOLD = Debug.isDebuggerConnected() ? 60 : 16;

    // List of service entries
    private final LinkedHashMap<String, RequestHandler> pluginMap = new LinkedHashMap<String, RequestHandler>();
    private final LinkedHashMap<String, HandlerEntry> entryMap = new LinkedHashMap<String, HandlerEntry>();

    private final Context ctx;
    protected BridgeInterface mBridgeInterface;
    private boolean isInitialized;

    public HandlerManager(Context context, Collection<HandlerEntry> pluginEntries) {
        this.ctx = context;
        if (context instanceof BridgeInterface) {
            mBridgeInterface = (BridgeInterface) context;
        }
        setPluginEntries(pluginEntries);
    }

    public Collection<HandlerEntry> getPluginEntries() {
        return entryMap.values();
    }

    public void setPluginEntries(Collection<HandlerEntry> pluginEntries) {
        if (isInitialized) {
            pluginMap.clear();
            entryMap.clear();
        }
        for (HandlerEntry entry : pluginEntries) {
            addService(entry);
        }
        if (isInitialized) {
            startupPlugins();
        }
    }

    /**
     * Init when loading a new HTML page into webview.
     */
    public void init() {
        isInitialized = true;
        pluginMap.clear();
        this.startupPlugins();
    }

    /**
     * Create plugins objects that have onload set.
     */
    private void startupPlugins() {
        for (HandlerEntry entry : entryMap.values()) {
            // Add a null entry to for each non-startup plugin to avoid ConcurrentModificationException
            // When iterating plugins.
            if (entry.onload) {
                getPlugin(entry.service);
            } else {
                pluginMap.put(entry.service, null);
            }
        }
    }


    public void exec(final String service, final String action, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        RequestHandler plugin = getPlugin(service);
        if (plugin == null) {
            Log.i(TAG, "exec() call to unknown plugin: " + service);
//            if (null != responseCallback) {
//                responseCallback.callback(new JSResult(false).setErrorMessage("no RequestHandler for this request").toJson());
//            }
            return;
        }

        try {
            long pluginStartTime = System.currentTimeMillis();

            plugin.handle(!TextUtils.isEmpty(action) ? new JSONObject(action) : new JSONObject(), responseCallback);

            long duration = System.currentTimeMillis() - pluginStartTime;

            if (duration > SLOW_EXEC_WARNING_THRESHOLD) {
                Log.w(TAG, "THREAD WARNING: exec() call to " + service + "." + action + " blocked the main thread for " + duration + "ms. Plugin should use CordovaInterface.getThreadPool().");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the plugin object that implements the service.
     * If the plugin object does not already exist, then create it.
     * If the service doesn't exist, then return null.
     *
     * @param service       The name of the service.
     * @return CordovaPlugin or null
     */
    public RequestHandler getPlugin(String service) {
        RequestHandler ret = pluginMap.get(service);
        if (ret == null) {
            HandlerEntry pe = entryMap.get(service);
            if (pe == null) {
                return null;
            }
            if (pe.handler != null) {
                ret = pe.handler;
            } else {
                ret = instantiatePlugin(pe.handlerClass);
            }
            ret.privateInitialize(service, ctx, mBridgeInterface);
            pluginMap.put(service, ret);
        }
        return ret;
    }

    /**
     * Add a plugin class that implements a service to the service entry table.
     * This does not create the plugin object instance.
     *
     * @param service           The service name
     * @param className         The plugin class name
     */
    public void addService(String service, String className) {
        HandlerEntry entry = new HandlerEntry(service, className, false);
        this.addService(entry);
    }

    /**
     * Add a plugin class that implements a service to the service entry table.
     * This does not create the plugin object instance.
     *
     * @param entry             The plugin entry
     */
    public void addService(HandlerEntry entry) {
        this.entryMap.put(entry.service, entry);
        if (entry.handler != null) {
            entry.handler.privateInitialize(entry.service, ctx, mBridgeInterface);
            pluginMap.put(entry.service, entry.handler);
        }
    }


    /**
     * Create a plugin based on class name.
     */
    private RequestHandler instantiatePlugin(String className) {
        RequestHandler ret = null;
        try {
            Class<?> c = null;
            if ((className != null) && !("".equals(className))) {
                c = Class.forName(className);
            }
            if (c != null & RequestHandler.class.isAssignableFrom(c)) {
                ret = (RequestHandler) c.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding handler " + className + ".");
        }
        return ret;
    }
}
