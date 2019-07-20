package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.httprequest.net.revert.BaseResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class SimpleListEntity<T> extends BaseResponseEntity {

    public List<T> result = new ArrayList<>();

}
