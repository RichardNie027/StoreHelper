package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.httprequest.use.RetrofitFactory;
import com.tlg.storehelper.httprequest.net.entity.CollocationEntity;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;

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

    public Observable<SimpleEntity<String>> loginValidation(String username, String password) {
        ArrayMap<String, Object> map = new ArrayMap<>();
        map.put("username", username);
        map.put("password", password);
        return mMainApi.loginValidation(map);
    }

    public Observable<SimpleEntity<String>> getGoodsBarcodes(String lastModDate) {
        return mMainApi.getGoodsBarcodes(lastModDate);
    }

    public Observable<BaseResponseEntity> uploadInventory(InventoryEntity inventoryEntity) {
        return mMainApi.uploadInventory(inventoryEntity);
    }

    public Observable<CollocationEntity> getCollocation(String goodsNo) {
        return mMainApi.getCollocation(goodsNo);
    }
}
