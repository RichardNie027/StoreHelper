package com.nec.utils;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CastUtil {

    public static String toString(int i) {
        return String.valueOf(i);
    }

    public static String toString(long l) {
        return String.valueOf(l);
    }

    public static String toString(float f) {
        return String.valueOf(f);
    }

    public static String toString(double d) {
        return String.valueOf(d);
    }

    public static String toString(boolean b) {
        return String.valueOf(b);
    }

    public static String toString(Object o) {
        return String.valueOf(o);
    }

    public static ArrayList<String> cast(ArrayList list) {
        ArrayList<String> arrayList = new ArrayList<>(list.size());
        for(int i=0; i<list.size(); i++)
            arrayList.add(list.get(i).toString());
        return arrayList;
    }

    /**
     * 将对象转为ArrayList
     * 属性的转换规则：
     * 不转换类别（String,Integer,Long,Float,Double,Boolean）
     * 转换类别（Date->"yyyy-MM-dd"字符串，其它类型toString）
     * @param bean
     * @return
     */
    public static ArrayList cast(Object bean, String[] orderedFieldNames) {
        Field[] fields = bean.getClass().getDeclaredFields();
        ArrayList arrayList = new ArrayList();
        for(String orderedFieldName: orderedFieldNames) {
            Field field = null;
            for(Field _field: fields) {
                if(orderedFieldName.equalsIgnoreCase(_field.getName())) {
                    field = _field;
                    break;
                }
            }
            if(field == null) {
                Log.e(CastUtil.class.getName(), "字段"+orderedFieldName+"不存在");
                continue;
            }
            field.setAccessible(true);  //打开私有访问
            Object fieldValue;
            try {
                fieldValue = field.get(bean);
            } catch (IllegalAccessException e) {
                fieldValue = "";
            }
            if(fieldValue instanceof String)
                arrayList.add(fieldValue.toString());
            else if(fieldValue instanceof Integer)
                arrayList.add((Integer)fieldValue);
            else if(fieldValue instanceof Long)
                arrayList.add((Long)fieldValue);
            else if(fieldValue instanceof Float)
                arrayList.add((Float)fieldValue);
            else if(fieldValue instanceof Double)
                arrayList.add((Double)fieldValue);
            else if(fieldValue instanceof Boolean)
                arrayList.add((Boolean)fieldValue);
            else if(fieldValue instanceof Date)
                arrayList.add(DateUtil.toStr((Date)fieldValue, "yyyy-MM-dd"));
            else
                arrayList.add(fieldValue.toString());
        }
        return arrayList;
    }

}
