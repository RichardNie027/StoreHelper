package com.tlg.storehelper;

import android.util.ArrayMap;

import com.nec.lib.application.MyApplication;
import com.nec.lib.httprequest.utils.ApiConfig;

public class MyApp extends MyApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        String baseUrl = "http://192.168.1.176:8080/JsonTest/api/";
        ArrayMap<String, String> headMap = new ArrayMap<String, String>();
        headMap.put("key1", "value1");
        headMap.put("key2", "value2");
        headMap.put("key3", "value3");

        /*
        SslSocketConfigure sslSocketConfigure = new SslSocketConfigure.Builder()
                .setVerifyType(2)//单向双向验证 1单向  2 双向
                .setClientPriKey("client.bks")//客户端keystore名称
                .setTrustPubKey("truststore.bks")//受信任密钥库keystore名称
                .setClientBKSPassword("123456")//客户端密码
                .setTruststoreBKSPassword("123456")//受信任密钥库密码
                .setKeystoreType("BKS")//客户端密钥类型
                .setProtocolType("TLS")//协议类型
                .setCertificateType("X.509")//证书类型
                .build();
        */

        ApiConfig build = new ApiConfig.Builder()
                .setBaseUrl(baseUrl)//BaseUrl，这个地方加入后项目中默认使用该url
                .setInvalidateToken(-100)//Token失效码
                .setSucceedCode(200)//成功返回码
                .setFilter("com.tlg.storehelper.activity.broadcastFilter")//失效广播Filter设置
                .setDefaultTimeout(2000)//响应时间，可以不设置，默认为2000毫秒
                .setHeads(headMap)//动态添加的header，也可以在其他地方通过ApiConfig.setHeads()设置
                .setOpenHttps(false)//开启HTTPS验证
                //.setSslSocketConfigure(sslSocketConfigure)//HTTPS认证配置
                .build();
        build.init(this);
    }
}
