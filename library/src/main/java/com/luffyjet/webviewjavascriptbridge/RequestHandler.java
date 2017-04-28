package com.luffyjet.webviewjavascriptbridge;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/16
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public abstract class RequestHandler implements WebViewJavaScriptBridgeBase.WVJBHandler {
    protected BridgeInterface mBridgeInterface;
    protected Context mContext;
    private String service;

    public RequestHandler() {
    }

    public void log(String msg) {
        if (WebViewJavaScriptBridgeBase.logging) {
            Log.d("RequestHandler", msg);
        }
    }


    public void log(String tag, String msg) {
        if (WebViewJavaScriptBridgeBase.logging) {
            Log.d(tag, msg);
        }
    }

    protected String getHandlerName() {
        return service;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

    }

    public void onPayResult(String result, String message){

    }

    void privateInitialize(String service, Context ctx , BridgeInterface bridgeInterface) {
        this.service = service;
        mContext = ctx;
        mBridgeInterface = bridgeInterface;
    }
}
