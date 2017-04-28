package com.luffyjet.webviewjavascriptbridge;

/**
 * Title :    
 * Author : luffyjet 
 * Date : 2016/12/19
 * Project : WebViewJavaScriptBridge
 * Site : http://www.luffyjet.com
 */

public class HandlerEntry {

    /**
     * The name of the service that this handler implements
     */
    public final String service;

    /**
     * The handler class name that implements the service.
     */
    public final String handlerClass;

    /**
     * The pre-instantiated handler to use for this entry.
     */
    public final RequestHandler handler;

    /**
     * Flag that indicates the handler object should be created when PluginManager is initialized.
     */
    public final boolean onload;

    /**
     * Constructs with a handler already instantiated.
     */
    public HandlerEntry(String service, RequestHandler handler) {
        this(service, handler.getClass().getName(), true, handler);
    }

    /**
     * @param service               The name of the service
     * @param handlerClass           The handler class name
     * @param onload                Create handler object when HTML page is loaded
     */
    public HandlerEntry(String service, String handlerClass, boolean onload) {
        this(service, handlerClass, onload, null);
    }

    private HandlerEntry(String service, String handlerClass, boolean onload, RequestHandler handler) {
        this.service = service;
        this.handlerClass = handlerClass;
        this.onload = onload;
        this.handler = handler;
    }
}
