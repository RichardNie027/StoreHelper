package com.nec.utils;

import java.lang.reflect.Field;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import com.nec.application.MyApplication;

public class ResourceUtil {

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getLayoutId(String paramString) {
        return ResourceUtil.getLayoutId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getLayoutId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "layout", context.getPackageName());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getStringId(String paramString) {
        return ResourceUtil.getStringId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getStringId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "string",
                context.getPackageName());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getDrawableId(String paramString) {
        return ResourceUtil.getDrawableId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getDrawableId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "drawable", context.getPackageName());
    }

    public static int getStyleId(String paramString) {
        return ResourceUtil.getStyleId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getStyleId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "style",
                context.getPackageName());
    }

    public static int getId(String paramString) {
        return ResourceUtil.getId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "id",
                context.getPackageName());
    }

    public static int getColor(String paramString) {
        return ResourceUtil.getColor(paramString, MyApplication.getInstance());
    }

    /**获取颜色值*/
    public static int getColor(String paramString, Context context) {
        int colorId = getColorId(paramString, context);
        if (colorId == 0)
            return 0;
        else {
            int color = context.getResources().getColor(colorId);
            return color;
        }
    }

    public static int getColorId(String paramString) {
        return ResourceUtil.getColorId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getColorId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "color", context.getPackageName());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getDimenId(String paramString) {
        return ResourceUtil.getDimenId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getDimenId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "dimen",
                context.getPackageName());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getAnimId(String paramString) {
        return ResourceUtil.getAnimId(paramString, MyApplication.getInstance());
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public static int getAnimId(String paramString, Context context) {
        if (context == null)
            return 0;
        return context.getResources().getIdentifier(paramString, "anim", context.getPackageName());
    }

    // 通过反射实现
    public static final int[] getStyleableIntArray(String name) {
        return ResourceUtil.getStyleableIntArray(name, MyApplication.getInstance());
    }

    public static final int[] getStyleableIntArray(String name, Context context) {
        try {
            if (context == null)
                return null;
            Field field = Class.forName(context.getPackageName() + ".R$styleable").getDeclaredField(name);
            int[] ret = (int[]) field.get(null);
            return ret;
        } catch (Throwable t) {
        }
        return null;
    }

    public static final int getStyleableIntArrayIndex(String name) {
        return ResourceUtil.getStyleableIntArrayIndex(name, MyApplication.getInstance());
    }

    public static final int getStyleableIntArrayIndex(String name, Context context) {
        try {
            if (context == null)
                return 0;
            // use reflection to access the resource class
            Field field = Class.forName(context.getPackageName() + ".R$styleable").getDeclaredField(name);
            int ret = (Integer) field.get(null);
            return ret;
        } catch (Throwable t) {
        }
        return 0;
    }

    /**
     * 产生shape类型的drawable
     *
     * DEMO：
     * GradientDrawable()
     <?xml version="1.0" encoding="utf-8"?>
     <shape xmlns:android="http://schemas.android.com/apk/res/android">
        <solid android:color="@color/white" />
        <corners android:radius="5dp" />
        <stroke android:width="1dp" android:color="@color/red" />
     </shape>

     * @param solidColor
     * @param strokeColor
     * @param strokeWidth
     * @param radius
     * @return
     */
    public static GradientDrawable getDrawable(int solidColor, int strokeColor, int strokeWidth, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(solidColor);
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setCornerRadius(radius);
        return drawable;
    }
}
