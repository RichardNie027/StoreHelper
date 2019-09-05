package com.tlg.storehelper.activity.common;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.activity.collocation.CollocationActivity;
import com.tlg.storehelper.activity.inventory.InventoryListsActivity;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

public class MainActivity extends BaseRxAppCompatActivity implements BestSellingFragment.OnFragmentInteractionListener {

    private String[] mStoreCodes = null;
    private Spinner mSpinner = null;
    private EditText mEtBarcode = null;
    private RadioGroup mRgDimension = null;

    /**畅销款页面*/
    private BestSellingFragment mBestSellingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        // find view
        mSpinner = findViewById(R.id.spinner);
        mEtBarcode = findViewById(R.id.etBarcode);
        mRgDimension = findViewById(R.id.rgDimension);

        // initialize controls
        hideKeyboard(true);

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
                mBestSellingFragment = BestSellingFragment.newInstance(BestSellingFragment.class, BestSellingRecyclerViewItemAdapter.class, bestSellingListDataRequest, dataBundle, LoadMoreFragment.DisplayMode.STAGGERED, 3);
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

    public void ivInventoryClick(View v) {
        Intent intent = new Intent(this, InventoryListsActivity.class);
        startActivity(intent);
    }

    public void ivCollocationClick(View v) {
        Intent intent = new Intent(this, CollocationActivity.class);
        startActivity(intent);
    }

    public void btnSettingsClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void ivSearchClick(View v) {
        AndroidUtil.showToast("此功能暂未开放");
    }

    public void btnExitClick(View v) {
        finish();
    }
}
