package com.tlg.storehelper.activity.performance;

import android.os.Bundle;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.tlg.storehelper.R;

public class PerformanceActivity extends BaseRxAppCompatActivity {

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_performance;
    }
}
