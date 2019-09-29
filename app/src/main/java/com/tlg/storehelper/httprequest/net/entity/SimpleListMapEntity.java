package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleListMapEntity<T> extends BaseResponseEntity {

    public List<T> list = new ArrayList<>();

    public Map<String, Object> map = new HashMap<>();

}
