package com.luffyjet.jsbridgeexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.gson.Gson;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridge;
import com.luffyjet.webviewjavascriptbridge.WebViewJavaScriptBridgeBase;

import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    WebViewJavaScriptBridge mBridge;

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

        WebViewJavaScriptBridge.enableLogging();

        mBridge = WebViewJavaScriptBridge.bridgeForWebView(this,webView);
        mBridge.setWebViewDelegate(new MyWebViewClient());
        webView.setWebChromeClient(new MyChromeClient());
//        Model model = new Model();
//        model.name = "test";
//        model.age = 0;
//        model.msg = "before ready";

//        mBridge.callHandler("NativeCallJS", model.toJSON());


        callJsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callHandler();
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


        mBridge.registerHandler("", new WebViewJavaScriptBridgeBase.WVJBHandler() {
            @Override
            public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {

            }
        });

//        webView.loadUrl("http://192.168.118.55:8090/spittr/ExampleApp.html");

        webView.loadUrl("file:///android_asset/ExampleApp.html");
    }

    void callHandler() {
        Model model = new Model();
        model.name = "lucy";
        model.age = 18;
        model.msg = "Hi there, JS!";

        mBridge.callHandler("NativeCallJS", model.toJSON(), new WebViewJavaScriptBridgeBase.WVJBResponseCallback() {
            @Override
            public void callback(String responseData) {
                Log.d(TAG, "NativeCallJS responded:" + responseData);
            }
        });
    }

    void disableSafetyTimeout() {
        mBridge.disableJavscriptAlertBoxSafetyTimeout();
    }

    private ValueCallback<Uri> mUploadMessage;
    ValueCallback<Uri[]> mFilePathCallback;

    class MyChromeClient extends WebChromeClient {

        // Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            pickImg(FILE_CHOOSER_RESULT_CODE);
        }

        // Android > 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg);
        }

        // Android  > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            pickImg(FILE_CHOOSER_RESULT_CODE_LOLIPOP);
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

    private static final int FILE_CHOOSER_RESULT_CODE = 0x001;
    private static final int FILE_CHOOSER_RESULT_CODE_LOLIPOP = 0x101;
    private int FILE_CHOOSER_VERSION;
    int pos;

    private void pickImg(final int code) {
//        ToastUtil.show("pickImg");
        FILE_CHOOSER_VERSION = code;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"拍照", "相册", "取消"});

        pos = -1;

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("选取图片").setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pos = which;
                switch (which) {
                    case 0:
                        takePhoto();

                        break;
                    case 1:

                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("image/*");
                        startActivityForResult(Intent.createChooser(i, "选取图片"), IMAGE);

                        break;
                    case 3:


                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        }).create();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (pos != 0 && pos != 1) {

                    if (null != mUploadMessage) {
                        mUploadMessage.onReceiveValue(null);
                        mUploadMessage = null;
                    }

                    if (null != mFilePathCallback) {
                        mFilePathCallback.onReceiveValue(null);
                        mFilePathCallback = null;
                    }
                }
            }
        });

        dialog.show();
    }

    /**
     * 拍照
     */
    File mCaptureFile;
    private static final int CAPTURE = 0x121;
    private static final int IMAGE = 0x122;

    private void takePhoto() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                mCaptureFile = new File(dir, "abs_" + System.currentTimeMillis() + ".jpg");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCaptureFile));
                startActivityForResult(intent, CAPTURE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (FILE_CHOOSER_VERSION == FILE_CHOOSER_RESULT_CODE) {
                    if (null == mUploadMessage) {
                        return;
                    }
                    Uri result = null;
                    if (requestCode == CAPTURE) {
                        result = Uri.fromFile(mCaptureFile);
                    } else if (requestCode == IMAGE) {
                        result = data.getData();
                    }

                    mUploadMessage.onReceiveValue(result);

                    mUploadMessage = null;
                } else if (FILE_CHOOSER_VERSION == FILE_CHOOSER_RESULT_CODE_LOLIPOP) {
                    if (null == mFilePathCallback) {
                        return;
                    }

                    Uri result = null;
                    if (requestCode == CAPTURE) {
                        result = Uri.fromFile(mCaptureFile);
                    } else if (requestCode == IMAGE) {
                        result = data.getData();
                    }

                    if (null != result) {
//                        String truePath = getRealPathFromURI(mContext, result);
                        mFilePathCallback.onReceiveValue(new Uri[]{result});
                    } else {
                        mFilePathCallback.onReceiveValue(null);
                    }

                    mFilePathCallback = null;
                }

                //刷新系统图片库
                if (null != mCaptureFile) {
                    MediaScannerConnection.scanFile(this, new String[]{mCaptureFile.getAbsolutePath()}, new String[]{"image/*"}, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (null != mUploadMessage) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }

                if (null != mFilePathCallback) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
