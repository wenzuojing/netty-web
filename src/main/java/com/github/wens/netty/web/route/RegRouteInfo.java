package com.github.wens.netty.web.route;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by wens on 15-9-1.
 */
public class RegRouteInfo extends RouteInfo {

    private Pattern regRoute;

    private Set<String> varNames;

    public RegRouteInfo(String httpMethod, String route, Method handleMethod, Pattern regRoute, Set<String> varNames) {
        super(httpMethod, route, handleMethod);
        this.regRoute = regRoute;
        this.varNames = varNames;
    }

    public Pattern getRegRoute() {
        return regRoute;
    }

    public void setRegRoute(Pattern regRoute) {
        this.regRoute = regRoute;
    }

    public Set<String> getVarNames() {
        return varNames;
    }

    public void setVarNames(Set<String> varNames) {
        this.varNames = varNames;
    }

    @Override
    public String toString() {
        return "RegRouteInfo{" +
                "regRoute=" + regRoute +
                ", varNames=" + varNames +
                "} " + super.toString();
    }
}
