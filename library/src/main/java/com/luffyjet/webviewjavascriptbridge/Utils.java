package com.luffyjet.webviewjavascriptbridge;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/15
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class Utils {

    static String getLocalJs(Context context, String path) {
        String js = assets2Str(context, path);
        return "javascript:" + js;
    }


    private static String assets2Str(Context c, String urlStr) {
        InputStream in = null;
        try {
            in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                sb.append(line);
            } while (line != null);

            bufferedReader.close();
            in.close();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
