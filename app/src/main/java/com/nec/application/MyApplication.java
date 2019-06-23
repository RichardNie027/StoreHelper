package com.nec.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/*
在AndroidManifest.xml文件的<application>标签下进行指定:
<application
    android:name="com.nec.application.MyApplication"
 */
public class MyApplication extends Application {

    /** 用来保存当前该Application的context */
    private static Context instance;
    /** 用来保存最新打开页面的context */
    private volatile static WeakReference<Context> instanceRef = null;

    /**
     * 取得最新Activity或Application的context弱引用
     * @return 优先返回最新Activity的context弱引用
     */
    public static Context getInstance(){
        //if (instanceRef == null || instanceRef.get() == null){
            synchronized (MyApplication.class) {
                //if (instanceRef == null || instanceRef.get() == null) {
                    Context context = ActivityManager.getInstance().getCurrentActivity();
                    if (context != null)
                        instanceRef = new WeakReference<>(context);
                    else {
                        instanceRef = new WeakReference<>(instance);
                    }
                }
            //}
        //}
        return instanceRef.get();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityManager.getInstance().addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                ActivityManager.getInstance().removeActivity(activity);
            }
        });
    }

}