package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.tlg.storehelper.httprequest.net.entity.GoodsBarcodeEntity;
import com.tlg.storehelper.httprequest.net.entity.LoginEntity;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface RegentApi {

    /**
     * 接口描述：POST login
     * 接口参数：username String, password String
     * 接口返回：
     */
    @POST("login")
    Observable<LoginEntity> loginValidation(@Body ArrayMap<String, Object> map);

    /**
     * 接口描述：GET biz
     * 接口参数：timestamp int
     * 接口返回：GoodsBarcodeEntity
     */
    @GET("biz?method=getgoodsbarcodelist")
    Observable<GoodsBarcodeEntity> getGoodsBarcodes(@Query("timestamp") String timestamp);
}
