package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.tlg.storehelper.httprequest.net.entity.GoodsBarcodeEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleListEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RegentApi {

    /**
     * 接口描述：POST login
     * 接口参数：username String, password String
     * 接口返回：
     */
    @POST("login")
    Observable<SimpleListEntity<String>> loginValidation(@Body ArrayMap<String, Object> map);

    /**
     * 接口描述：GET getToken
     * 接口参数：
     * 接口返回：SimpleMapEntity - - map result - - token
     */
    @GET("getToken")
    Observable<SimpleMapEntity> getToken();

    /**
     * 接口描述：GET getGoodsBarcodeList
     * 接口参数：
     * 接口返回：SimpleListEntity<String>
     */
    @GET("getGoodsBarcodeList")
    Observable<SimpleListEntity<String>> getGoodsBarcodes();
}
