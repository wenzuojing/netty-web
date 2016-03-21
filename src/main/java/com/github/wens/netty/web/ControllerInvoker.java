package com.github.wens.netty.web;

import com.github.wens.netty.web.annotaction.BodyValue;
import com.github.wens.netty.web.annotaction.ParamValue;
import com.github.wens.netty.web.annotaction.PathValue;
import com.github.wens.netty.web.route.RouteMatcher;
import com.github.wens.netty.web.route.RouteResult;
import com.github.wens.netty.web.util.JsonUtils;
import com.github.wens.netty.web.util.TypeConvertUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * Created by wens on 15-9-1.
 */
public class ControllerInvoker {

    private RouteMatcher routeMatcher;

    private ObjectFactory objectFactory;

    private ServerConfig serverConfig;

    public ControllerInvoker(RouteMatcher routeMatcher, ObjectFactory objectFactory, ServerConfig serverConfig) {
        this.routeMatcher = routeMatcher;
        this.objectFactory = objectFactory;
        this.serverConfig = serverConfig;
    }

    public void invoke(String method, String uri, WebContext webContext) {

        RouteResult routeResult = this.routeMatcher.match(method, uri);
        if (routeResult == null) {
            webContext.getResponse().setStatus(NOT_FOUND.code(), NOT_FOUND.reasonPhrase());
            return;
        }

        Object[] args = collectArgs(webContext, routeResult.getRouteInfo().getHandleMethod(), routeResult.getParams());

        Controller controller = null;
        Exception exception = null;

        try {
            Method handleMethod = routeResult.getRouteInfo().getHandleMethod();
            Object returnObj = null;
            Object controllerObject = this.objectFactory.instance(handleMethod.getDeclaringClass().getCanonicalName(), true);
            if (controllerObject instanceof Controller) {
                controller = (Controller) controllerObject;
                if (controller.preHandle(webContext)) {
                    returnObj = handleMethod.invoke(controller, args);
                    controller.postHandle(webContext);
                }
            } else {
                returnObj = handleMethod.invoke(controllerObject, args);
            }

            if (returnObj != null) {
                webContext.getResponse().setContentType(String.format("application/json; charset=%s", serverConfig.getCharset()));
                webContext.getResponse().writeBody(JsonUtils.serialize(returnObj));
            }

        } catch (Exception e) {
            exception = e;
            throw new WebException("Invoke method fail :" + routeResult.getRouteInfo().getHandleMethod() + ":" + Arrays.toString(args), e);
        } finally {
            if (controller != null) {
                controller.afterCompletion(webContext, exception);
            }
        }


    }


    private Object[] collectArgs(WebContext webContext, Method handleMethod, Map<String, String> params) {


        Class<?>[] parameterTypes = handleMethod.getParameterTypes();
        Annotation[][] parameterAnnotations = handleMethod.getParameterAnnotations();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            Object value = null;
            if (clazz.isAssignableFrom(WebContext.class)) {
                value = webContext;
            } else {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations.length == 1) {
                    if (annotations[0] instanceof PathValue) {
                        String pathVar = params.get(((PathValue) annotations[0]).value());
                        value = TypeConvertUtils.convert(pathVar, clazz);
                    } else if (annotations[0] instanceof ParamValue) {
                        String name = ((ParamValue) annotations[0]).value();
                        String paramValue = webContext.getRequest().queryParams(name);
                        if (paramValue == null) {
                            paramValue = webContext.getRequest().getParam(name);
                        }
                        value = TypeConvertUtils.convert(paramValue, clazz);
                    } else if (annotations[0] instanceof BodyValue) {
                        value = webContext.getRequest().getBodyAsBytes();
                    }
                }

            }
            args[i] = value;
        }
        return args;
    }

}
