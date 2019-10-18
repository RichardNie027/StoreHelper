package com.tlg.storehelper.activity.membership;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.LoadMoreActivity;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.httprequest.net.entity.SimpleListResponseVo;
import com.tlg.storehelper.vo.MembershipVo;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MembershipActivity extends LoadMoreActivity {

    private EditText mEtMembershipId;
    private ViewPager mViewpagerHeader;
    private TextView mTvHeaderIdx;

    private List<MembershipVo> mMembershipList;

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
        mViewpagerHeader = rootView.findViewById(R.id.viewpagerHeader);
        mTvHeaderIdx = rootView.findViewById(R.id.tvHeaderIdx);

        hideKeyboard(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        String lastMembershipId = pref.getString("lastMembershipId", "");
        mEtMembershipId.setText(lastMembershipId);

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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("lastMembershipId", membershipId);
        editor.commit();

        RequestUtil.requestMembership(membershipId, GlobalVars.storeCode, this, new RequestUtil.OnSuccessListener<SimpleListResponseVo<MembershipVo>>() {
            @Override
            public void onSuccess(SimpleListResponseVo<MembershipVo> response) {
                //Header ViewPager
                mMembershipList = response.list;
                if(mMembershipList==null || mMembershipList.size() <= 1)
                    mTvHeaderIdx.setText("");
                else
                    mTvHeaderIdx.setText("1/" + mMembershipList.size());
                if(mMembershipList.size() > 0) {
                    mDataBundle.putString("membership_id", mMembershipList.get(0).membershipCardId);
                    doRefreshOnRecyclerView();
                }
                MyAdapter adapter = new MyAdapter(mEtMembershipId.getRootView().getContext(), mMembershipList);
                mViewpagerHeader.setOffscreenPageLimit(3); // cache 3 pages
                mViewpagerHeader.setAdapter(adapter);
                mViewpagerHeader.setPageMargin(10);
                mViewpagerHeader.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        if(mMembershipList==null || mMembershipList.size() <= 1)
                            mTvHeaderIdx.setText("");
                        else
                            mTvHeaderIdx.setText(String.valueOf(position+1) + "/" + mMembershipList.size());
                        mDataBundle.putString("membership_id", mMembershipList.get(position).membershipCardId);
                        doRefreshOnRecyclerView();
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                   }
                });
            }
        });
    }

    //CardView Adapter
    public class MyAdapter extends PagerAdapter {
        private List<MembershipVo> mList = new ArrayList<>();
        private Context context;
        private LayoutInflater inflater;

        public MyAdapter(Context context, List<MembershipVo> list) {
            this.context = context;
            this.mList.addAll(list);
            inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.activity_membership_header_viewpager_item, container, false);

            TextView tvMembershipCardId = view.findViewById(R.id.tvMembershipCardId);
            TextView tvName = view.findViewById(R.id.tvName);
            TextView tvMobile = view.findViewById(R.id.tvMobile);
            TextView tvPerExpenditure = view.findViewById(R.id.tvPerExpenditure);
            TextView tvYearExpenditure = view.findViewById(R.id.tvYearExpenditure);
            TextView tvTotalExpenditure = view.findViewById(R.id.tvTotalExpenditure);

            tvMembershipCardId.setText("卡号：" + mList.get(position).membershipCardId);
            tvName.setText("姓名：" + mList.get(position).membershipName);
            tvMobile.setText("手机：" + mList.get(position).mobile);
            tvPerExpenditure.setText("单笔" + new DecimalFormat("￥,###").format(mList.get(position).perExpenditure));
            tvYearExpenditure.setText("年" + new DecimalFormat("￥,###").format(mList.get(position).yearExpenditure));
            tvTotalExpenditure.setText("总" + new DecimalFormat("￥,###").format(mList.get(position).totalExpenditure));

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
