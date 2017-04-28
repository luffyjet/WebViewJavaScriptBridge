package com.luffyjet.jsbridgeexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.luffyjet.webviewjavascriptbridge.BridgeInterface;
import com.luffyjet.webviewjavascriptbridge.JSResult;
import com.luffyjet.webviewjavascriptbridge.RequestHandler;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridge;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridgeBase;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MainActivity extends AppCompatActivity implements BridgeInterface{
    private static final String TAG = MainActivity.class.getSimpleName();
    WebViewJavaScriptBridge mBridge;
    RequestHandler mRequestHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        if (mBridge != null) {
            return;
        }

        final WebView webView = (WebView) findViewById(R.id.webview);
        Button callJsBtn = (Button) findViewById(R.id.button);
        Button disableTimeoutBtn = (Button) findViewById(R.id.disable_timeout);
        Button refreshBtn = (Button) findViewById(R.id.refresh);


        //Start 初始化 WebViewJavaScriptBridge
        WebViewJavaScriptBridge.enableLogging();//打印Log
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);//很关键
        mBridge = WebViewJavaScriptBridge.bridgeForWebView(this, webView);
        mBridge.setWebViewDelegate(new MyWebViewClient());//设置WebViewClient
        webView.setWebChromeClient(new MyChromeClient());//设置ChromeClient
        //End


        callJsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //向js端发送消息
                callHandler();
            }
        });

        //注册一个 处理 js端发来消息的 handler
        mBridge.registerHandler("abs", new WebViewJavaScriptBridgeBase.WVJBHandler() {
            @Override
            public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
                Log.d(TAG, "from JS req: " + data.toString());
                responseCallback.callback(new JSResult("i like milk from native").toJson());
            }
        });


        disableTimeoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableSafetyTimeout();
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

//        webView.loadUrl("http://192.168.118.55:8090/spittr/ExampleApp.html");

        webView.loadUrl("file:///android_asset/ExampleApp.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != mRequestHandler) {
            RequestHandler callback = mRequestHandler;
            mRequestHandler = null;
            callback.onActivityResult(requestCode, resultCode, data);
            return;
        }

        //other code
    }

    @Override
    public void startActivityForResult(RequestHandler command, Intent intent, int requestCode) {
        setActivityResultCallback(command);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void setActivityResultCallback(RequestHandler plugin) {
        mRequestHandler = plugin;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public ExecutorService getThreadPool() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return  new Thread(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        r.run();
                    }
                },"Test");
            }
        });
    }


    /**
     * 发送消息给JS端
     */
    void callHandler() {
        Model model = new Model();
        model.name = "lucy";
        model.age = 18;
        model.msg = "Hi there, JS!";

        mBridge.callHandler("NativeCallJS", model.toJSON(), new WebViewJavaScriptBridgeBase.WVJBResponseCallback() {
            @Override
            public void callback(String responseData) {
                Log.d(TAG, "JS responded:" + responseData);
                Toast.makeText(MainActivity.this, "JS responded:" + responseData , Toast.LENGTH_SHORT).show();
            }
        });
    }

    void disableSafetyTimeout() {
        mBridge.disableJavscriptAlertBoxSafetyTimeout();
    }

    class MyChromeClient extends WebChromeClient {

        // Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            //choose image or take photo
        }

        // Android > 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg);
        }

        // Android  > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg);
        }

        //Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            //choose image or take photo
            return true;
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(TAG, "onPageStarted");
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i(TAG, "onPageFinished");
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, "shouldOverrideUrlLoading");
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    static class Model {
        String name;
        int age;
        String msg;

        @Override
        public String toString() {
            return toJSON();
        }

        public String toJSON() {
            return new Gson().toJson(this);
        }
    }

}
