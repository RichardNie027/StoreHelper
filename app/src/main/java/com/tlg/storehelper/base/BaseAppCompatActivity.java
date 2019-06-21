package com.tlg.storehelper.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class BaseAppCompatActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnTouchListener {

    //全屏，隐藏系统顶部状态栏
    protected boolean mFullScreen = false;
    //
    protected View[] mHideInputViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        if(mFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        //用于控件失去焦点时隐藏输入法
        if (!hasFocus) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //用于隐藏输入法
        if(EditText.class.isAssignableFrom(view.getClass())) {
            ((EditText) view).setInputType(InputType.TYPE_NULL);
            return false;
        } else
            return true;
    }

    /**
     *     设置控件的OnFocusChangeListener
     *     用于控件失去焦点时隐藏输入法。
     *
     *     在Activity顶层布局中，需要设置：
     *     android:clickable="true"
     *     android:focusableInTouchMode="true"
     */
    public void setOnFocusChangeListener(View... views) {
        for(View view: views) {
            if(view != null)
                view.setOnFocusChangeListener(this);
        }
    }

    /**
     *    设置控件的OnTouchListener
     *    用于隐藏输入法。
     */
    protected void setHideInputViews(View... views) {
        for(View view: views) {
            if(view != null)
                view.setOnTouchListener(this);
        }
        mHideInputViews = views;
    }

}
