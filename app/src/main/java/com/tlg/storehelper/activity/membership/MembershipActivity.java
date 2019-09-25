package com.tlg.storehelper.activity.membership;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.LoadMoreActivity;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.httprequest.net.entity.ShopHistoryEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

import java.text.DecimalFormat;
import java.util.List;

public class MembershipActivity extends LoadMoreActivity {

    private EditText mEtMembershipId;

    private TextView mtvMembershipCardId;
    private TextView mtvName;
    private TextView mtvMobile;
    private TextView mtvYearExpenditure;
    private TextView mtvTotalExpenditure;

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected int setToolbarResourceID() {
        return R.id.toolbar;
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_membership;
    }

    @Override
    protected void initViewBegin(View rootView) {
        setArguments(MembershipRecyclerViewItemAdapter.class, new MembershipListDataRequest(), DisplayMode.LINEAR);
        mDataBundle.putString("membership_id", "");
        mDataBundle.putString("store_code", GlobalVars.storeCode);
        mAutoload = false;
    }

    @Override
    protected void initViewEnd(View rootView) {
        // find view
        mEtMembershipId = rootView.findViewById(R.id.etMembershipId);
        mtvMembershipCardId = rootView.findViewById(R.id.tvMembershipCardId);
        mtvName = rootView.findViewById(R.id.tvName);
        mtvMobile = rootView.findViewById(R.id.tvMobile);
        mtvYearExpenditure = rootView.findViewById(R.id.tvYearExpenditure);
        mtvTotalExpenditure = rootView.findViewById(R.id.tvTotalExpenditure);

        hideKeyboard(true);

        //setup view
        if(mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setEnabled(false);

        ///设置“会员ID”控件
        //回车键响应
        mEtMembershipId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    onEnterPress(mEtMembershipId.getText().toString());
                    mEtMembershipId.requestFocus();
                    return true;
                }
                return false;
            }
        });
        //获得焦点全选
        mEtMembershipId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtMembershipId.selectAll();
                }
            }
        });

        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtMembershipId.requestFocus();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onEnterPress(String membershipId) {
        RequestUtil.requestMembershipShopHistory(membershipId, GlobalVars.storeCode, this, new RequestUtil.OnSuccessListener<ShopHistoryEntity>() {
            @Override
            public void onSuccess(ShopHistoryEntity response) {
                mtvMembershipCardId.setText("卡号："+response.membershipCardId);
                mtvName.setText("姓名："+response.name);
                mtvMobile.setText("手机号："+response.mobile);
                mtvYearExpenditure.setText("本年消费："+new DecimalFormat("￥,###").format(response.yearExpenditure));
                mtvTotalExpenditure.setText("总消费额："+new DecimalFormat("￥,###").format(response.totalExpenditure));
                mDataBundle.putString("membership_id", response.membershipCardId);
                doRefreshOnRecyclerView();
            }
        });
    }

}
