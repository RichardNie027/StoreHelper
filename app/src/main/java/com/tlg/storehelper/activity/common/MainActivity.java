package com.tlg.storehelper.activity.common;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.activity.calculator.CalculatorActivity;
import com.tlg.storehelper.activity.collocation.CollocationActivity;
import com.tlg.storehelper.activity.inventory.InventoryListsActivity;
import com.tlg.storehelper.R;
import com.tlg.storehelper.activity.membership.MembershipActivity;
import com.tlg.storehelper.activity.stock.StockActivity;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

import java.util.ArrayList;

public class MainActivity extends BaseRxAppCompatActivity implements BestSellingFragment.OnFragmentInteractionListener {

    private String[] mStoreCodes = null;
    private Spinner mSpinner = null;
    private EditText mEtBarcode = null;
    private RadioGroup mRgDimension = null;
    private View mVSection1 = null;
    private View mVSection2 = null;
    private ImageView mIvLeft = null;
    private ImageView mIvRight = null;

    /**畅销款页面*/
    private BestSellingFragment mBestSellingFragment;

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected int setToolbarResourceID() {
        return 0;
    }

    @Override
    protected void initView() {
        // find view
        mSpinner = findViewById(R.id.spinner);
        mEtBarcode = findViewById(R.id.etBarcode);
        mRgDimension = findViewById(R.id.rgDimension);
        ViewPager viewPager = (ViewPager)findViewById(R.id.vpToobarPager);
        mVSection1 = findViewById(R.id.vSection1);
        mVSection2 = findViewById(R.id.vSection2);
        mIvLeft = findViewById(R.id.ivLeft);
        mIvRight = findViewById(R.id.ivRight);

        // initialize controls
        hideKeyboard(true);
        viewPager.setOffscreenPageLimit(3);
        ArrayList<View> pagerList = new ArrayList<View>();
        pagerList.add(getLayoutInflater().inflate(R.layout.view_main_toolbar_page1, null, false));
        pagerList.add(getLayoutInflater().inflate(R.layout.view_main_toolbar_page2, null, false));
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(pagerList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(positionOffset == 0f) {
                    mIvLeft.setImageAlpha(100);
                    mIvRight.setImageAlpha(100);
                } else {
                    final float distance = 0.15f;
                    mIvLeft.setImageAlpha((int)((1-(1-positionOffset>distance ? distance : 1-positionOffset)/distance)*100));
                    mIvRight.setImageAlpha((int)((1-(positionOffset>distance ? distance : positionOffset)/distance)*100));
                }
            }

            @Override
            public void onPageSelected(int position) {
                mVSection1.setBackgroundResource(position==0 ? R.color.colorPrimaryLight : R.color.colorSecondDark);
                mVSection2.setBackgroundResource(position==1 ? R.color.colorPrimaryLight : R.color.colorSecondDark);
                mIvLeft.setVisibility(position==0 ? View.INVISIBLE : View.VISIBLE);
                mIvRight.setVisibility(position==0 && viewPager.getChildCount()>0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mRgDimension.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateBestSelling(true);
            }
        });

        ///获取用户可用门店集合
        Bundle extras = getIntent().getExtras();
        mStoreCodes = extras.getStringArray("storeCodes");

        //填充店铺列表
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_white_select, mStoreCodes);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        if(GlobalVars.storeCode.equals("") && mStoreCodes.length > 0)
            GlobalVars.storeCode = mStoreCodes[0];

        spinner.setOnItemSelectedListener(new StoreCodeOnItemSelectedListener());

        //更新货品资料
        RequestUtil.requestGoodBarcodes(_this, null, null, new RequestUtil.OnRequestEndListener() {
            @Override
            public void onRequestEnd() {
                // 初始化并动态添加 fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Bundle dataBundle = new Bundle();
                String dimension;
                switch (mRgDimension.getCheckedRadioButtonId()) {
                    case R.id.rbWeek:
                        dimension = "WEEK";
                        break;
                    case R.id.rbMonth:
                        dimension = "MONTH";
                        break;
                    default:
                        dimension = "NEW";
                }
                dataBundle.putString(BestSellingFragment.sDimensionLabel, dimension);
                BestSellingListDataRequest bestSellingListDataRequest = new BestSellingListDataRequest();
                mBestSellingFragment = BestSellingFragment.newInstance(BestSellingFragment.class, BestSellingRecyclerViewItemAdapter.class, bestSellingListDataRequest, dataBundle, DisplayMode.STAGGERED, 3);
                fragmentTransaction.add(R.id.fragment_container, mBestSellingFragment);
                fragmentTransaction.commit();
            }
        });

        ///设置“条形码”控件
        //回车键响应
        mEtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    String barcode = mEtBarcode.getText().toString().toUpperCase();
                    String goodsNo = "";
                    if(barcode.length() > 0) {
                        if(DbUtil.checkGoodsNo(barcode))
                            goodsNo = barcode;
                        else
                            goodsNo = DbUtil.checkGoodsBarcode(barcode, true);
                        if (!goodsNo.isEmpty()) {
                            mEtBarcode.setText("");
                        } else {                    //错误
                            AndroidUtil.showToast("货号 / 条码不存在");
                            mEtBarcode.selectAll();
                            mEtBarcode.requestFocus();
                            return false;
                        }
                    } else
                        return false;
                    Intent intent = new Intent(_this, CollocationActivity.class);
                    intent.putExtra("goodsNo", goodsNo);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        //获得焦点全选
        mEtBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtBarcode.selectAll();
                }
            }
        });
        //Touch清空条形码
        mEtBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mEtBarcode.setText("");
                return false;
            }
        });

    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_main;
    }

    private void updateBestSelling(boolean reload) {
        String dimension;
        if(mRgDimension != null && mBestSellingFragment != null) {
            switch (mRgDimension.getCheckedRadioButtonId()) {
                case R.id.rbWeek:
                    dimension = "WEEK";
                    break;
                case R.id.rbMonth:
                    dimension = "MONTH";
                    break;
                default:
                    dimension = "NEW";
            }
            Bundle dataBundle = new Bundle();
            dataBundle.putString(BestSellingFragment.sDimensionLabel, dimension);
            mBestSellingFragment.updateBundle(dataBundle, reload);
        }
    }

    @Override
    public void finish() {
        new AlertDialog.Builder(MyApp.getInstance())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("温馨提示")
                .setMessage("是否退出本程序？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AndroidUtil.showToast("欢迎继续使用");
                    }
                })
                .show();
    }

    @Override
    public void onClickGoodsItem(String goodsNo) {
        if(goodsNo.length() == 0 || !DbUtil.checkGoodsNo(goodsNo)) {
            AndroidUtil.showToast("货号 / 条码不存在");
            return;
        }
        Intent intent = new Intent(_this, CollocationActivity.class);
        intent.putExtra("goodsNo", goodsNo);
        startActivity(intent);
    }

    @Override
    public void onRecyclerViewHasFocus() {
        mEtBarcode.requestFocus();
    }

    private class StoreCodeOnItemSelectedListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            String item  = adapterView.getItemAtPosition(position).toString();
            GlobalVars.storeCode = mStoreCodes[position];
            updateBestSelling(false);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void ivStockClick(View v) {
        Intent intent = new Intent(this, StockActivity.class);
        startActivity(intent);
    }

    public void ivMembershipClick(View v) {
        Intent intent = new Intent(this, MembershipActivity.class);
        startActivity(intent);
    }

    public void ivInventoryClick(View v) {
        Intent intent = new Intent(this, InventoryListsActivity.class);
        startActivity(intent);
    }

    public void ivCollocationClick(View v) {
        Intent intent = new Intent(this, CollocationActivity.class);
        startActivity(intent);
    }

    public void ivCalculatorClick(View v) {
        Intent intent = new Intent(this, CalculatorActivity.class);
        startActivity(intent);
    }

    public void btnSettingsClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void unsupportedClick(View v) {
        AndroidUtil.showToast("此功能暂未开放");
    }

    public void btnExitClick(View v) {
        finish();
    }

    /** 适配View的Pager适配器 */
    class ViewPagerAdapter extends PagerAdapter {
        private ArrayList<View> viewList;

        public ViewPagerAdapter() {
        }

        public ViewPagerAdapter(ArrayList<View> viewList) {
            super();
            this.viewList = viewList;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }
    }
}
