package com.loy.kit.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 反射工具类
 * 封装反射一般使用方法, 如获取成员, 设置成员值, 调用方法
 * 另外, 还包括获取泛型实例类型的方法, 如类, 接口, 方法等上面的泛型具体类型
 */
public class ReflectUtil {

    // 获取对象的成员对象
    public static <T> T getFiledObject(Object parent, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(parent);
    }

    // 设置对象的成员值
    public static void setFieldObject(Object parent, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(parent, value);
    }

    // 调用对象的成员方法, 支持静态方法调用
    public static <T> T callMethod(Object target, String methodName, Object[] args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class[] clsArgs = null;
        if (args != null && args.length > 0) {
            clsArgs = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                clsArgs[i] = args[i].getClass();
            }
        }
        Method method = target.getClass().getDeclaredMethod(methodName, clsArgs);
        T ret = (T) method.invoke(target, args);
        return ret;
    }

    /**
     * 获得当前Class父类对象上的泛型实际类型
     * 如Bean<K,V> 获得Bean类上,K类型下标为0,V类型下标为1
     *
     * @param cls
     * @param index
     * @param <T>
     * @return
     */
    public static <T> Class<T> getTypeFromSuperClass(Class<?> cls, int index) {
        try {
            ParameterizedType type = (ParameterizedType) cls.getGenericSuperclass();
            Type[] actualTypeArguments = type.getActualTypeArguments();

            if (index < actualTypeArguments.length) {
                return (Class<T>) actualTypeArguments[index];
            }

        } catch (Exception e) {
        }

        return null;
    }

    // 获取接口实现对象上的泛型, index 为几个泛型, 0 为 第一个
    public static Type getTypeFromInterface(Class<?> cls, int index) {
        try {
            Type[] genericInterfaces = cls.getGenericInterfaces();
            if (EmptyUtil.isArrayNotEmpty(genericInterfaces)) {
                ParameterizedType type = (ParameterizedType) genericInterfaces[0];
                Type[] actualTypeArguments = type.getActualTypeArguments();
                if (EmptyUtil.isArrayNotEmpty(actualTypeArguments) && index < actualTypeArguments.length) {
                    Type genericType = actualTypeArguments[index];
                    return genericType;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Type getInnerUpClass(Type type) {
        Type ret = type;
        if (type != null) {
            Type t = type;
            if (t instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) t).getActualTypeArguments();
                while (actualTypeArguments != null) {
                    t = actualTypeArguments[0];
                    if (t instanceof ParameterizedType) {
                        actualTypeArguments = ((ParameterizedType) t).getActualTypeArguments();
                    }else if (t instanceof Class){
                        Class<?> declaringClass =((Class) t).getDeclaringClass();
                        while (declaringClass != null) {
                            t = declaringClass;
                            declaringClass = t.getClass().getDeclaringClass();
                        }
                        ret = t;
                        break;
                    }
                }
            }else if (t instanceof Class){
                Class<?> declaringClass =((Class) t).getDeclaringClass();
                while (declaringClass != null) {
                    t = declaringClass;
                    declaringClass = t.getClass().getDeclaringClass();
                }
                ret = t;
            }
        }
        return ret;
    }

    public static <T> Class<T> getMethodReturnType(Class<?> cls, String methodName, Class<?>... params) {

        try {
            Method method = cls.getMethod(methodName, params);
            if (method != null) {
                return (Class<T>) method.getReturnType();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> Class<T> getMethodReturnType(Class<?> cls, String methodName) {

        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return (Class<T>) m.getReturnType();
            }
        }

        return null;
    }

    public static <T> Class<T>[] getMethodParams(Class<?> cls, String methodName) {
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return (Class<T>[]) m.getParameterTypes();
            }
        }
        return null;
    }

    public static <T> Class<T> getMethodParam(Class<?> cls, String methodName, int index) {
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (index < parameterTypes.length) {
                    return (Class<T>) parameterTypes[index];
                }
            }
        }
        return null;
    }

}
