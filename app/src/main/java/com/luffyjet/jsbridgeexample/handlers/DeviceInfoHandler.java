package com.luffyjet.jsbridgeexample.handlers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.luffyjet.jsbridgeexample.NetworkStauts;
import com.luffyjet.jsbridgeexample.R;
import com.luffyjet.webviewjavascriptbridge.RequestHandler;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridgeBase;

import org.json.JSONObject;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/19
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class DeviceInfoHandler extends RequestHandler {

    @Override
    public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        log(getHandlerName(), data.toString());
        responseCallback.callback(new DeviceInfo().toJson(mContext));
    }


    private static class DeviceInfo {

        String appName;//应用名称
        String appVersion;//CC+版本号
        String systemType;//系统类型
        String systemVersion;//版本
        String version;//JS桥接引擎版本
        String deviceModel;//设备型号，字符串类型
        String deviceName;//设备名称，字符串类型
        String operator;//运营商名称，若未获取到则返回none，字符串类型

        /**
         * 网络类型，字符串类型
         *
         * unknown            //未知
         * wifi            //wifi
         * 2g                //2G网络
         * 3g                //3G网络
         * 4g                //4G网络
         * none            //无网络
         */
        public String connectionType;//


        String toJson(Context context) {
            try {
                appName = context.getString(R.string.app_name);
                systemType = "Android";
                systemVersion = Build.VERSION.RELEASE;
                version = "1.0";
                deviceModel = Build.MODEL;
                deviceName = Build.PRODUCT;

                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                appVersion = pi.versionName;

                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                operator = telephonyManager.getSimOperatorName();

                connectionType = NetworkStauts.getCurrentNetworkType(context);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return new Gson().toJson(this);
        }
    }
}
