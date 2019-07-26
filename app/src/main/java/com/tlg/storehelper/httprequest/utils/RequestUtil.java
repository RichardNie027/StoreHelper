package com.tlg.storehelper.httprequest.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.httprequest.utils.ApiConfig;
import com.nec.lib.android.utils.DateUtil;
import com.nec.lib.android.utils.XxteaUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.net.AppBaseObserver;
import com.tlg.storehelper.httprequest.net.api.RegentService;
import com.tlg.storehelper.httprequest.net.entity.SimpleEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RequestUtil {

    private static void signRequest(Map params) {
        ArrayMap<String, String> headerMap = new ArrayMap<String, String>();
        headerMap.put("AppVersion", String.valueOf(MyApp.getVersionCode(MyApp.getInstance())));
        headerMap.put("ApiVersion", String.valueOf(1));
        headerMap.put("Timestamp", DateUtil.toStr(new Date(), "yyyyMMddHHmmss"));
        headerMap.put("Uid", GlobalVars.username);
        headerMap.put("Authorization", GlobalVars.token);   //Authorization在HttpHeaderInterceptor中添加
        //生成签名
        Map<String, String> sortedMap = new TreeMap<>();
        sortedMap.putAll(headerMap);
        sortedMap.putAll(params);
        StringBuffer signatureBuffer = new StringBuffer();
        for(String key: sortedMap.keySet()) {
            signatureBuffer.append(key).append(sortedMap.get(key));
        }
        String signature = "";
        String token = GlobalVars.token.isEmpty() ? "store_helper" : GlobalVars.token;
        try {
            signature = XxteaUtil.encryptBase64String(signatureBuffer.toString(), "UTF-8", token);
        } catch (Exception e) {}

        headerMap.put("Signature", signature);
        ApiConfig.setHeaders(headerMap);
    }

    static class RequestMap {
        public Map map = new HashMap();
        public RequestMap put(String param, String value) {
            map.put(param, value);
            return this;
        }
    }

    public static void requestAppVersion(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        RegentService.getInstance()
                .appAppVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleMapEntity>(activity, true, "APP版本检测") {

                    @Override
                    public void onSuccess(SimpleMapEntity response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestLogin(String username, String password, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        /*
        Map requestMap = new RequestMap()
                .put("username", username)
                .put("password", password)
                .map;
        signRequest(requestMap);
        */

        RegentService.getInstance()
                .loginValidation(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleEntity<String>>(activity, true, "登录验证中") {

                    @Override
                    public void onSuccess(SimpleEntity<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        GlobalVars.username = username;
                        GlobalVars.token = response.result_map.get("token").toString();
                        ApiConfig.setToken(GlobalVars.token);
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestGoodBarcodes(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        String lastModDate = pref.getString("lastModDate", "2000-01-01 00:00:00");
        SharedPreferences.Editor editor = pref.edit();

        Map requestMap = new RequestMap()
                .put("lastModDate", lastModDate)
                .map;
        signRequest(requestMap);

        RegentService.getInstance()
                .getGoodsBarcodes(lastModDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleEntity<String>>(activity, true,"正在同步商品资料") {
                    @Override
                    public void onFailing(SimpleEntity<String> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("同步商品资料失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimpleEntity<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(response.result_list.size() > 0) {
                            DbUtil.saveGoodsBarcodes(response.result_list);

                            String lastModDate = response.result_map.get("lastModDate").toString();
                            editor.putString("lastModDate", lastModDate);
                            editor.commit();
                        } else {
                            Toast.makeText(MyApp.getInstance(), response.msg, Toast.LENGTH_SHORT).show();
                        }
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
