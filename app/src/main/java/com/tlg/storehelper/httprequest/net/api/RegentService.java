package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.httprequest.use.RetrofitFactory;
import com.tlg.storehelper.httprequest.net.entity.GoodsBarcodeEntity;

import io.reactivex.Observable;

public class RegentService {

    private RegentApi mRegentApi;

    private RegentService() {
        mRegentApi = RetrofitFactory.getInstance().create(RegentApi.class);
    }

    public static RegentService getInstance() {
        return RegentServiceHolder.S_INSTANCE;
    }

    private static class RegentServiceHolder {
        private static final RegentService S_INSTANCE = new RegentService();
    }

    public Observable<GoodsBarcodeEntity> getGoodsBarcodes() {
        ArrayMap<String, Object> map = new ArrayMap<>();
        return mRegentApi.getGoodsBarcodes(map);
    }
}
