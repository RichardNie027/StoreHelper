package com.tlg.storehelper.httprequest.net.api;

import android.util.ArrayMap;

import com.tlg.storehelper.httprequest.net.entity.GoodsBarcodeEntity;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface RegentApi {

    @GET("good_barcode_list")
    Observable<GoodsBarcodeEntity> getGoodsBarcodes(@QueryMap ArrayMap<String, Object> map);

}
