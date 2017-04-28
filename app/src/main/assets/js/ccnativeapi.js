;(function() {
	window.onerror = function(err) {
    		console.log("page on error:" + err);
    	};

    	var browserType = {
    		types: function() {
    			var u = window.navigator.userAgent,
    				app = window.navigator.appVersion;
    			return { //移动终端浏览器版本信息
    				trident: u.indexOf('Trident') > -1, //IE内核
    				presto: u.indexOf('Presto') > -1, //opera内核
    				webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
    				gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
    				mobile: !!u.match(/AppleWebKit.*Mobile.*/) || !!u.match(/AppleWebKit/), //是否为移动终端
    				mac: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
    				android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
    				iPhone: u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, //是否为iPhone或者QQHD浏览器
    				iPad: u.indexOf('iPad') > -1, //是否iPad
    				webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部

    			};
    		}(),

    		language: (window.navigator.browserLanguage || window.navigator.language).toLowerCase()
    	};

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
    		if(browserType.types.android) {
    			WVJBIframe.src = 'wvjbscheme://__BRIDGE_LOADED__';
    		} else if(browserType.types.mac || browserType.types.iPhone || browserType.types.iPad) {
    			WVJBIframe.src = 'https://__bridge_loaded__';
    		}
    		document.documentElement.appendChild(WVJBIframe);
    		setTimeout(function() {
    			document.documentElement.removeChild(WVJBIframe)
    		}, 0);
    	}

    	function invoke(handlerName, data, callback) {
    		if(window.WebViewJavascriptBridge) {
    			window.WebViewJavascriptBridge.callHandler(handlerName, data, callback);
    		} else {
    			document.addEventListener(
    				'WebViewJavascriptBridgeReady',
    				function() {
    					window.WebViewJavascriptBridge.callHandler(handlerName, data, callback);
    				},
    				false
    			);
    		}
    	}

    	setupWebViewJavascriptBridge(function(bridge) {
    		bridge.registerHandler('NativeCallJS', function(data, responseCallback) {
    			var responseData = {
    				'Javascript Says': 'Right back atcha!'
    			};
    			responseCallback(responseData);
    		});

    		var doc = document;
    		var readyEvent = doc.createEvent('Events');
    		readyEvent.initEvent('WebViewJavascriptBridgeReady');
    		readyEvent.bridge = WebViewJavascriptBridge;
    		doc.dispatchEvent(readyEvent);
    	});


	window.ccapi = {
		//设备信息
		deviceInfo: function(data) {
			invoke("deviceInfo", {
				'full': data.full
			}, data.success);
		},
		
		//扫二维码
		scanQRCode: function(data) {
			invoke("scanQRCode", {
				'needResult': data.needResult,
				'scanType': data.scanType
			}, data.success);
		},
		//选择图片
		chooseImage: function(data) {
			invoke("chooseImage", {
				'count': data.count,
				'sizeType': data.sizeType,
				'sourceType': data.sourceType
			}, data.success);
		}

	};
})();