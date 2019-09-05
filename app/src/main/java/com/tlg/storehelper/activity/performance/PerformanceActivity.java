package com.tlg.storehelper.activity.performance;

import android.os.Bundle;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.tlg.storehelper.R;

public class PerformanceActivity extends BaseRxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);
        initView();
    }

    private void initView() {

    }
}
