package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.httprequest.use.RetrofitFactory;
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

import io.reactivex.Observable;

public class MainApiService {

    private MainApi mMainApi;

    private MainApiService() {
        mMainApi = RetrofitFactory.getInstance().create(MainApi.class);
    }

    public static MainApiService getInstance() {
        return RegentServiceHolder.S_INSTANCE;
    }

    private static class RegentServiceHolder {
        private static final MainApiService S_INSTANCE = new MainApiService();
    }

    public Observable<SimpleMapEntity> appAppVersion() {
        return mMainApi.appAppVersion();
    }

    public Observable<SimpleListMapEntity<String>> loginValidation(String username, String password) {
        ArrayMap<String, Object> map = new ArrayMap<>();
        map.put("username", username);
        map.put("password", password);
        return mMainApi.loginValidation(map);
    }

    public Observable<SimpleListMapEntity<String>> getGoodsBarcodes(String lastModDate) {
        return mMainApi.getGoodsBarcodes(lastModDate);
    }

    public Observable<BaseResponseEntity> uploadInventory(InventoryEntity inventoryEntity) {
        return mMainApi.uploadInventory(inventoryEntity);
    }

    public Observable<CollocationEntity> getCollocation(String goodsNo) {
        return mMainApi.getCollocation(goodsNo);
    }

    public Observable<SimplePageListEntity<GoodsSimpleVo>> getBestSelling(String storeCode, String dim, int page) {
        return mMainApi.getBestSelling(storeCode, dim, page);
    }

    public Observable<SimpleListMapEntity<StockVo>> getStoreStock(String storeCode, String goodsNo) {
        return mMainApi.getStoreStock(storeCode, goodsNo);
    }

    public Observable<SimpleListEntity<MembershipVo>> getMembership(String membershipId, String storeCode) {
        return mMainApi.getMembership(membershipId, storeCode);
    }

    public Observable<SimplePageListEntity<ShopHistoryVo>> getMembershipShopHistory(String membershipId, String storeCode, int page) {
        return mMainApi.getMembershipShopHistory(membershipId, storeCode, page);
    }
}
