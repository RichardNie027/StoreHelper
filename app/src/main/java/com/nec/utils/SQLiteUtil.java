package com.nec.utils;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * （在数据增改操作前，调用convert方法）将实体对象转换为ContentValues
 *
 * 关于SQLiteDatabase字段类型和实体对象属性类型的约定：
 * 1、SQLiteDatabase字段类型：integer,(text/varchar/char),(real/float/double)
 * 2、实体对象属性类型：(long,int,boolean),(String,Date),(float/double)
 * 3、类型的对应关系：integer->(long,int,boolean)
 * 4、convert方法自动实现的转换规则：Date(yyyy-MM-dd HH:mm:ss), boolean(1-true,0-false)
 *
 * 调用范式：
SQLiteOpenHelper helper = new SQLiteDbHelper(this);
//SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
SQLiteDatabase db = null;
try {
    db = helper.getWritableDatabase();
    db.beginTransaction();
    Entity entity = new Entity(...);
    ContentValues contentValues = SQLiteUtil.toContentValues(entity);
    db.insert(TABLE_NAME, null, contentValues);
    db.setTransactionSuccessful();
} finally {
    if (db != null) {
        db.endTransaction();
    }
    db.close();
}
 *
 * db.insert(TABLE_NAME, null, contentValues);
 * db.update(TABLE_NAME, whereClause, whereArgs);
 * db.delete(TABLE_NAME, whereClause);
 * db.execSQL(SQL, Object[] bindArgs);
 * db.rawQuery(SQL, String[] selectionArgs);
 * db.query(TABLE_NAME, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
 *
 * demo:
 *
Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
while(cursor.moveToNext()){
    String columnName = cursor.getString(cursor.getColumnIndex("columnName"));
 }
 cursor.close();
 *
 */
public class SQLiteUtil {

    public static ContentValues toContentValues(Object entity) {
        return toContentValues(entity, null);
    }

    /**
     * （在数据增改操作前，）将实体对象转换为ContentValues
     * @param entity 实体对象，属性支持的类型及对应的字段类型：(long,int,Date,boolean)->integer,(String)->text,(float/double)->real
     * @param exclusiveFields 排除字段，多字段以逗号分隔
     * @return null if an IllegalAccessException occurs.
     */
    public static ContentValues toContentValues(Object entity, String exclusiveFields) {
        ContentValues contentValues = new ContentValues();
        Field[] fields = entity.getClass().getDeclaredFields();
        try {
            exclusiveFields = "serialVersionUID," + (exclusiveFields==null ? "" : exclusiveFields+",");
            for(Field field: fields) {
                if(exclusiveFields.indexOf(field.getName()+",") != -1)
                    continue;
                String classname = field.getType().toString();
                if(classname.toLowerCase().endsWith("long")) {
                    long value = field.getLong(entity);
                    contentValues.put(field.getName(), value);
                } else if(classname.equals("int") || classname.endsWith("Integer")) {
                    int value = field.getInt(entity);
                    contentValues.put(field.getName(), value);
                } else if(classname.endsWith("String")) {
                    contentValues.put(field.getName(), field.get(entity).toString());
                } else if(classname.endsWith("Date")) {   // Date -> String
                    Date value = (Date) field.get(entity);
                    contentValues.put(field.getName(), DateUtil.toStr(value));
                } else if(classname.toLowerCase().endsWith("boolean")) {   // Boolean -> Integer
                    boolean value = field.getBoolean(entity);
                    contentValues.put(field.getName(), value?1:0);
                } else if(classname.equals("float") || classname.endsWith("Float")) {
                    float value = field.getFloat(entity);
                    contentValues.put(field.getName(), value);
                } else if(classname.equals("double") || classname.endsWith("Double")) {
                    double value = field.getDouble(entity);
                    contentValues.put(field.getName(), value);
                }
            }
        } catch (IllegalAccessException e) {
            return null;
        }
        return contentValues;
    }

}
