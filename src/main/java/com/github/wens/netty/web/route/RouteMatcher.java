package com.github.wens.netty.web.route;

import com.github.wens.netty.web.WebException;
import com.github.wens.netty.web.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by wens on 15-9-1.
 */
public class RouteMatcher {

    private static final Pattern METHOD_SIGNATURE = Pattern.compile("([^\\(\\)]+(\\(.*\\))?)");

    private static final Pattern SIMPLE_TEXT_ROUTE = Pattern.compile("[A-Za-z0-9/]+");

    private Map<String, Map<String, RouteInfo>> routes = new HashMap<String, Map<String, RouteInfo>>();
    private Map<String, List<RegRouteInfo>> regRoutes = new HashMap<String, List<RegRouteInfo>>();

    private String contextPath;

    public RouteMatcher(String contextPath) {
        this.contextPath = contextPath.trim();

    }


    public void addRouter(String route, String httpMethod, String handle) {

        if (handle == null) {
            throw new WebException("handle can not be null.");
        }

        handle = handle.replaceAll("\\s+", "");

        Method handleMethod = getHandleMethod(handle);

        addRouter(route, httpMethod, handleMethod);
    }


    public RouteInfo addRouter(String route, String httpMethod, Method handleMethod) {
        handleMethod.setAccessible(true);//通过setAccessible(true)的方式关闭安全检查，提升反射性能
        if (isSimpleRoute(route)) {
            return addSimpleRouteInfo(route, httpMethod, handleMethod);
        } else {
            return addRegRouteInfo(route, httpMethod, handleMethod);
        }
    }


    public RouteResult match(String httpMethod, String uri) {
        uri = uri.trim();
        int index = uri.indexOf("?");
        if (index != -1) {
            uri = uri.substring(0, index);
        }
        Map<String, RouteInfo> routeInfos = routes.get(httpMethod);

        if (routeInfos != null) {
            RouteInfo routeInfo = routeInfos.get(uri);
            if (routeInfo != null) {
                return new RouteResult(routeInfo);
            }
        }


        List<RegRouteInfo> regRouteInfos = regRoutes.get(httpMethod);

        if (regRouteInfos == null || regRouteInfos.size() == 0) {
            return null;
        }

        boolean found = false;
        RegRouteInfo regRouteInfo = null;
        Matcher routeMatcher = null;


        //find routeInfo
        for (int i = 0, len = regRouteInfos.size(); i < len; i++) {
            regRouteInfo = regRouteInfos.get(i);
            routeMatcher = regRouteInfo.getRegRoute().matcher(uri);
            if (routeMatcher.matches()) {
                found = true;
                break;
            }
        }

        if (!found) {
            return null;
        }

        if (regRouteInfo.getVarNames() != null) {
            Map<String, String> params = new HashMap<>();
            for (String varName : regRouteInfo.getVarNames()) {
                params.put(varName, routeMatcher.group(varName));
            }
            return new RouteResult(regRouteInfo, params);
        } else {
            return new RouteResult(regRouteInfo);
        }
    }

    private RegRouteInfo addRegRouteInfo(String route, String httpMethod, Method handleMethod) {

        route = contextPath + route;

        RegRouteParser routeParser = new RegRouteParser(route).parse();
        Pattern pattern = null;
        try {
            pattern = Pattern.compile(routeParser.getRouteRegex());
        } catch (PatternSyntaxException e) {
            throw new RouteConfigException("Error in route  : " + route, e);
        }

        List<RegRouteInfo> routeInfos = regRoutes.get(httpMethod);

        if (routeInfos == null) {
            routeInfos = new ArrayList<>(5);
            regRoutes.put(httpMethod, routeInfos);
        }

        RegRouteInfo regRouteInfo = new RegRouteInfo(httpMethod, route, handleMethod, pattern, routeParser.getKeys());
        routeInfos.add(regRouteInfo);
        return regRouteInfo;

    }

    private RouteInfo addSimpleRouteInfo(String route, String httpMethod, Method handleMethod) {
        route = contextPath + route;
        Map<String, RouteInfo> routeInfos = routes.get(httpMethod);

        if (routeInfos == null) {
            routeInfos = new HashMap<>();
            routes.put(httpMethod, routeInfos);
        }
        RouteInfo routeInfo = new RouteInfo(httpMethod, route, handleMethod);
        routeInfos.put(route, routeInfo);
        return routeInfo;
    }

    private boolean isSimpleRoute(String route) {
        return SIMPLE_TEXT_ROUTE.matcher(route).matches();
    }

    private Method getHandleMethod(String handle) {
        //"org.wzj.App.index"  or "org.wzj.App.index()" or "org.wzj.App.index(java.lang.String)"

        Matcher matcher = METHOD_SIGNATURE.matcher(handle);

        if (!matcher.matches()) {
            throw new WebException("Illegal handle : " + handle);
        }

        String[] parts = handle.split("\\(");

        String classNameAndMethodName = parts[0];

        int dotIndex = classNameAndMethodName.lastIndexOf(".");

        if (dotIndex < 0) {
            throw new WebException("Illegal handle : " + handle);
        }

        String className = classNameAndMethodName.substring(0, dotIndex);
        String classMethodName = classNameAndMethodName.substring(dotIndex + 1);

        Class<?> aClass = ClassUtils.loadClass(className);

        Method handleMethod = null;

        if (parts.length == 2) {

            String param = parts[1].substring(0, parts[1].length() - 1);

            if ("".equals(param)) {
                handleMethod = ClassUtils.getMethod(aClass, classMethodName, new Class<?>[]{});
            } else {
                String[] parameters = param.split(",");
                Class<?>[] parameterTypes = new Class<?>[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    parameterTypes[i] = ClassUtils.loadClass(parameters[i]);
                }
                handleMethod = ClassUtils.getMethod(aClass, classMethodName, parameterTypes);
            }


        } else {
            handleMethod = ClassUtils.getMethod(aClass, classMethodName);
        }

        return handleMethod;
    }
}
