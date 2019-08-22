package com.tlg.storehelper.httprequest.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.httprequest.use.DownloaderManager;
import com.nec.lib.android.httprequest.utils.ApiConfig;
import com.nec.lib.android.httprequest.utils.DownloadFileUtil;
import com.nec.lib.android.utils.BeanUtil;
import com.nec.lib.android.utils.DateUtil;
import com.nec.lib.android.utils.XxteaUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.net.AppBaseObserver;
import com.tlg.storehelper.httprequest.net.api.MainApiService;
import com.tlg.storehelper.httprequest.net.entity.CollocationEntity;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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
        Map<String, Object> sortedMap = new TreeMap<>();
        sortedMap.putAll(headerMap);
        sortedMap.putAll(params);
        StringBuffer signatureBuffer = new StringBuffer();
        for(String key: sortedMap.keySet()) {
            signatureBuffer.append(key).append(sortedMap.get(key).toString());
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
        public RequestMap putAll(Map _map) {
            map.putAll(_map);
            return this;
        }
    }

    public static void requestAppVersion(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        MainApiService.getInstance()
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

        MainApiService.getInstance()
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

        MainApiService.getInstance()
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

    public static void requestUploadInventory(InventoryEntity inventoryEntity, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map beanMap = BeanUtil.objectToMap(inventoryEntity);
        Map requestMap = new RequestMap()
                .putAll(beanMap)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .uploadInventory(inventoryEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<BaseResponseEntity>(activity, true, "上传盘点单") {

                    @Override
                    public void onSuccess(BaseResponseEntity response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        Toast.makeText(MyApp.getInstance(), response.msg, Toast.LENGTH_SHORT).show();
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestCollocation(String goodsNo, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("goodsNo", goodsNo)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getCollocation(goodsNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<CollocationEntity>(activity, true,"正在获取连带信息") {
                    @Override
                    public void onFailing(CollocationEntity response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取连带信息失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(CollocationEntity response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void downloadPic(String url, @NonNull BaseRxAppCompatActivity activity, OnFileDownloadedListener onFileDownloadedListener) {
        DownloaderManager downloaderManager = new DownloaderManager(url, new DownloaderManager.ProgressListener() {
            @Override
            public void onPreExecute(long contentLength) {

            }

            @Override
            public void update(long totalBytes, boolean done) {
                if (done) {
                    //下载文件的路径
                    String pathFile = null;
                    try {
                        pathFile = DownloadFileUtil.getDownloadPath(url, "/StoreHelper/pic");
                    } catch (IOException e) {}
                    if(onFileDownloadedListener != null)
                        if(pathFile != null)
                            onFileDownloadedListener.onSuccess(pathFile);
                        else
                            onFileDownloadedListener.onFail();
                }
            }

            @Override
            public void onFail() {
                if(onFileDownloadedListener != null)
                    onFileDownloadedListener.onFail();
            }
        });
        downloaderManager.setSavePath("/StoreHelper/pic").downloadStartPoint(0L);
    }

    public interface OnSuccessListener<ResponseEntity extends BaseResponseEntity> {
        /**
         * 网络请求返回成功的监听事件
         */
        public void onSuccess(ResponseEntity response);
    }

    public interface OnFileDownloadedListener {
        /**
         * 下载成功的监听事件
         */
        public void onSuccess(String pathFile);

        /**
         * 下载失败的监听事件
         */
        public void onFail();
    }

}
