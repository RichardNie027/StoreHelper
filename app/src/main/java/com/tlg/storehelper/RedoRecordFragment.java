package com.tlg.storehelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.application.MyApplication;

import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.tlg.storehelper.loadmorerecycler.LoadMoreFragment;
import com.tlg.storehelper.vo.InventoryRedoDetailVo;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RedoRecordFragment extends LoadMoreFragment implements RedoRecordListDataRequest.RequireRedoDataListener {

    private OnFragmentInteractionListener mListener;
    private long mInventoryListId;
    private String mInventoryBinCoding;

    private TextView mTvBinCoding;
    private EditText mEtBarcode;

    /**复盘明细（已有条码）*/
    private List<InventoryRedoDetailVo> redoDetailList = new ArrayList<>();
    /**复盘明细（新条码）*/
    private List<InventoryRedoDetailVo> redoNewDetailList = new ArrayList<>();
    /**复盘按条码汇总（已有条码）*/
    private Map<String, Integer> mRedoDataMap = new LinkedHashMap<>();
    /**复盘按条码汇总（新条码）*/
    private Map<String, Integer> mRedoNewDataMap = new LinkedHashMap<>();

    //传参名称
    public static final String sInventoryListIdLabel = "inventory_list_id";
    public static final String sInventoryBinCodingLabel = "inventory_bin_coding";

    public RedoRecordFragment() {
        //资源名称
        mLayoutOfFragmentItemList = "fragment_redo_record";
        mIdOfSwipeRefreshLayout = "refresh_layout"; //内部资源名称
        mIdOfRecycleView = "recycle_list";          //内部资源名称
    }

    @Override
    protected void doParamBundle(Bundle bundle) {
        //1、组装mDataBundle
        mDataBundle.putLong(sInventoryListIdLabel, bundle.getLong(sInventoryListIdLabel, -1));
        mDataBundle.putString(sInventoryBinCodingLabel, bundle.getString(sInventoryBinCodingLabel, ""));
        //2、为页面传参
        mInventoryListId = bundle.getLong(sInventoryListIdLabel, -1);
        mInventoryBinCoding = bundle.getString(sInventoryBinCodingLabel, "");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideKeyboard(mEtBarcode);
            }
        }, 1000);
    }

    private void initView(View rootView) {
        // find view
        mTvBinCoding = rootView.findViewById(R.id.tvBinCoding);
        mEtBarcode = rootView.findViewById(R.id.etBarcode);

        // initial controls
        mTvBinCoding.setText(mInventoryBinCoding);

        ///设置“条形码”控件
        //回车键响应
        mEtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    onScanBarcodeAsNewRecord(mEtBarcode.getText().toString());
                    mEtBarcode.requestFocus();
                    _this.hideKeyboard(view);
                    return true;
                }
                return false;
            }
        });
        //获得焦点全选货位
        mEtBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtBarcode.selectAll();
                }
                _this.hideKeyboard(view);
            }
        });
        //Touch清空条形码
        mEtBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mEtBarcode.setText("");
                _this.hideKeyboard(view);
                return false;
            }
        });

        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtBarcode.requestFocus();
                }
            }
        });
    }

    public void onScanBarcodeAsNewRecord(String barcode) {
        if(barcode.length() > 1) {  //模拟正确
            mEtBarcode.setText("");
        } else {                    //模拟错误
            mEtBarcode.selectAll();
            return;
        }
        if(barcodeCheck(barcode)) {
            redoDetailList.add(new InventoryRedoDetailVo(redoDetailList.size(), barcode));
            int value = mRedoDataMap.containsKey(barcode) ? mRedoDataMap.get(barcode) + 1 : 1;
            mRedoDataMap.put(barcode, value);
        } else {
            redoNewDetailList.add(new InventoryRedoDetailVo(redoNewDetailList.size(), barcode));
            int value = mRedoNewDataMap.containsKey(barcode) ? mRedoNewDataMap.get(barcode) + 1 : 1;
            mRedoNewDataMap.put(barcode, value);
        }
        doRefreshOnRecyclerView();
    }

    private boolean barcodeCheck(String barcode) {
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApplication.getInstance());
        SQLiteDatabase db = null;
        boolean result = false;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;
            sql = new StringBuffer().append("select count(*) as num").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=? and bin_coding=? and barcode=?").toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), mInventoryBinCoding, barcode});
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0) > 0;
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            Toast.makeText(MyApplication.getInstance(), "数据检索失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Map<String, Integer> onNeedRedoData(List<String> barcodeList) {
        Map<String, Integer> redoDataMap = new LinkedHashMap<>(barcodeList.size());
        for(int i=0; i<barcodeList.size(); i++) {
            redoDataMap.put(barcodeList.get(i), 0);
        }
        for(String key: mRedoDataMap.keySet()) {
            if (redoDataMap.containsKey(key)) {
                redoDataMap.put(key, redoDataMap.get(key) + mRedoDataMap.get(key));
            }
        }
        for(String key: mRedoNewDataMap.keySet()) {
            if (redoDataMap.containsKey(key)) {
                redoDataMap.put(key, redoDataMap.get(key) + mRedoNewDataMap.get(key));
            }
        }
        return redoDataMap;
    }

    @Override
    public Map<String, Integer> beforeDataRequest() {
        return mRedoNewDataMap;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * 删除扫码记录触发
         * @param id
         */
        void onInventoryDeleteRecord(long id);
    }
}
