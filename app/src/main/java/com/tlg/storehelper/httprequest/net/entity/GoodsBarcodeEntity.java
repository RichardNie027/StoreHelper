package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.httprequest.net.revert.BaseResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class GoodsBarcodeEntity extends BaseResponseEntity {

    public List<ResultBean> result = new ArrayList<>();

    public static class ResultBean {
        public String id;
        public String goods_id;
        public String size_id;
        public String size_desc;
        public String barcode;
    }
}
