package com.luffyjet.jsbridgeexample.handlers;

import com.luffyjet.webviewjavascriptbridge.RequestHandler;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridgeBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/16
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class ScanQRCodeHandler extends RequestHandler {

    @Override
    public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        log(getHandlerName(), data.toString());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("resStr","content://sss");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        responseCallback.callback(jsonObject.toString());
    }
}
