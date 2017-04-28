package com.luffyjet.jsbridgeexample.handlers;

import com.luffyjet.jsbridgeexample.ImagesResult;
import com.luffyjet.webviewjavascriptbridge.RequestHandler;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridgeBase;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/16
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class ImageChooseHandler extends RequestHandler {
    @Override
    public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        log(getHandlerName(), data.toString());
        ImagesResult jsonObject = new ImagesResult(true);
        ArrayList<String> list = new ArrayList<>();
        list.add("ccplus:///storage/emulated/0/Pictures/netease/newsreader/BT27QS9600AJ0003.jpg");
        jsonObject.localIds = list;
        responseCallback.callback(jsonObject.toJSON());
    }
}
