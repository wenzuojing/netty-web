package com.github.wens.netty.web;

import com.github.wens.netty.web.annotaction.BodyValue;
import com.github.wens.netty.web.annotaction.ParamValue;
import com.github.wens.netty.web.annotaction.PathValue;
import com.github.wens.netty.web.annotaction.Router;
import com.github.wens.netty.web.route.RegRouteInfo;
import com.github.wens.netty.web.route.RouteInfo;
import com.github.wens.netty.web.route.RouteMatcher;
import com.github.wens.netty.web.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Created by wens on 15-9-1.
 */
public class ControllerScanner {

    private static final Logger log = LoggerFactory.getLogger("netty-server");

    private RouteMatcher routeMatcher;

    private ObjectFactory objectFactory;

    public ControllerScanner(RouteMatcher routeMatcher, ObjectFactory objectFactory) {
        this.routeMatcher = routeMatcher;
        this.objectFactory = objectFactory;
    }

    public void scanControllers(String packageName) {
        try {
            Set<Class<?>> classes = ClassLoaderUtils.getClasses(packageName);

            for (Class<?> clazz : classes) {

                if (!clazz.isAnnotationPresent(com.github.wens.netty.web.annotaction.Controller.class)) {
                    continue;
                }

                objectFactory.instance(clazz.getCanonicalName(), true);

                for (Method method : clazz.getDeclaredMethods()) {
                    if (!Modifier.isPublic(method.getModifiers()) || !method.isAnnotationPresent(Router.class)) {
                        continue;
                    }
                    Router router = method.getAnnotation(Router.class);
                    if (HttpMethod.ALL == router.method()) {
                        for (HttpMethod httpMethod : HttpMethod.values()) {
                            RouteInfo routeInfo = routeMatcher.addRouter(router.value(), httpMethod.name(), method);
                            validate(routeInfo);
                            if (log.isDebugEnabled()) {
                                log.debug("Add Route:" + routeInfo);
                            }
                        }
                    } else {
                        RouteInfo routeInfo = routeMatcher.addRouter(router.value(), router.method().name(), method);
                        validate(routeInfo);
                        if (log.isDebugEnabled()) {
                            log.debug("Add Route:" + routeInfo);
                        }
                    }


                }
            }

        } catch (IOException e) {
            new WebException(e);
        } catch (ClassNotFoundException e) {
            new WebException(e);
        }

    }


    private void validate(RouteInfo routeInfo) {

        if (routeInfo instanceof RegRouteInfo) {
            RegRouteInfo regRouteInfo = (RegRouteInfo) routeInfo;

            //验证参数annotation合法性
            Annotation[][] parameterAnnotations = regRouteInfo.getHandleMethod().getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations.length == 1) {
                    if (annotations[0] instanceof PathValue) {
                        String key = ((PathValue) annotations[0]).value();
                        if (!regRouteInfo.getVarNames().contains(key)) {
                            throw new WebException(regRouteInfo.getRoute() + " does not contains " + key + " key.");
                        }
                    } else if (annotations[0] instanceof ParamValue) {
                        //
                    } else if (annotations[0] instanceof BodyValue) {
                        if (!regRouteInfo.getHandleMethod().getParameterTypes()[i].isAssignableFrom(byte[].class)) {
                            throw new WebException("The BodyValue annotation of " + regRouteInfo.getHandleMethod() + " must be place on byte[] type.");
                        }
                    }
                }
            }
        }


    }
}
