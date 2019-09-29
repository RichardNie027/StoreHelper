package com.tlg.storehelper.httprequest.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.httprequest.utils.ApiConfig;
import com.nec.lib.android.utils.AndroidUtil;
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
import com.tlg.storehelper.httprequest.net.entity.SimpleListEntity;
import com.tlg.storehelper.vo.MembershipVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleListMapEntity;
import com.tlg.storehelper.httprequest.net.entity.SimplePageListEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;
import com.tlg.storehelper.vo.GoodsSimpleVo;
import com.tlg.storehelper.vo.ShopHistoryVo;
import com.tlg.storehelper.vo.StockVo;

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
        headerMap.put("Auth", GlobalVars.token);   //Authorization在HttpHeaderInterceptor中添加
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
                .subscribe(new AppBaseObserver<SimpleListMapEntity<String>>(activity, true, "登录验证中") {

                    @Override
                    public void onSuccess(SimpleListMapEntity<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        GlobalVars.username = username;
                        GlobalVars.token = response.map.get("token").toString();
                        ApiConfig.setToken(GlobalVars.token);
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestGoodBarcodes(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener, OnFailingListener onFailingListener, OnRequestEndListener onRequestEndListener) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        String lastModDate = pref.getString("lastModDate", "20000101000000");
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
                .subscribe(new AppBaseObserver<SimpleListMapEntity<String>>(activity, true,"正在同步商品资料") {
                    @Override
                    public void onFailing(SimpleListMapEntity<String> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("同步商品资料失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                        if(onFailingListener != null)
                            onFailingListener.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimpleListMapEntity<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(response.list.size() > 0) {
                            DbUtil.saveGoodsBarcodes(response.list, lastModDate.equals("20000101000000"));

                            String lastModDate = response.map.get("lastModDate").toString();
                            editor.putString("lastModDate", lastModDate);
                            editor.commit();
                        } else {
                            AndroidUtil.showToast(response.msg);
                        }
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }

                    @Override
                    protected void onRequestEnd() {
                        super.onRequestEnd();
                        if(onRequestEndListener != null)
                            onRequestEndListener.onRequestEnd();
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
                        AndroidUtil.showToast(response.msg);
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

    public static void requestBestSelling(String storeCode, String dim, int page, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("storeCode", storeCode)
                .put("dim", dim)
                .put("page", String.valueOf(page))
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getBestSelling(storeCode, dim, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimplePageListEntity<GoodsSimpleVo>>(activity, false,"正在获取畅销款") {
                    @Override
                    public void onFailing(SimplePageListEntity<GoodsSimpleVo> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取畅销款信息失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimplePageListEntity<GoodsSimpleVo> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestStoreStock(String storeCode, String goodsNo, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("storeCode", storeCode)
                .put("goodsNo", goodsNo)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getStoreStock(storeCode, goodsNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListMapEntity<StockVo>>(activity, false,"正在获取库存") {
                    @Override
                    public void onFailing(SimpleListMapEntity<StockVo> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取库存失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimpleListMapEntity<StockVo> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestMembership(String membershipId, String storeCode, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("membershipId", membershipId)
                .put("storeCode", storeCode)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getMembership(membershipId, storeCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListEntity<MembershipVo>>(activity, false,"正在获取消费记录") {
                    @Override
                    public void onFailing(SimpleListEntity<MembershipVo> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取消费记录失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimpleListEntity<MembershipVo> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestMembershipShopHistory(String membershipId, String storeCode, int page, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("membershipId", membershipId)
                .put("storeCode", storeCode)
                .put("page", String.valueOf(page))
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getMembershipShopHistory(membershipId, storeCode, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimplePageListEntity<ShopHistoryVo>>(activity, false,"正在获取消费记录") {
                    @Override
                    public void onFailing(SimplePageListEntity<ShopHistoryVo> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取消费记录失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimplePageListEntity<ShopHistoryVo> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }





    /**
    @Deprecated // 废除的下载方法（BUG:多次请求、字节流中部分字节丢失）
    public static void downloadPic(String url, OnFileDownloadedListener onFileDownloadedListener) {
        DownloaderManager downloaderManager = new DownloaderManager(url, new DownloaderManager.ProgressListener() {
            @Override
            public void onPreExecute(long contentLength) {

            }

            @Override
            public void update(long totalBytes, boolean done) {
                if (done && totalBytes > 0) {
                    //下载文件的路径
                    String pathFile = null;
                    try {
                        pathFile = DownloadFileUtil.getDownloadPath(url, "/StoreHelper/pic");
                    } catch (IOException e) {}
                    if(onFileDownloadedListener != null)
                        if(pathFile != null)
                            onFileDownloadedListener.onSuccess(pathFile);
                        else {
                            File file = new File(pathFile);
                            if(file.exists() && file.length()==0)
                                file.delete();
                            onFileDownloadedListener.onFail();
                        }
                }
            }

            @Override
            public void onFail() {
                try {
                    String pathFile = DownloadFileUtil.getDownloadPath(url, "/StoreHelper/pic");
                    File file = new File(pathFile);
                    if(file.exists() && file.length()==0)
                        file.delete();
                } catch (IOException e) {}
                if(onFileDownloadedListener != null)
                    onFileDownloadedListener.onFail();
            }
        });
        downloaderManager.setSavePath("/StoreHelper/pic").downloadStartPoint(0L);
    }
    public interface OnFileDownloadedListener {
        //下载成功的监听事件
        public void onSuccess(String pathFile);

        //下载失败的监听事件
        public void onFail();
    }
    */

    public interface OnSuccessListener<ResponseEntity extends BaseResponseEntity> {
        /**
         * 网络请求返回成功的监听事件
         */
        public void onSuccess(ResponseEntity response);
    }

    public interface OnFailingListener<ResponseEntity extends BaseResponseEntity> {
        /**
         * 网络请求返回失败的监听事件
         */
        public void onFailing(ResponseEntity response);
    }

    public interface OnRequestEndListener {
        /**
         * 网络请求结束的监听事件
         */
        public void onRequestEnd();
    }

}
