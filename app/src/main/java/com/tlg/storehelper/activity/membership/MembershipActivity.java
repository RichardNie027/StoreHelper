package com.tlg.storehelper.activity.membership;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.LoadMoreActivity;
import com.tlg.storehelper.R;
import com.tlg.storehelper.httprequest.net.entity.ShopHistoryEntity;

public class MembershipActivity extends LoadMoreActivity {

    private Toolbar mToolbar;
    private EditText mEtMembershipId;

    private ShopHistoryEntity mShopHistoryEntity;

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
        mIdOfSwipeRefreshLayout = "refresh_layout";     //内部资源名称
        mIdOfRecyclerView = "recycler_list";            //内部资源名称
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_membership;
    }

    @Override
    protected void initViewBegin(View rootView) {
        setArguments(MembershipRecyclerViewItemAdapter.class, new MembershipListDataRequest(), DisplayMode.LINEAR);
        mDataBundle.putString("membership_id", "121212");
        mDataBundle.putString("store_code", "222");
    }

    @Override
    protected void initViewEnd(View rootView) {
        // find view
//        mEtMembershipId = rootView.findViewById(R.id.etMembershipId);

        hideKeyboard(true);
        if(mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setEnabled(false);

//        ///设置“条形码”控件
//        //回车键响应
//        mEtMembershipId.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int keyCode, KeyEvent event) {
//                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
//                    onEnterPress(mEtMembershipId.getText().toString());
//                    mEtMembershipId.requestFocus();
//                    return true;
//                }
//                return false;
//            }
//        });
//        //获得焦点全选货位
//        mEtMembershipId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if(hasFocus) {
//                    mEtMembershipId.selectAll();
//                }
//            }
//        });
//        //Touch清空条形码
//        mEtMembershipId.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                mEtMembershipId.setText("");
//                return false;
//            }
//        });
//
//        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if(hasFocus) {
//                    mEtMembershipId.requestFocus();
//                }
//            }
//        });

    }

    private void onEnterPress(String memberId) {
        doRefreshOnRecyclerView();
    }
}
