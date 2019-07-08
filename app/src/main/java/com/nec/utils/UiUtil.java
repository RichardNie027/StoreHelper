package com.nec.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.nec.application.MyApplication;

public class UiUtil {

    public static int dp2px(float dpValue) {
        return dp2px(MyApplication.getInstance(), dpValue);
    }

    public static int sp2px(float spValue) {
        return sp2px(MyApplication.getInstance(), spValue);
    }

    /**
     * 单位大小dp转为像素px
     *
     * @param context  上下文
     * @param dpValue dp值
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 文字大小sp转为像素px
     * @param context 上下文
     * @param spValue sp值
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return Math.round((spValue - 0.5f) * fontScale);
    }

    /**屏幕宽度（像素）*/
    public static int sScreenWidthInPx = 0;
    /**屏幕高度（像素）*/
    public static int sScreenHeightInPx = 0;
    /**屏幕宽度（dp）*/
    public static int sScreenWidthInDp = 0;
    /**屏幕高度（dp）*/
    public static int sScreenHeightInDp = 0;
    /**屏幕密度（0.75 / 1.0 / 1.5）*/
    public static float sScreenDensity = 1;
    /**屏幕密度dpi（120 / 160 / 240）*/
    public static int sScreenDensityDpi = 160;

    /**生成屏幕尺寸，全局静态*/
    public static void getAndroiodScreenProperty(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        sScreenWidthInPx = dm.widthPixels;          // 屏幕宽度（像素）
        sScreenHeightInPx = dm.heightPixels;        // 屏幕高度（像素）
        sScreenDensity = dm.density;                // 屏幕密度（0.75 / 1.0 / 1.5）
        sScreenDensityDpi = dm.densityDpi;          // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        sScreenWidthInDp = (int) (sScreenWidthInPx / sScreenDensity);   // 屏幕宽度(dp)
        sScreenHeightInDp = (int) (sScreenHeightInPx / sScreenDensity); // 屏幕高度(dp)
    }
}
