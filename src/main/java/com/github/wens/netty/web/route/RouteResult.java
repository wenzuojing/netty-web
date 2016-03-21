package com.github.wens.netty.web.route;

import java.util.Map;

/**
 * Created by wens on 15-9-1.
 */
public class RouteResult {

    private Map<String, String> params;

    private RouteInfo routeInfo;

    public RouteResult(RouteInfo routeInfo) {
        this(routeInfo, null);
    }

    public RouteResult(RouteInfo routeInfo, Map<String, String> params) {
        this.routeInfo = routeInfo;
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(RouteInfo routeInfo) {
        this.routeInfo = routeInfo;
    }

    @Override
    public String toString() {
        return "RouteResult{" +
                "params=" + params +
                ", routeInfo=" + routeInfo +
                '}';
    }
}
