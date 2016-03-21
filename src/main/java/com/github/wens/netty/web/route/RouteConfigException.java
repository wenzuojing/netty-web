package com.github.wens.netty.web.route;

/**
 * Created by wens on 15-9-1.
 */
public class RouteConfigException extends RuntimeException {

    public RouteConfigException() {
        super();
    }

    public RouteConfigException(String message) {
        super(message);
    }

    public RouteConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteConfigException(Throwable cause) {
        super(cause);
    }

    protected RouteConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
