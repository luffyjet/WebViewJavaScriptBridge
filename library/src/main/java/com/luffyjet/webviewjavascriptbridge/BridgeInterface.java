package com.luffyjet.webviewjavascriptbridge;

import android.app.Activity;
import android.content.Intent;

import java.util.concurrent.ExecutorService;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/20
 * Project : branch_1009
 * Site : http://www.luffyjet.com
 */

public interface BridgeInterface {
    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits,
     * your onActivityResult() method will be called.
     *
     * @param command     The command object
     * @param intent      The intent to start
     * @param requestCode   The request code that is passed to callback to identify the activity
     */
    abstract public void startActivityForResult(RequestHandler command, Intent intent, int requestCode);

    /**
     * Set the plugin to be called when a sub-activity exits.
     *
     * @param plugin      The plugin on which onActivityResult is to be called
     */
    abstract public void setActivityResultCallback(RequestHandler plugin);

    /**
     * Get the Android activity.
     *
     * @return the Activity
     */
    public abstract Activity getActivity();


    /**
     * Returns a shared thread pool that can be used for background tasks.
     */
    public ExecutorService getThreadPool();
}
