package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;
import com.tlg.storehelper.httprequest.net.entity.CollocationResponseVo;
import com.tlg.storehelper.httprequest.net.entity.GoodsInfoResponseVo;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleListResponseVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleListMapResponseVo;
import com.tlg.storehelper.vo.GoodsPopularityVo;
import com.tlg.storehelper.vo.MembershipVo;
import com.tlg.storehelper.httprequest.net.entity.SimplePageListResponseVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapResponseVo;
import com.tlg.storehelper.vo.GoodsSimpleVo;
import com.tlg.storehelper.vo.ShopHistoryVo;
import com.tlg.storehelper.vo.GoodsPsiVo;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MainApi {

    /**
     * 接口描述：GET appAppVersion
     * 接口参数：
     * 接口返回：SimpleMapResponseVo
     */
    @GET("pre_api/appVersion")
    Observable<SimpleMapResponseVo> appAppVersion();

    /**
     * 接口描述：POST loginValidation
     * 接口参数：username String, password String
     * 接口返回：SimpleListMapResponseVo<String>
     */
    @POST("pre_api/login")
    Observable<SimpleListMapResponseVo<String>> loginValidation(@Body ArrayMap<String, Object> map);

    /**
     * 接口描述：GET getGoodsBarcodeList
     * 接口参数：
     * 接口返回：GoodsInfoResponseVo
     */
    @GET("api/getGoodsList")
    Observable<GoodsInfoResponseVo> getGoodsList(@Query("lastModDate")String lastModDate);

    /**
     * 接口描述：GET getGoodsPopularity
     * 接口参数：
     * 接口返回：SimpleListResponseVo<GoodsPopularityVo>
     */
    @GET("api/getGoodsPopularity")
    Observable<SimpleListResponseVo<GoodsPopularityVo>> getGoodsPopularity(@Query("storeCode")String storeCode);

    /**
     * 接口描述：POST uploadInventory
     * 接口参数：
     * 接口返回：InventoryEntity
     */
    @POST("api/uploadInventory")
    Observable<BaseResponseVo> uploadInventory(@Body InventoryEntity inventoryEntity);

    /**
     * 接口描述：GET getCollocation
     * 接口参数：
     * 接口返回：InventoryEntity
     */
    @GET("api/getCollocation")
    Observable<CollocationResponseVo> getCollocation(@Query("goodsNo")String goodsNo);

    /**
     * 接口描述：GET getBestSelling
     * 接口参数：
     * 接口返回：SimplePageListResponseVo<GoodsSimpleVo>
     */
    @GET("api/getBestSelling")
    Observable<SimplePageListResponseVo<GoodsSimpleVo>> getBestSelling(@Query("storeCode")String storeCode, @Query("dim")String dim, @Query("page")int page);

    /**
     * 接口描述：GET getStorePsi
     * 接口参数：
     * 接口返回：SimpleListResponseVo<GoodsPsiVo>
     */
    @GET("api/getStorePsi")
    Observable<SimpleListResponseVo<GoodsPsiVo>> getStorePsi(@Query("storeCode")String storeCode, @Query("goodsNo")String goodsNo);

    /**
     * 接口描述：GET getMembership
     * 接口参数：
     * 接口返回：SimpleListResponseVo<MembershipVo>
     */
    @GET("api/getMembership")
    Observable<SimpleListResponseVo<MembershipVo>> getMembership(@Query("membershipId")String membershipId, @Query("storeCode")String storeCode);

    /**
     * 接口描述：GET getMembershipShopHistory
     * 接口参数：
     * 接口返回：SimpleListResponseVo<ShopHistoryVo>
     */
    @GET("api/getMembershipShopHistory")
    Observable<SimplePageListResponseVo<ShopHistoryVo>> getMembershipShopHistory(@Query("membershipId")String membershipId, @Query("storeCode")String storeCode, @Query("page")int page);

}
