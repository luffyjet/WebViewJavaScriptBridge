package com.luffyjet.webviewjavascriptbridge;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2017/4/27
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class JSResult {
    public boolean status;
    public String resultStr;
    public String errorMessage;

    public JSResult() {
    }

    public JSResult(boolean result) {
        status = result;
    }

    public JSResult(String result) {
        resultStr = result;
        status = true;
    }

    public JSResult setErrorMessage(String message) {
        errorMessage = message;
        return this;
    }


    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", status);
            jsonObject.put("resultStr", resultStr);
            jsonObject.put("errorMessage", errorMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
