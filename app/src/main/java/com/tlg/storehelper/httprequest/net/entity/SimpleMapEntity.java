package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class SimpleMapEntity extends BaseResponseEntity {

    public Map<String, Object> result = new HashMap<>();

}
