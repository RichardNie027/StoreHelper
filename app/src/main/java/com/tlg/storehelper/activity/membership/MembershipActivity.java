package com.tlg.storehelper.activity.membership;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.tlg.storehelper.R;
import com.tlg.storehelper.httprequest.net.entity.ShopHistoryEntity;

import java.util.ArrayList;
import java.util.List;

public class MembershipActivity extends BaseRxAppCompatActivity {

    private Toolbar mToolbar;
    private EditText mEtBarcode = null;
    private TextView mTvGoodsNo = null;
    private TextView mTvGoodsName = null;
    private RecyclerView mRecyclerView;
    private List<ShopHistoryEntity> mDatas = new ArrayList<>();

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_membership;
    }
}
