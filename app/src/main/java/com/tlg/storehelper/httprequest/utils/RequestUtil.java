package com.tlg.storehelper.httprequest.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.nec.lib.base.BaseRxAppCompatActivity;
import com.nec.lib.httprequest.net.revert.BaseResponseEntity;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.net.AppBaseObserver;
import com.tlg.storehelper.httprequest.net.api.RegentService;
import com.tlg.storehelper.httprequest.net.entity.SimpleListEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RequestUtil {

    public static void requestToken(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        RegentService.getInstance()
                .getToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleMapEntity>(activity, true) {

                    @Override
                    public void onSuccess(SimpleMapEntity response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        GlobalVars.token = response.result.get("token").toString();
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestGoodBarcodes(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        RegentService.getInstance()
                .getGoodsBarcodes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListEntity<String>>(activity, true,"正在同步商品资料") {

                    @Override
                    public void onSuccess(SimpleListEntity<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        DbUtil.saveGoodsBarcodes(response.result);
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestLogin(String username, String password, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        RegentService.getInstance()
                .loginValidation(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListEntity<String>>(activity, true, "登录验证中") {

                    @Override
                    public void onSuccess(SimpleListEntity<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        GlobalVars.username = username;
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public interface OnSuccessListener<ResponseEntity extends BaseResponseEntity> {
        /**
         * 网络请求返回成功的监听事件
         */
        public void onSuccess(ResponseEntity response);
    }

}
