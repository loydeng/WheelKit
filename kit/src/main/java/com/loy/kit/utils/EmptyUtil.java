package com.loy.kit.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loy
 * @time 2022/8/18 16:54
 * @des
 */
public class EmptyUtil {
    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean isNotNull(Object o) {
        return o != null;
    }

    public static boolean isStringEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isStringNotEmpty(String s) {
        return s != null && s.length() != 0;
    }

    public static boolean isStringBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isStringSpace(final String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStringNotBlank(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static String getEmptyString() {
        return "";
    }

    public static <T> List<T> getEmptyList() {
        return new ArrayList<T>();
    }

    public static <T> Set<T> getEmptySet() {
        return new HashSet<T>();
    }

    public static <K, V> Map<K, V> getEmptyMap() {
        return new HashMap<K, V>();
    }

    public static <T> boolean isArrayEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean isArrayNotEmpty(T[] array) {
        return !isArrayEmpty(array);
    }

    public static <T> T getEmptyImpl(Class<T> interfaceT) {
        ClassLoader classLoader = interfaceT.getClassLoader();
        T t = (T) Proxy.newProxyInstance(classLoader, new Class[]{interfaceT}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(args);
            }
        });
        return t;
    }

}
