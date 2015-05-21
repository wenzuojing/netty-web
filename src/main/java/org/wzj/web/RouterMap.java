package org.wzj.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wzj.web.annotaction.BodyValue;
import org.wzj.web.annotaction.Controller;
import org.wzj.web.annotaction.ParamValue;
import org.wzj.web.annotaction.PathValue;
import org.wzj.web.imp.DefaultObjectFactory;
import org.wzj.web.util.ClassLoaderUtils;
import org.wzj.web.util.ClassUtils;
import org.wzj.web.util.StringUtils;
import org.wzj.web.util.TypeConvertUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by wens on 15-5-13.
 */
public class RouterMap {

    private static Logger log = LoggerFactory.getLogger(RouterMap.class);

    private static final Pattern METHOD_SIGNATURE = Pattern.compile("([^\\(\\)]+(\\(.*\\))?)");

    private static final Pattern KEY_PATTERN = Pattern.compile("\\{(\\w+)(:(.*?))?\\}");

    private static final Pattern REGEX = Pattern.compile("(\\{\\d+(,\\d+)?\\})");

    private static final Pattern REGEX_FLAG = Pattern.compile("#REGEX_FLAG#");

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Map<String, List<Router>> map = new HashMap<String, List<Router>>();

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    public void scanRouters(String packageName) {
        try {
            Set<Class<?>> classes = ClassLoaderUtils.getClasses(packageName);

            for (Class<?> clazz : classes) {

                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                for (Method method : clazz.getDeclaredMethods()) {

                    if (!Modifier.isPublic(method.getModifiers()) || !method.isAnnotationPresent(org.wzj.web.annotaction.Router.class)) {
                        continue;
                    }

                    org.wzj.web.annotaction.Router router = method.getAnnotation(org.wzj.web.annotaction.Router.class);
                    addRouter(router.value(), router.method().name(), method);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void addRouter(String route, String httpMethod, String handle) {

        if (handle == null) {
            throw new WebException("handle can not be null.");
        }

        handle = handle.replaceAll("\\s+", "");

        Method handleMethod = getHandleMethod(handle);

        addRouter(route, httpMethod, handleMethod);
    }


    public void addRouter(String route, String httpMethod, Method handleMethod) {

        Router router = new Router();
        router.route = route;
        router.httpMethod = httpMethod;
        router.handleMethod = handleMethod;

        RouteParser routeParser = new RouteParser(route).parse();
        router.keys = routeParser.keys;

        try {
            router.p_route = Pattern.compile(routeParser.routeRegex);
        } catch (PatternSyntaxException e) {
            throw new WebException("Error in route  : " + route, e);
        }

        log.info("[add router]" + router);

        validateRouter(router);

        rwLock.writeLock().lock();
        try {

            List<Router> routers = map.get(httpMethod);

            if (routers == null) {
                routers = new ArrayList<Router>();
                map.put(httpMethod, routers);
            }
            routers.add(router);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private void validateRouter(Router router) {
        //验证参数annotation合法性
        Annotation[][] parameterAnnotations = router.handleMethod.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (annotations.length == 1) {
                if (annotations[0] instanceof PathValue) {
                    String key = ((PathValue) annotations[0]).value();
                    if (!router.keys.contains(key)) {
                        throw new WebException(router.route + " does not contains " + key + " key.");
                    }
                } else if (annotations[0] instanceof ParamValue) {
                    //
                } else if (annotations[0] instanceof BodyValue) {
                    if (!router.handleMethod.getParameterTypes()[i].isAssignableFrom(byte[].class)) {
                        throw new WebException("The BodyValue annotation of " + router.handleMethod + " must be place on byte[] type.");
                    }
                }
            }
        }

    }

    public boolean handle(String httpMethod, String uri, WebContext webContext) {

        rwLock.readLock().lock();
        List<Router> routers;

        try {
            routers = map.get(httpMethod);
        } finally {
            rwLock.readLock().unlock();
        }

        if (routers == null || routers.size() == 0) {
            return false;
        }


        boolean found = false;
        Router router = null;
        Matcher routerMatcher = null;


        //find router
        for (int i = 0, len = routers.size(); i < len; i++) {
            router = routers.get(i);
            routerMatcher = router.p_route.matcher(uri);
            if (routerMatcher.matches()) {
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        Object[] args = collectArgs(webContext, router, routerMatcher);

        try {
            Method handleMethod = router.handleMethod;
            if (Modifier.isStatic(handleMethod.getModifiers())) {
                handleMethod.invoke(null, args);
            } else {
                handleMethod.invoke(objectFactory.instance(handleMethod.getDeclaringClass().getCanonicalName(), true), args);
            }

        } catch (Exception e) {
            throw new WebException("Invoke method fail :" + router.handleMethod + ":" + Arrays.toString(args), e);
        }

        return true;

    }

    private Object[] collectArgs(WebContext webContext, Router router, Matcher routerMatcher) {

        Class<?>[] parameterTypes = router.handleMethod.getParameterTypes();
        Annotation[][] parameterAnnotations = router.handleMethod.getParameterAnnotations();
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
                        String pathVar = routerMatcher.group(((PathValue) annotations[0]).value());
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


    static class Router {

        protected String httpMethod;

        protected String route;

        protected Pattern p_route;

        protected Set<String> keys;

        protected Method handleMethod;

        @Override
        public String toString() {
            return "Router{" +
                    "httpMethod='" + httpMethod + '\'' +
                    ", route='" + route + '\'' +
                    ", p_route=" + p_route +
                    ", keys=" + keys +
                    '}';
        }
    }


    private class RouteParser {
        final Set<String> keys = new HashSet<String>();
        String route;
        String routeRegex;

        RouteParser(String route) {
            this.route = route;
        }

        public RouteParser parse() {
            //替换正则,消除对路由解析,如{3} {3,8}
            final List<String> regexs = new ArrayList<String>(5);

            route = StringUtils.replace(route, REGEX, new StringUtils.ReplacementHandler() {
                @Override
                public String doReplace(Matcher matcher) {
                    regexs.add(matcher.group(1));
                    return "#REGEX_FLAG#";
                }
            });


            routeRegex = StringUtils.replace(route, KEY_PATTERN, new StringUtils.ReplacementHandler() {
                @Override
                public String doReplace(Matcher matcher) {
                    keys.add(matcher.group(1));
                    return "(?<" + matcher.group(1) + ">" + (matcher.group(3) == null ? "[^/]+" : matcher.group(3)) + ")";
                }
            });

            routeRegex = StringUtils.replace(routeRegex, REGEX_FLAG, new StringUtils.ReplacementHandler() {
                int index = 0;

                @Override
                public String doReplace(Matcher matcher) {
                    return regexs.get(index++);
                }
            });
            return this;
        }
    }
}
