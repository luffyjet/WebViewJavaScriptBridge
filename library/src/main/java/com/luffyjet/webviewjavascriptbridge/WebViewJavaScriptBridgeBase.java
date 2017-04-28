package com.luffyjet.webviewjavascriptbridge;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/13
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class WebViewJavaScriptBridgeBase {
    private static final String TAG = WebViewJavaScriptBridgeBase.class.getSimpleName();
    private static final String PROTOCOL_SCHEME = "wvjbscheme";
    static final String QUEUE_HAS_MESSAGE = "__WVJB_QUEUE_MESSAGE__";
    private static final String QUEUE_RETURN_MESSAGE = "__WVJB_RETURN_MESSAGE__";
    private static final String BRIDGE_LOADED = "__BRIDGE_LOADED__";
    static final String QUEUE_FETCH_MESSAGE = PROTOCOL_SCHEME + "://" + QUEUE_RETURN_MESSAGE + "/";

    static boolean logging = false;

    interface WebViewJavascriptBridgeBaseDelegate {
        void evaluateJavascript(String javascriptCommand);
    }

    public interface WVJBResponseCallback {
        void callback(String responseData);
    }


    public interface WVJBHandler {
        void handle(JSONObject data, WVJBResponseCallback responseCallback);
    }

    private WebViewJavascriptBridgeBaseDelegate delegate;
    private ArrayList<HashMap<String, WVJBResponseCallback>> startupMessageQueue;
    HashMap<String, WVJBResponseCallback> responseCallbacks;
    HashMap<String, WVJBHandler> messageHandlers;
    private HandlerManager mHandlerManager;
    private long _uniqueId;


    static void enableLogging() {
        logging = true;
    }


    WebViewJavaScriptBridgeBase init(Context context, List<HandlerEntry> pluginEntries) {
        this.messageHandlers = new HashMap<>();
        this.startupMessageQueue = new ArrayList<>();
        this.responseCallbacks = new HashMap<>();

        mHandlerManager = new HandlerManager(context, pluginEntries);
        mHandlerManager.init();

        _uniqueId = 0;
        return this;
    }


    void sendData(Object data, WVJBResponseCallback responseCallback, String handlerName) {
        HashMap<String, Object> message = new HashMap<>();

        if (null != data) {
            message.put("data", data);
        }

        if (null != responseCallback) {
            String callbackId = String.format("objc_cb_%s", ++_uniqueId);
            responseCallbacks.put(callbackId, responseCallback);
            message.put("callbackId", callbackId);
        }

        if (!TextUtils.isEmpty(handlerName)) {
            message.put("handlerName", handlerName);
        }

        _queueMessage(message);
    }

    void flushMessageQueue(String messageQueueString) {
        if (TextUtils.isEmpty(messageQueueString)) {
            Log.e(TAG, "WebViewJavascriptBridge: WARNING: ObjC got nil while fetching the message queue JSON from webview. This can happen if the WebViewJavascriptBridge JS is not currently present in the webview, e.g if the webview just loaded a new page.");
            return;
        }

        JSONArray messages = _deserializeMessageJSON(messageQueueString);

        if (messages != null) {
            for (int i = 0; i < messages.length(); i++) {
                try {
                    JSONObject message = messages.optJSONObject(i);

                    log("flushMessageQueue", message.toString());

                    String responseId = null;

                    responseId = message.optString("responseId");

                    if (!TextUtils.isEmpty(responseId)) {
                        WVJBResponseCallback responseCallback = responseCallbacks.get(responseId);
                        responseCallback.callback(message.optString("responseData"));
                        responseCallbacks.remove(responseId);
                    } else {
                        WVJBResponseCallback responseCallback = null;
                        final String callbackId = message.optString("callbackId");
                        if (!TextUtils.isEmpty(callbackId)) {
                            responseCallback = new WVJBResponseCallback() {
                                @Override
                                public void callback(String responseData) {
                                    HashMap<String, String> msg = new HashMap<>();
                                    msg.put("responseId", callbackId);
                                    msg.put("responseData", responseData);
                                    _queueMessage(msg);
                                }
                            };
                        } else {
                            responseCallback = new WVJBResponseCallback() {
                                @Override
                                public void callback(String responseData) {

                                }
                            };
                        }

                        mHandlerManager.exec(message.optString("handlerName"), message.optString("data"), responseCallback);


                        WVJBHandler handler = messageHandlers.get(message.optString("handlerName"));

                        if (null == handler) {
                            Log.e(TAG, String.format("WVJBNoHandlerException, No handler for message from JS: %s", message));
                            continue;
                        }

                        String action = message.optString("data");
                        handler.handle(!TextUtils.isEmpty(action) ? new JSONObject(action) : new JSONObject(), responseCallback);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    void injectJavascriptFile(Context context) {
        String js = Utils.getLocalJs(context, "webviewjsbridge.js");
        _evaluateJavascript(js);

        if (null != startupMessageQueue) {
            ArrayList<HashMap<String, WVJBResponseCallback>> queue = new ArrayList<>();
            queue.addAll(startupMessageQueue);
            startupMessageQueue = null;
            for (HashMap queuedMessage : queue) {
                _dispatchMessage(queuedMessage);
            }
        }
    }


    boolean isCorrectProcotocolScheme(Uri url) {
        return !(null == url || null == url.getScheme()) && url.getScheme().equals(PROTOCOL_SCHEME);
    }

    boolean isQueueMessageURL(Uri url) {
        return !(null == url || null == url.getHost()) && url.getHost().equals(QUEUE_HAS_MESSAGE);
    }

    boolean isReturnMessageURL(Uri url) {
        return !(null == url || null == url.getHost()) && url.getHost().equals(QUEUE_RETURN_MESSAGE);
    }

    boolean isBridgeLoadedURL(Uri url) {
        return !(null == url || null == url.getScheme() || null == url.getHost()) && url.getScheme().equals(PROTOCOL_SCHEME) && url.getHost().equals(BRIDGE_LOADED);
    }

    void logUnkownMessage(Uri url) {
        Log.i(TAG, String.format("WebViewJavascriptBridge: WARNING: Received unknown WebViewJavascriptBridge command %s://%s", PROTOCOL_SCHEME, url.getPath()));
    }

    public String webViewJavascriptCheckCommand() {
        return "javascript:typeof WebViewJavascriptBridge == \'object\';";
    }

    String webViewJavascriptFetchQueyCommand() {
        return "javascript:WebViewJavascriptBridge._fetchQueue();";
    }

    void disableJavscriptAlertBoxSafetyTimeout() {
        sendData(null, null, "_disableJavascriptAlertBoxSafetyTimeout");
    }


    private void _evaluateJavascript(String javascriptCommand) {
        delegate.evaluateJavascript(javascriptCommand);
    }


    private void _queueMessage(HashMap message) {
        if (null != startupMessageQueue) {
            startupMessageQueue.add(message);
        } else {
            _dispatchMessage(message);
        }
    }


    private void _dispatchMessage(HashMap message) {
        String messageJSON = _serializeMessage(message);


        messageJSON = messageJSON.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJSON = messageJSON.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");

        log("_dispatchMessage", messageJSON);

        final String javascriptCommand = String.format("javascript:WebViewJavascriptBridge._handleMessageFromObjC('%s');", messageJSON);

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this._evaluateJavascript(javascriptCommand);
        }
    }

    private String _serializeMessage(HashMap message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("callbackId", message.get("callbackId"));
            jsonObject.put("data", message.get("data"));
            jsonObject.put("handlerName", message.get("handlerName"));
            jsonObject.put("responseData", message.get("responseData"));
            jsonObject.put("responseId", message.get("responseId"));
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private JSONArray _deserializeMessageJSON(String messageJSON) {
        log("decode", messageJSON);
        try {
            return new JSONArray(messageJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    List<String> getApiList() {
        return new ArrayList<String>(messageHandlers.keySet());
    }


    void setDelegate(WebViewJavascriptBridgeBaseDelegate delegate) {
        this.delegate = delegate;
    }

    private void log(String action, String json) {
        if (!logging) {
            return;
        }
        Log.i(TAG, String.format("WVJB %s: %s", action, json));
    }
}
