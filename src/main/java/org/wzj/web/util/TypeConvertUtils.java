package org.wzj.web.util;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by wens on 15-5-21.
 */
public class TypeConvertUtils {

    /**
     * 字符串转换成其他类型
     *
     * @param string
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T convert(String string, Class<T> tClass) {

        if (tClass.isAssignableFrom(String.class)) {
            return (T) string;
        }

        if (tClass == Integer.TYPE || tClass.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(string);
        } else if (tClass == Long.TYPE || tClass.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(string);
        } else if (tClass == Float.TYPE || tClass.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(string);
        } else if (tClass == Double.TYPE || tClass.isAssignableFrom(Double.class)) {
            return (T) Double.valueOf(string);
        } else if (tClass == Short.TYPE || tClass.isAssignableFrom(Short.class)) {
            return (T) Short.valueOf(string);
        } else if (tClass.isAssignableFrom(Number.class)) {
            try {
                return (T) NumberFormat.getNumberInstance().parse(string);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else if (tClass.isAssignableFrom(BigDecimal.class)) {
            return (T) new BigDecimal(string);
        } else if (tClass.isAssignableFrom(BigDecimal.class)) {
            return (T) new BigDecimal(string);
        } else if (tClass == Byte.TYPE || tClass.isAssignableFrom(Byte.class)) {
            return (T) Byte.valueOf(string);
        } else if (tClass == Boolean.TYPE || tClass.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(string);
        } else {
            throw new RuntimeException("Sorry,i can't convert this " + tClass + ".");
        }
    }


    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        System.out.println(Integer.valueOf(null));

    }

    public static void test(int i) {

    }
}
