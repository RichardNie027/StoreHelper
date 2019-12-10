package com.tlg.storehelper.httprequest.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;
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
import com.tlg.storehelper.httprequest.net.entity.CollocationVo;
import com.tlg.storehelper.httprequest.net.entity.GoodsInfoResponseVo;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleListResponseVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleListMapResponseVo;
import com.tlg.storehelper.httprequest.net.entity.SimplePageListResponseVo;
import com.tlg.storehelper.vo.GoodsPopularityVo;
import com.tlg.storehelper.vo.GoodsPsiVo;
import com.tlg.storehelper.vo.MembershipVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapResponseVo;
import com.tlg.storehelper.vo.GoodsSimpleVo;
import com.tlg.storehelper.vo.ShopHistoryVo;

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
                .subscribe(new AppBaseObserver<SimpleMapResponseVo>(activity, true, "APP版本检测") {

                    @Override
                    public void onSuccess(SimpleMapResponseVo response) {
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
                .subscribe(new AppBaseObserver<SimpleListMapResponseVo<String>>(activity, true, "登录验证中") {

                    @Override
                    public void onSuccess(SimpleListMapResponseVo<String> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        GlobalVars.username = username;
                        GlobalVars.token = response.map.get("token").toString();
                        ApiConfig.setToken(GlobalVars.token);
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestGoodsList(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener, OnFailingListener onFailingListener, OnRequestEndListener onRequestEndListener) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        String defaultLastModDate = "20000101000000";
        String lastModDate = pref.getString("lastModDate", defaultLastModDate);
        SharedPreferences.Editor editor = pref.edit();

        Map requestMap = new RequestMap()
                .put("lastModDate", lastModDate)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getGoodsList(lastModDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<GoodsInfoResponseVo>(activity, true,"正在同步商品资料") {
                    @Override
                    public void onFailing(GoodsInfoResponseVo response) {
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
                    public void onSuccess(GoodsInfoResponseVo response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(!response.goodsList.isEmpty() || !response.goodsBarcodeList.isEmpty()) {
                            DbUtil.saveGoodsList(response.goodsList, response.goodsBarcodeList, lastModDate.equals("20000101000000"));
                            response.lastModDate = response.lastModDate.isEmpty() ? defaultLastModDate : response.lastModDate;
                            editor.putString("lastModDate", response.lastModDate);
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

    public static void requestGoodsPopularity(@NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener, OnFailingListener onFailingListener, OnRequestEndListener onRequestEndListener) {
        Map requestMap = new RequestMap()
                .put("storeCode", GlobalVars.storeCode)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getGoodsPopularity(GlobalVars.storeCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListResponseVo<GoodsPopularityVo>>(activity, false,"正在获取商品热度") {
                    @Override
                    public void onFailing(SimpleListResponseVo<GoodsPopularityVo> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取商品热度失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                        if(onFailingListener != null)
                            onFailingListener.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimpleListResponseVo<GoodsPopularityVo> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(!response.list.isEmpty()) {
                            DbUtil.saveGoodsPopularity(response.list);
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
                .subscribe(new AppBaseObserver<BaseResponseVo>(activity, true, "上传盘点单") {

                    @Override
                    public void onSuccess(BaseResponseVo response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        AndroidUtil.showToast(response.msg);
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestCollocation(String goodsNo, String storeCodes, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("goodsNo", goodsNo)
                .put("storeCodes", storeCodes)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getCollocation(goodsNo, storeCodes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<CollocationVo>(activity, true,"正在获取连带信息") {
                    @Override
                    public void onFailing(CollocationVo response) {
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
                    public void onSuccess(CollocationVo response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestBestSelling(String storeCodes, String dim, int floorNumber, int page, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("storeCodes", storeCodes)
                .put("dim", dim)
                .put("floorNumber", String.valueOf(floorNumber))
                .put("page", String.valueOf(page))
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getBestSelling(storeCodes, dim, floorNumber, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimplePageListResponseVo<GoodsSimpleVo>>(activity, false,"正在获取畅销款") {
                    @Override
                    public void onFailing(SimplePageListResponseVo<GoodsSimpleVo> response) {
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
                    public void onSuccess(SimplePageListResponseVo<GoodsSimpleVo> response) {
                        Log.d(activity.getClass().getName(), "请求成功");
                        if(onSuccessListener != null)
                            onSuccessListener.onSuccess(response);
                    }
                });
    }

    public static void requestStorePsi(String storeCode, String goodsNo, @NonNull BaseRxAppCompatActivity activity, OnSuccessListener onSuccessListener) {
        Map requestMap = new RequestMap()
                .put("storeCode", storeCode)
                .put("goodsNo", goodsNo)
                .map;
        signRequest(requestMap);

        MainApiService.getInstance()
                .getStorePsi(storeCode, goodsNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(activity.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListResponseVo<GoodsPsiVo>>(activity, false,"正在获取进销存") {
                    @Override
                    public void onFailing(SimpleListResponseVo<GoodsPsiVo> response) {
                        int code = response.getCode();
                        if (code >= 900 && code < 999) {
                            new android.app.AlertDialog.Builder(MyApp.getInstance())
                                    .setTitle("获取进销存失败")
                                    .setMessage(response.msg)
                                    .setPositiveButton("确定", null)
                                    .show();
                        } else
                            super.onFailing(response);
                    }

                    @Override
                    public void onSuccess(SimpleListResponseVo<GoodsPsiVo> response) {
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
                .subscribe(new AppBaseObserver<SimpleListResponseVo<MembershipVo>>(activity, false,"正在获取消费记录") {
                    @Override
                    public void onFailing(SimpleListResponseVo<MembershipVo> response) {
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
                    public void onSuccess(SimpleListResponseVo<MembershipVo> response) {
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
                .subscribe(new AppBaseObserver<SimplePageListResponseVo<ShopHistoryVo>>(activity, false,"正在获取消费记录") {
                    @Override
                    public void onFailing(SimplePageListResponseVo<ShopHistoryVo> response) {
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
                    public void onSuccess(SimplePageListResponseVo<ShopHistoryVo> response) {
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

    public interface OnSuccessListener<ResponseEntity extends BaseResponseVo> {
        /**
         * 网络请求返回成功的监听事件
         */
        public void onSuccess(ResponseEntity response);
    }

    public interface OnFailingListener<ResponseEntity extends BaseResponseVo> {
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
