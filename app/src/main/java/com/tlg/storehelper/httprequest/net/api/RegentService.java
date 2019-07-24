package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.nec.lib.android.httprequest.use.RetrofitFactory;
import com.tlg.storehelper.httprequest.net.entity.SimpleEntity;

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

    public Observable<SimpleEntity<String>> loginValidation(String username, String password) {
        ArrayMap<String, Object> map = new ArrayMap<>();
        map.put("username", username);
        map.put("password", password);
        return mRegentApi.loginValidation(map);
    }

    public Observable<SimpleEntity<String>> getGoodsBarcodes(String lastModDate) {
        return mRegentApi.getGoodsBarcodes(lastModDate);
    }
}
