package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.tlg.storehelper.httprequest.net.entity.CollocationEntity;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleListEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleListPageEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;
import com.tlg.storehelper.vo.GoodsSimpleVo;
import com.tlg.storehelper.vo.StockVo;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MainApi {

    /**
     * 接口描述：GET appAppVersion
     * 接口参数：
     * 接口返回：SimpleMapEntity
     */
    @GET("pre_api/appVersion")
    Observable<SimpleMapEntity> appAppVersion();

    /**
     * 接口描述：POST loginValidation
     * 接口参数：username String, password String
     * 接口返回：SimpleEntity<String>
     */
    @POST("pre_api/login")
    Observable<SimpleEntity<String>> loginValidation(@Body ArrayMap<String, Object> map);

    /**
     * 接口描述：GET getGoodsBarcodeList
     * 接口参数：
     * 接口返回：SimpleEntity<String>
     */
    @GET("api/getGoodsBarcodeList")
    Observable<SimpleEntity<String>> getGoodsBarcodes(@Query("lastModDate")String lastModDate);

    /**
     * 接口描述：POST uploadInventory
     * 接口参数：
     * 接口返回：InventoryEntity
     */
    @POST("api/uploadInventory")
    Observable<BaseResponseEntity> uploadInventory(@Body InventoryEntity inventoryEntity);

    /**
     * 接口描述：GET getCollocation
     * 接口参数：
     * 接口返回：InventoryEntity
     */
    @GET("api/getCollocation")
    Observable<CollocationEntity> getCollocation(@Query("goodsNo")String goodsNo);

    /**
     * 接口描述：GET getBestSelling
     * 接口参数：
     * 接口返回：SimpleListPageEntity<GoodsSimpleVo>
     */
    @GET("api/getBestSelling")
    Observable<SimpleListPageEntity<GoodsSimpleVo>> getBestSelling(@Query("storeCode")String storeCode, @Query("dim")String dim, @Query("page")int page);

    /**
     * 接口描述：GET getStoreStock
     * 接口参数：
     * 接口返回：SimpleListEntity<StockVo>
     */
    @GET("api/getStoreStock")
    Observable<SimpleEntity<StockVo>> getStoreStock(@Query("storeCode")String storeCode, @Query("goodsNo")String goodsNo);
}
