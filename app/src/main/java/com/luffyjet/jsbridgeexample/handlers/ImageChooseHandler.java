package com.luffyjet.jsbridgeexample.handlers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.luffyjet.jsbridgeexample.ImagesResult;
import com.luffyjet.jsbridgeexample.PathUtil;
import com.luffyjet.webviewjavascriptbridge.RequestHandler;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridge;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridgeBase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/16
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class ImageChooseHandler extends RequestHandler {
    private static final String TAG = "ImageChooseHandler";
    private static final int REQUEST_CODE_IMAGES = 100;
    private WebViewJavaScriptBridgeBase.WVJBResponseCallback mResponseCallback;

    @Override
    public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        log(getHandlerName(), data.toString());
        mResponseCallback = responseCallback;

        int count = 0;

        try {
            count = data.optInt("count");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count == 0) {
            count = 9;
        }

        //这里只选取一张，真实情况下，可以通过自定义的选图界面，选择复数的图片，并返回给JS
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        mBridgeInterface.startActivityForResult(this, Intent.createChooser(i, "选取图片"), REQUEST_CODE_IMAGES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGES) {
            Log.i(TAG, "onActivityResult: " + data.getData());
            try {
                //根据Uri 获取真实路径
                String path = PathUtil.getImageAbsolutePath(mBridgeInterface.getActivity(), data.getData());
                Log.i(TAG, "onActivityResult: " + path);
                ImagesResult result = new ImagesResult(true);
                List<String> ids = new ArrayList<>();
                //在真实路径之前添加特殊SCHEMA，在WebViewClient.shouldInterceptRequest方法里做资源替换，将图片显示出来
                //具体可查看WebViewJavaScriptBridge的源码
                ids.add(WebViewJavaScriptBridge.LOCAL_FILE_SCHEMA + path);
                result.localIds = ids;
                mResponseCallback.callback(result.toJSON());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
