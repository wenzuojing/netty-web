package org.wzj.web.util;


import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wens on 15-5-15.
 */
public class ClassUtils {

    private static Pattern METHOD_SIGNATURE = Pattern.compile("\\s([\\S]+\\(.*\\))");


    public static String getMethodSignature(Method method) {

        Matcher matcher = METHOD_SIGNATURE.matcher(method.toString());

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Can not extract method signature : " + method);
        }
    }

    public static Method getMethod(Class<?> aClass, String methodName) {

        Method[] methods = aClass.getMethods();

        for (Method m : methods) {

            if (m.getName().equals(methodName)) {
                return m;
            }
        }

        return null;


    }

    public static Method getMethod(Class<?> aClass, String methodName, Class<?>[] parameterTypes) {
        try {
            return aClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can not found " + methodName + " method in " + aClass, e);
        }


    }

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can not found class : " + className, e);
        }
    }

    public static Object newInstance(String className) {
        try {
            return loadClass(className).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Instantiation fail : " + className, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
