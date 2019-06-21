package com.tlg.storehelper.utils;

import java.lang.reflect.Field;
import android.content.Context;
import android.graphics.Color;

public class ResourceUtil {
    private Context mContext;

    public void init(Context context) {
        if (context != null)
            mContext = context;
    }

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public int getLayoutId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "layout", mContext.getPackageName());
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
    public int getStringId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "string",
                mContext.getPackageName());
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
    public int getDrawableId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "drawable", mContext.getPackageName());
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

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public int getStyleId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "style",
                mContext.getPackageName());
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

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public int getId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "id",
                mContext.getPackageName());
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

    /**
     *
     * @param paramString String resource name.
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public int getColorId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "color", mContext.getPackageName());
    }
    /**获取颜色值*/
    public int getColor(String paramString) {
        int colorId = getColorId(paramString);
        if (colorId == 0)
            return 0;
        else {
            int color = mContext.getResources().getColor(colorId);
            return color;
        }
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
    public int getDimenId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "dimen",
                mContext.getPackageName());
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
    public int getAnimId(String paramString) {
        if (mContext == null)
            return 0;
        return mContext.getResources().getIdentifier(paramString, "anim", mContext.getPackageName());
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
    public final int[] getStyleableIntArray(String name) {
        try {
            if (mContext == null)
                return null;
            Field field = Class.forName(mContext.getPackageName() + ".R$styleable").getDeclaredField(name);
            int[] ret = (int[]) field.get(null);
            return ret;
        } catch (Throwable t) {
        }
        return null;
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

    public final int getStyleableIntArrayIndex(String name) {
        try {
            if (mContext == null)
                return 0;
            // use reflection to access the resource class
            Field field = Class.forName(mContext.getPackageName() + ".R$styleable").getDeclaredField(name);
            int ret = (Integer) field.get(null);
            return ret;
        } catch (Throwable t) {
        }
        return 0;
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
}
