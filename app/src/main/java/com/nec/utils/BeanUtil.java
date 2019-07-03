package com.nec.utils;

import junit.framework.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class BeanUtil {
    /**
     * 利用反射实现对象之间属性复制
     * @param from
     * @param to
     */
    public static void copyProperties(Object from, Object to) {
        copyPropertiesExclude(from, to, null);
    }

    /**
     * 复制对象属性
     * @param from
     * @param to
     * @param excludsArray 排除属性列表
     * @throws
     */
    public static void copyPropertiesExclude(Object from, Object to, String[] excludsArray) {
        List<String> excludesList = null;
        if(excludsArray != null && excludsArray.length > 0) {
            excludesList = Arrays.asList(excludsArray); //构造列表对象
        }
        Method[] fromMethods = from.getClass().getDeclaredMethods();
        Method[] toMethods = to.getClass().getDeclaredMethods();
        Method fromMethod = null, toMethod = null;
        String fromMethodName = null, toMethodName = null;
        for (int i = 0; i < fromMethods.length; i++) {
            fromMethod = fromMethods[i];
            fromMethodName = fromMethod.getName();
            if (!fromMethodName.contains("get"))
                continue;
            //排除列表检测
            if(excludesList != null && excludesList.contains(fromMethodName.substring(3).toLowerCase())) {
                continue;
            }
            toMethodName = "set" + fromMethodName.substring(3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null)
                continue;
            try {
                Object value = fromMethod.invoke(from, new Object[0]);
                if(value == null)
                    continue;
                //集合类判空处理
                if(value instanceof Collection) {
                    Collection newValue = (Collection)value;
                    if(newValue.size() <= 0)
                        continue;
                }
                toMethod.invoke(to, new Object[] {value});
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * 对象属性值复制，仅复制指定名称的属性值
     * @param from
     * @param to
     * @param includsArray
     * @throws
     */
    public static void copyPropertiesInclude(Object from, Object to, String[] includsArray) {
        List<String> includesList = null;
        if(includsArray != null && includsArray.length > 0) {
            includesList = Arrays.asList(includsArray); //构造列表对象
        } else {
            return;
        }
        Method[] fromMethods = from.getClass().getDeclaredMethods();
        Method[] toMethods = to.getClass().getDeclaredMethods();
        Method fromMethod = null, toMethod = null;
        String fromMethodName = null, toMethodName = null;
        for (int i = 0; i < fromMethods.length; i++) {
            fromMethod = fromMethods[i];
            fromMethodName = fromMethod.getName();
            if (!fromMethodName.contains("get"))
                continue;
            //排除列表检测
            String str = fromMethodName.substring(3);
            if(!includesList.contains(str.substring(0,1).toLowerCase() + str.substring(1))) {
                continue;
            }
            toMethodName = "set" + fromMethodName.substring(3);
            toMethod = findMethodByName(toMethods, toMethodName);
            if (toMethod == null)
                continue;
            try {
                Object value = fromMethod.invoke(from, new Object[0]);
                if(value == null)
                    continue;
                //集合类判空处理
                if(value instanceof Collection) {
                    Collection newValue = (Collection)value;
                    if(newValue.size() <= 0)
                        continue;
                }
                toMethod.invoke(to, new Object[] {value});
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////

    public static boolean equalsInRange(Object obj1, Object obj2, String... names) {
        if(obj1==null && obj2==null)
            return true;
        if(obj1==null || obj2==null)
            return false;
        Method[] methods1 = obj1.getClass().getDeclaredMethods();
        Method[] methods2 = obj2.getClass().getDeclaredMethods();
        for(String name: names) {
            name = "get" + name.substring(0,1).toUpperCase() + name.substring(1);
            Method method1 = findMethodByName(methods1, name);
            Method method2 = findMethodByName(methods2, name);
            if(method1==null || method2==null)
                return false;
            try {
                Object value1 = method1.invoke(obj1, new Object[0]);
                Object value2 = method2.invoke(obj2, new Object[0]);
                if(value1 == null && value2 == null)
                    ;
                else if(value1 == null || value2 == null)
                    return false;
                else {
                    if(value1 instanceof Collection && value2 instanceof Collection) {
                        if(((Collection)value1).size() != ((Collection)value1).size())
                            return false;
                    } else
                    if(!value1.equals(value2))
                        return false;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////

    /**
     * 从方法数组中获取指定名称的方法
     *
     * @param methods
     * @param name
     * @return
     */
    public static Method findMethodByName(Method[] methods, String name) {
        for (int j = 0; j < methods.length; j++) {
            if (methods[j].getName().equals(name))
                return methods[j];
        }
        return null;
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
     */
    public static Object getFieldValue(final Object object, final String fieldName) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("never happend exception!", e);
        }
        return result;
    }

    /**
     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("never happend exception!", e);
        }
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     */
    protected static Field getDeclaredField(final Object object, final String fieldName) {
        Assert.assertNotNull(object);
        return getDeclaredField(object.getClass(), fieldName);
    }

    /**
     * 循环向上转型,获取类的DeclaredField.
     */
    @SuppressWarnings("unchecked")
    protected static Field getDeclaredField(final Class clazz, final String fieldName) {
        Assert.assertNotNull(clazz);
        Assert.assertFalse(fieldName.isEmpty());
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 强制转换fileld可访问.
     */
    protected static void makeAccessible(final Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    public static Object getSimpleProperty(Object bean, String propName) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return bean.getClass().getMethod(getReadMethod(propName)).invoke(bean);
    }

    private static String getReadMethod(String name) {
        return "get" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
    }

}
