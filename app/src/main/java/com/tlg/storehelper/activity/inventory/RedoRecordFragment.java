package com.tlg.storehelper.activity.inventory;

import android.content.ContentValues;
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

import com.nec.lib.android.utils.SQLiteUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.dao.InventoryDetail;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.tlg.storehelper.vo.InventoryRedoVo;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RedoRecordFragment extends LoadMoreFragment implements RedoRecordListDataRequest.RequireRedoDataListener {

    private OnFragmentInteractionListener mListener;
    private long mInventoryListId;
    private String mInventoryBinCoding;

    private TextView tvTip4, tvTip3, tvTip2, tvTip1;
    private EditText mEtBarcode;

    /**盘点单原始按条码汇总记录*/
    private List<InventoryRedoVo> mInventoryRedoList = new ArrayList<>();
    /**反应实际扫码的按条码汇总记录*/
    private List<InventoryRedoVo> mInventoryRedoRealList = new ArrayList<>();
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
        mIdOfRecyclerView = "recycle_list";          //内部资源名称
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
        mSwipeRefreshLayout.setEnabled(false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        doRefreshOnRecyclerView();
        displayStatistic();
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideKeyboard(mEtBarcode);
            }
        }, 1000);
    }

    private void initView(View rootView) {
        // find view
        tvTip4 = rootView.findViewById(R.id.tvTip4);
        tvTip3 = rootView.findViewById(R.id.tvTip3);
        tvTip2 = rootView.findViewById(R.id.tvTip2);
        tvTip1 = rootView.findViewById(R.id.tvTip1);
        mEtBarcode = rootView.findViewById(R.id.etBarcode);

        // load data
        loadData();

        // initialize controls
        displayStatistic();

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

    private void loadData() {
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;

            //记录明细汇总
            mInventoryRedoList.clear();
            sql = new StringBuffer().append("select barcode, sum(quantity) as quantity").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=? and bin_coding=?")
                    .append(" group by barcode")
                    .append(" order by barcode asc")
                    .toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), mInventoryBinCoding});
            while (cursor.moveToNext()) {
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                InventoryRedoVo inventoryDetailVo = new InventoryRedoVo(barcode, quantity, 0, 0);
                mInventoryRedoList.add(inventoryDetailVo);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            Toast.makeText(MyApp.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    private void displayStatistic() {
        int sum1 = 0;
        int sum2 = 0;
        int sum3 = 0;
        int sum4 = 0;
        for(InventoryRedoVo vo: mInventoryRedoRealList) {
            if(vo.quantity3 == 0)
                sum4++;
            else if(vo.quantity1 == 0)
                sum3++;
            else if(vo.quantity3 > 0)
                sum1++;
            else
                sum2++;
        }
        tvTip4.setText("未完 " + sum1);
        tvTip3.setText("超出 " + sum2);
        tvTip2.setText("新增 " + sum3);
        tvTip1.setText("一致 " + sum4);
    }

    public void onScanBarcodeAsNewRecord(String barcode) {
        barcode = barcode.toUpperCase();
        if(barcode.length() > 0 && DbUtil.checkGoodsBarcode(barcode)) {
            mEtBarcode.setText("");
        } else {                    //错误
            Toast.makeText(MyApp.getInstance(), "条码不存在", Toast.LENGTH_SHORT).show();
            mEtBarcode.selectAll();
            return;
        }
        if(barcodeCheck(barcode)) {
            int value = mRedoDataMap.containsKey(barcode) ? mRedoDataMap.get(barcode) + 1 : 1;
            mRedoDataMap.put(barcode, value);
        } else {
            int value = mRedoNewDataMap.containsKey(barcode) ? mRedoNewDataMap.get(barcode) + 1 : 1;
            mRedoNewDataMap.put(barcode, value);
        }
        doRefreshOnRecyclerView();
        displayStatistic();
    }

    private boolean barcodeCheck(String barcode) {
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
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
            Toast.makeText(MyApp.getInstance(), "数据检索失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
        return result;
    }

    public boolean doUpdteInventory() {
        long result = 0; //影响的记录数
        SQLiteOpenHelper helper = new SQLiteDbHelper(this.getActivity().getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            db.delete(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "pid=? and bin_coding=?", new String[]{Long.toString(mInventoryListId), mInventoryBinCoding});
            for(String key: mRedoDataMap.keySet()) {
                InventoryDetail inventoryDetail = new InventoryDetail(-1L, mInventoryListId, mInventoryBinCoding, key, mRedoDataMap.get(key));
                ContentValues contentValues = SQLiteUtil.toContentValues(inventoryDetail, "id");
                result = db.insert(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "id", contentValues);
                if (result == -1L)
                    throw new Exception("新增记录出错");
            }
            for(String key: mRedoNewDataMap.keySet()) {
                InventoryDetail inventoryDetail = new InventoryDetail(-1L, mInventoryListId, mInventoryBinCoding, key, mRedoNewDataMap.get(key));
                ContentValues contentValues = SQLiteUtil.toContentValues(inventoryDetail, "id");
                result = db.insert(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "id", contentValues);
                if (result == -1L)
                    throw new Exception("新增记录出错");
            }
            mRedoDataMap.clear();
            mRedoNewDataMap.clear();

            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            result = -1;
            //Toast.makeText(MyApp.getInstance(), "删盘点单出错", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        if(result != -1)
            loadData();
        return result != -1;
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
    public List<InventoryRedoVo> onNeedData() {
        mInventoryRedoRealList.clear();
        mInventoryRedoRealList.addAll(mInventoryRedoList);
        for(InventoryRedoVo vo: mInventoryRedoRealList) {
            if (mRedoDataMap.containsKey(vo.barcode)) {
                vo.quantity2 = mRedoDataMap.get(vo.barcode);
            }
            vo.quantity3 = vo.quantity1 - vo.quantity2;
        }
        for(String key: mRedoNewDataMap.keySet()) {
            mInventoryRedoRealList.add(0, new InventoryRedoVo(key, 0, mRedoNewDataMap.get(key), -mRedoNewDataMap.get(key)));
        }
        return mInventoryRedoRealList;
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
    }
}
