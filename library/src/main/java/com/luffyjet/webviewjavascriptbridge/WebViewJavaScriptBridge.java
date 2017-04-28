package com.luffyjet.webviewjavascriptbridge;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;


/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/14
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class WebViewJavaScriptBridge extends WebViewClient implements WebViewJavaScriptBridgeBase.WebViewJavascriptBridgeBaseDelegate {

    private static final String TAG = WebViewJavaScriptBridge.class.getSimpleName();
    private WeakReference<WebView> _webView;
    private WeakReference<WebViewClient> _webViewDelegate;
    private WebViewJavaScriptBridgeBase _base;
    public static final String LOCAL_FILE_SCHEMA = "wjbfile://";

    private WebViewJavaScriptBridge() {

    }


    public static void enableLogging() {
        WebViewJavaScriptBridgeBase.enableLogging();
    }


    public static WebViewJavaScriptBridge bridgeForWebView(Context activity, WebView webView) {
        WebViewJavaScriptBridge bridge = new WebViewJavaScriptBridge();
        bridge.platformSpecificSetup(activity, webView);
        return bridge;
    }

    public void setWebViewDelegate(WebViewClient webViewDelegate) {
        _webViewDelegate = new WeakReference<>(webViewDelegate);
    }

    public void send(Object data) {
        send(data, null);
    }

    public void send(Object data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        _base.sendData(data, responseCallback, null);
    }

    public void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }

    public void callHandler(String handlerName, Object data) {
        callHandler(handlerName, data, null);
    }

    public void callHandler(String handlerName, Object data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        _base.sendData(data, responseCallback, handlerName);
    }

    public void registerHandler(RequestHandler handler) {
        registerHandler(handler.getHandlerName(), handler);
    }

    public void registerHandler(String handlerName, WebViewJavaScriptBridgeBase.WVJBHandler handler) {
        _base.messageHandlers.put(handlerName, handler);
    }

    public void disableJavscriptAlertBoxSafetyTimeout() {
        _base.disableJavscriptAlertBoxSafetyTimeout();
    }


    @Override
    public void evaluateJavascript(String javascriptCommand) {
        if (null != _webView && null != _webView.get()) {
            _webView.get().loadUrl(javascriptCommand);
        }
    }

    private void platformSpecificSetup(Context activity, WebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(this);

        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(activity);

        _webView = new WeakReference<>(webView);
        _base = new WebViewJavaScriptBridgeBase();
        _base.init(activity, parser.getHandlerEntries());
        _base.setDelegate(this);
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (null != _webViewDelegate && null != _webViewDelegate.get())
            _webViewDelegate.get().onPageFinished(view, url);
    }


    @Override
    public void onReceivedError(WebView webView, int i, String s, String s1) {
        super.onReceivedError(webView, i, s, s1);
        if (null != _webViewDelegate && null != _webViewDelegate.get())
            _webViewDelegate.get().onReceivedError(webView, i, s, s1);
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (null != _webViewDelegate && null != _webViewDelegate.get())
            _webViewDelegate.get().onPageStarted(view, url, favicon);
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        if (_base.isCorrectProcotocolScheme(uri)) {
            if (_base.isBridgeLoadedURL(uri)) {
                _base.injectJavascriptFile(view.getContext());
            } else if (_base.isQueueMessageURL(uri)) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    evaluateJavascript(_base.webViewJavascriptFetchQueyCommand());
                    _base.responseCallbacks.put(WebViewJavaScriptBridgeBase.QUEUE_HAS_MESSAGE, new WebViewJavaScriptBridgeBase.WVJBResponseCallback() {
                        @Override
                        public void callback(String responseData) {
                            _base.flushMessageQueue(responseData);
                        }
                    });
                }
            } else if (_base.isReturnMessageURL(uri)) {
                String data = uri.toString().replace(WebViewJavaScriptBridgeBase.QUEUE_FETCH_MESSAGE, "");
                WebViewJavaScriptBridgeBase.WVJBResponseCallback callback = _base.responseCallbacks.get(WebViewJavaScriptBridgeBase.QUEUE_HAS_MESSAGE);
                if (null != callback) {
                    callback.callback(data);
                    _base.responseCallbacks.remove(WebViewJavaScriptBridgeBase.QUEUE_HAS_MESSAGE);
                }
            } else {
                _base.logUnkownMessage(uri);
            }
            return true;
        } else {
            if (null != _webViewDelegate && null != _webViewDelegate.get()) {
                WebViewClient webViewClient = _webViewDelegate.get();
                return webViewClient.shouldOverrideUrlLoading(view, url);
            }
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        String url = null != webResourceRequest.getUrl() ? webResourceRequest.getUrl().toString() : null;
        if (!TextUtils.isEmpty(url) && url.startsWith(LOCAL_FILE_SCHEMA)) {
            return getLocalResource(url);
        }

        if (null != _webViewDelegate && null != _webViewDelegate.get()) {
            return _webViewDelegate.get().shouldInterceptRequest(webView, webResourceRequest);
        }
        return super.shouldInterceptRequest(webView, webResourceRequest);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith(LOCAL_FILE_SCHEMA)) {
            return getLocalResource(url);
        }

        if (null != _webViewDelegate && null != _webViewDelegate.get()) {
            return _webViewDelegate.get().shouldInterceptRequest(view, url);
        }
        return super.shouldInterceptRequest(view, url);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private WebResourceResponse getLocalResource(String url) {
        WebResourceResponse response = null;
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        File file = new File(url.replace(LOCAL_FILE_SCHEMA, ""));
        if (file.exists()) {
            try {
                InputStream localCopy = new FileInputStream(file);
                String mimeType = getMimeType(url);
                response = new WebResourceResponse(mimeType, "UTF-8", localCopy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("shouldInterceptRequest", "file is not exists!");
        }
        return response;
    }

    private String getMimeType(String url) {
        if (url.contains(".")) {
            int index = url.lastIndexOf(".");
            if (index > -1) {
                int paramIndex = url.indexOf("?");
                String type = url.substring(index + 1, paramIndex == -1 ? url.length() : paramIndex);
                switch (type) {
                    case "js":
                        return "text/javascript";
                    case "css":
                        return "text/css";
                    case "html":
                        return "text/html";
                    case "png":
                        return "image/png";
                    case "jpg":
                        return "image/jpg";
                    case "gif":
                        return "image/gif";
                }
            }
        }
        return "text/plain";
    }

}
