package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class SimplePageListEntity<T> extends BaseResponseEntity {

    public int page = 0;
    public int pageCount = 0;
    public int pageSize = 24;
    public int recordCount = 0;

    public List<T> list = new ArrayList<>();

    public SimplePageListEntity() {}

    public SimplePageListEntity(int page, int pageCount, int pageSize, int recordCount) {
        this.page = page;
        this.pageCount = pageCount;
        this.pageSize = pageSize;
        this.recordCount = recordCount;
    }

}
