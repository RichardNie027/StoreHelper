package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;
import com.nec.lib.android.httprequest.use.RetrofitFactory;
import com.tlg.storehelper.httprequest.net.entity.CollocationResponseVo;
import com.tlg.storehelper.httprequest.net.entity.GoodsInfoResponseVo;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleListResponseVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapResponseVo;
import com.tlg.storehelper.httprequest.net.entity.SimplePageListResponseVo;
import com.tlg.storehelper.vo.GoodsPopularityVo;
import com.tlg.storehelper.vo.MembershipVo;
import com.tlg.storehelper.httprequest.net.entity.SimpleListMapResponseVo;
import com.tlg.storehelper.vo.GoodsSimpleVo;
import com.tlg.storehelper.vo.ShopHistoryVo;
import com.tlg.storehelper.vo.StockVo;

import io.reactivex.Observable;

public class MainApiService {

    private MainApi mMainApi;

    private MainApiService() {
        mMainApi = RetrofitFactory.getInstance().create(MainApi.class);
    }

    public static MainApiService getInstance() {
        return MainApiServiceHolder.S_INSTANCE;
    }

    private static class MainApiServiceHolder {
        private static final MainApiService S_INSTANCE = new MainApiService();
    }

    public Observable<SimpleMapResponseVo> appAppVersion() {
        return mMainApi.appAppVersion();
    }

    public Observable<SimpleListMapResponseVo<String>> loginValidation(String username, String password) {
        ArrayMap<String, Object> map = new ArrayMap<>();
        map.put("username", username);
        map.put("password", password);
        return mMainApi.loginValidation(map);
    }

    public Observable<GoodsInfoResponseVo> getGoodsList(String lastModDate) {
        return mMainApi.getGoodsList(lastModDate);
    }

    public Observable<SimpleListResponseVo<GoodsPopularityVo>> getGoodsPopularity(String storeCode) {
        return mMainApi.getGoodsPopularity(storeCode);
    }

    public Observable<BaseResponseVo> uploadInventory(InventoryEntity inventoryEntity) {
        return mMainApi.uploadInventory(inventoryEntity);
    }

    public Observable<CollocationResponseVo> getCollocation(String goodsNo) {
        return mMainApi.getCollocation(goodsNo);
    }

    public Observable<SimplePageListResponseVo<GoodsSimpleVo>> getBestSelling(String storeCode, String dim, int page) {
        return mMainApi.getBestSelling(storeCode, dim, page);
    }

    public Observable<SimpleListMapResponseVo<StockVo>> getStoreStock(String storeCode, String goodsNo) {
        return mMainApi.getStoreStock(storeCode, goodsNo);
    }

    public Observable<SimpleListResponseVo<MembershipVo>> getMembership(String membershipId, String storeCode) {
        return mMainApi.getMembership(membershipId, storeCode);
    }

    public Observable<SimplePageListResponseVo<ShopHistoryVo>> getMembershipShopHistory(String membershipId, String storeCode, int page) {
        return mMainApi.getMembershipShopHistory(membershipId, storeCode, page);
    }
}
