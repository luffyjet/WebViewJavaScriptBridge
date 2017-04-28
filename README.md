WebViewJavascriptBridge
==========================
根据[IOS marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge) 编写而来的JavascriptBridge，这样一来前端可以公用一套JS代码。使用方法和 marcuswestin/WebViewJavascriptBridge 也是基本一样。

==同时也在此之上做了加强，根据Cordova的源码，将每一种消息封装成一个插件(RequestHandler)，并统一管理起来(HandlerManager)。具体请看下面的 插件管理功能一栏==

An Android bridge for sending messages between Java and JavaScript in WebViews. Based on [IOS marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge).


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


## 插件管理功能
可以和Cordova一样进行插件管理，每一种类型的消息都由一个插件管理。
插件类继承至 RequestHandler ，包含WebView的Activity要实现 BridgeInterface 接口。插件类由XML文件进行配置,请新建 res/xml/wjbconfig.xml 文件。

```
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
```

一个插件对应一个feature，feature name就是handle name。 onload属性为true代表插件会在webview初始化时一同初始化，false则是在需要该插件的时候通过反射加载。

具体使用方法请查看 ``app/`` 目录下的示例代码。


## Thanks
[marcuswestin/WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge) 
& [cordova-android](https://github.com/apache/cordova-android)



License
--------

    Copyright 2017 luffyjet.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.