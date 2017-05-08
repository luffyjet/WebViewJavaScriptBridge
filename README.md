WebViewJavascriptBridge
==========================
根据[IOS marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge) 编写而来的JavascriptBridge。


相比同类库的优点：
1. 和[IOS marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge) 一样的使用方法,可共用一套JS代码。
2. 同时也在此之上做了加强，参考了Cordova源码的模块管理，方便把各种不同的原生功能封装成独立的模块并统一管理。具体请看下面的 模块管理功能一栏。

规定JS和Java之间用标准JSON格式字符串交互，JS传给Java的数据会封装成 org.json.JSONObject。

(An Android bridge for sending messages between Java and JavaScript in WebViews. Based on [IOS marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge).)


### Gradle
 

```groovy
repositories {
    jcenter()
}

compile 'com.luffyjet:webviewjavascriptbridge:1.0'
```



Examples
--------

See the `app/` folder.  



Usage
-----

1) Init WebViewJavaScriptBridge

```java
WebSettings settings = webView.getSettings();
settings.setJavaScriptEnabled(true);//很关键
WebViewJavaScriptBridge mBridge = WebViewJavaScriptBridge.bridgeForWebView(this, webView);
mBridge.setWebViewDelegate(new MyWebViewClient());//设置WebViewClient
webView.setWebChromeClient(new MyChromeClient());//设置ChromeClient
```

2) Register a handler in Java, and call a JS handler:

```java
 //注册一个 处理 js端发来消息的 handler
mBridge.registerHandler("abs", new WebViewJavaScriptBridgeBase.WVJBHandler() {
    @Override
    public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {
        Log.d(TAG, "from JS req: " + data.toString());
        responseCallback.callback(new JSResult("i like milk from native").toJson());
    }
});

mBridge.callHandler("NativeCallJS", model.toJSON(), new WebViewJavaScriptBridgeBase.WVJBResponseCallback() {
    @Override
    public void callback(String responseData) {
        Log.d(TAG, "JS responded:" + responseData);
        Toast.makeText(MainActivity.this, "JS responded:" + responseData , Toast.LENGTH_SHORT).show();
    }
});
        
```

3) Copy and paste `setupWebViewJavascriptBridge` into your JS:
	
```javascript
function setupWebViewJavascriptBridge(callback) {
	if(window.WebViewJavascriptBridge) {
		return callback(WebViewJavascriptBridge);
	}
	if(window.WVJBCallbacks) {
		return window.WVJBCallbacks.push(callback);
	}
	window.WVJBCallbacks = [callback];
	var WVJBIframe = document.createElement('iframe');
	WVJBIframe.style.display = 'none';
	//这里最新IOS版是 https的scheme，真实环境下 需要判断iOS和Android，做下区分。
	WVJBIframe.src = 'wvjbscheme://__BRIDGE_LOADED__';
	document.documentElement.appendChild(WVJBIframe);
	setTimeout(function() {
		document.documentElement.removeChild(WVJBIframe)
	}, 0);
}
```

5) Finally, call `setupWebViewJavascriptBridge` and then use the bridge to register handlers and call Java handlers:

```javascript
setupWebViewJavascriptBridge(function(bridge) {
	/* Initialize your app here */

	bridge.registerHandler('NativeCallJS', function(data, responseCallback) {
		var responseData = {
			'Javascript Says': 'Right back atcha!'
		};

		log('Native call JS with ', data);
		responseCallback(responseData);
	});

	var doc = document;
	var readyEvent = doc.createEvent('Events');
	readyEvent.initEvent('WebViewJavascriptBridgeReady');
	readyEvent.bridge = WebViewJavascriptBridge;
	doc.dispatchEvent(readyEvent);
});
```


## 模块管理功能
可以和Cordova一样进行模块管理，同一种类型的消息(handler name相同)都由一个模块处理。
模块类需要继承 RequestHandler ，包含WebView的Activity要实现 BridgeInterface 接口。插件类由XML文件进行配置,请在项目中新建 res/xml/wjbconfig.xml 文件。

```java
<?xml version="1.0" encoding="utf-8"?>
<widget>
    <feature name="chooseImage">
        <param
            name="android-package"
            value="com.luffyjet.jsbridgeexample.handlers.ImageChooseHandler"/>
        <param
            name="onload"
            value="true"/>
    </feature>

    <feature name="deviceInfo">
        <param
            name="android-package"
            value="com.luffyjet.jsbridgeexample.handlers.DeviceInfoHandler"/>
        <param
            name="onload"
            value="true"/>
    </feature>

</widget>


//一个简单的选图模块示例
public class ImageChooseHandler extends RequestHandler {
    private static final String TAG = "ImageChooseHandler";
    private static final int REQUEST_CODE_IMAGES = 100;
    private WebViewJavaScriptBridgeBase.WVJBResponseCallback mResponseCallback;

    @Override
    public void handle(JSONObject data, WebViewJavaScriptBridgeBase.WVJBResponseCallback responseCallback) {

        mResponseCallback = responseCallback;
        ...
        mBridgeInterface.startActivityForResult(this, Intent.createChooser(i, "选取图片"), REQUEST_CODE_IMAGES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGES) {
            try {
                ...
                mResponseCallback.callback(result.toJSON());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


// 包含webview 的 WebActivity
public class WebActivity extends AppCompatActivity implements BridgeInterface{

    RequestHandler mRequestHandler;

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
}

```

一个功能模块对应一个feature，feature name就是handle name。 onload属性为true代表模块(插件)会在webview初始化时一同初始化，false则是在需要该模块(插件)的时候通过反射加载。

具体使用方法请查看 ``app/`` 目录下的示例代码。


## Thanks
[marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge) 
& [lzyzsd/JsBridge](https://github.com/lzyzsd/JsBridge) & [cordova-android](https://github.com/apache/cordova-android)



License
--------

See the [LICENSE](https://github.com/luffyjet/WebViewJavaScriptBridge/blob/master/LICENSE.md) file for license rights and limitations.