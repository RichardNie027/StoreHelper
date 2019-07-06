package com.nec.boost;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nec.utils.ResourceUtil;

public class CustomDialog extends Dialog implements View.OnClickListener {

    //在构造方法里提前加载了样式
    private Context mContext;   //上下文
    private int mLayoutResID;   //布局文件id
    public Drawable drawable; //背景Drawable
    public boolean modal;     //是否模式对话框
    private int[] mListenedItem;//监听的控件id
    private OnDialogItemClickListener mListener;

    public CustomDialog(Context context, int layoutResID, int themeResId, int[] listenedItem) {
        super(context, themeResId);     //加载dialog的样式
        this.mContext = context;
        this.mLayoutResID = layoutResID;
        this.drawable = ResourceUtil.getDrawable(ResourceUtil.getColor("whilte"), ResourceUtil.getColor("whilte"), 0, 0);
        this.modal = false;
        this.mListenedItem = listenedItem;
    }

    public CustomDialog(Context context, int layoutResID, int themeResId, Drawable drawable, boolean modal, int[] listenedItem) {
        super(context, themeResId);     //加载dialog的样式
        this.mContext = context;
        this.mLayoutResID = layoutResID;
        this.drawable = drawable;
        this.modal = modal;
        this.mListenedItem = listenedItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //提前设置Dialog的一些样式
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);//设置dialog显示居中
        //dialogWindow.setWindowAnimations();设置动画效果
        setContentView(mLayoutResID);
        this.getWindow().getDecorView().setBackground(drawable);

        WindowManager windowManager = ((Activity)mContext).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth()*4/5;  // 设置dialog宽度为屏幕的4/5
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(!modal); //点击外部Dialog消失
        //遍历控件id添加点击注册
        for(int id: mListenedItem){
            findViewById(id).setOnClickListener(this);
        }
    }

    public interface OnDialogItemClickListener {
        void OnDialogItemClick(CustomDialog dialog, View view);
    }

    public void setOnDialogItemClickListener(OnDialogItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View view) {
        mListener.OnDialogItemClick(this, view);
        dismiss();  //按任何一个通过参数传入的控件，弹窗都会消失
    }
}
