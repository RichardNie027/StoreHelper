package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class SimpleListPageEntity<T> extends BaseResponseEntity {

    public int page = 0;
    public int pageCount = 0;
    public int recordPerPage = 24;
    public int recordCount = 0;

    public List<T> result = new ArrayList<>();

    public SimpleListPageEntity() {}

    public SimpleListPageEntity(int page, int pageCount, int recordPerPage, int recordCount) {
        this.page = page;
        this.pageCount = pageCount;
        this.recordPerPage = recordPerPage;
        this.recordCount = recordCount;
    }

}
