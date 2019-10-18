package com.tlg.storehelper.httprequest.net;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;
import com.nec.lib.android.httprequest.use.BaseObserver;
import com.nec.lib.android.httprequest.utils.AppContextUtil;
import com.tlg.storehelper.MyApp;

public abstract class AppBaseObserver<T extends BaseResponseVo> extends BaseObserver<T> {
    public AppBaseObserver() {
        super();
    }

    public AppBaseObserver(Context context, boolean isShow) {
        super(context, isShow);
    }

    public AppBaseObserver(Context context, boolean isShow, String msg) {
        super(context, isShow, msg);
    }

    @Override
    public abstract void onSuccess(T response);

    @Override
    public void onFailing(T response) {
        int code = response.getCode();
        if (code == 404)
            Toast.makeText(AppContextUtil.getContext(), "服务器找不到请求目标", Toast.LENGTH_SHORT).show();
        else if (code == 500)
            Toast.makeText(AppContextUtil.getContext(), "服务器遇到错误，无法完成请求", Toast.LENGTH_SHORT).show();
        else if (code >= 900 && code < 999)
            new android.app.AlertDialog.Builder(MyApp.getInstance())
                    .setTitle("网络请求失败")
                    .setMessage(response.msg)
                    .setPositiveButton("确定", null)
                    .show();
        else
            super.onFailing(response);
    }

    @Override
    protected void onRequestStart() {
        super.onRequestStart();
        Log.d(MyApp.getCurrentActivity().getClass().getName(), "请求开始");
    }

    @Override
    protected void onRequestEnd() {
        super.onRequestEnd();
        Log.d(MyApp.getCurrentActivity().getClass().getName(), "请求结束");
    }

}
