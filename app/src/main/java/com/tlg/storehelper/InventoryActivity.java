package com.tlg.storehelper;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nec.application.MyApplication;
import com.nec.utils.ExcelUtil;
import com.nec.utils.StringUtil;
import com.tlg.storehelper.base.BaseAppCompatActivity;
import com.nec.utils.DateUtil;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.Inventory;
import com.tlg.storehelper.dao.InventoryDetail;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.utils.SQLiteUtil;
import com.tlg.storehelper.vo.StatisticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class InventoryActivity extends BaseAppCompatActivity
        implements ScannerFragment.OnFragmentInteractionListener, RecordFragment.OnFragmentInteractionListener, TotalRecordFragment.OnFragmentInteractionListener {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    /**调用者传递过来的“盘点ID”，-1L为错误*/
    private long mListId;
    private Inventory mInventory = new Inventory();
    private List<InventoryDetail> mInventoryDetailList = new ArrayList<InventoryDetail>();

    /**盘点单统计信息*/
    private StatisticInfo mStatisticInfo = new StatisticInfo();
    /**最新的货位号*/
    private String mLastBinCoding;

    /**数据发生变化，扫码页面统计信息需要刷新*/
    private boolean mScannerNeedRefresh = false;
    /**扫码数据发生变化，汇总记录需要刷新*/
    private boolean mRecordListNeedRefresh = false;
    /**扫码数据发生变化，明细记录需要刷新*/
    private boolean mRecordTotalNeedRefresh = false;

    /**扫码页面*/
    private ScannerFragment mScannerFragment;
    /**扫码明细记录页面*/
    private RecordFragment mRecordFragment;
    /**货位汇总记录页面*/
    private TotalRecordFragment mTotalRecordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        //接收参数
        Intent intent =getIntent();
        mListId = intent.getLongExtra("list_id", -1L);
        initView();
    }

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.vpFragment);
        mTabRadioGroup = findViewById(R.id.rgTabs);
        //toolbar
        setSupportActionBar(mToolbar);
        //处理异常
        if(mListId == -1L) {
            new AlertDialog.Builder(MyApplication.getInstance())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("内部错误")
                    .setMessage("盘点单关键信息丢失")
                    .setCancelable(true)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            setResult(4, intent);
                            finish();
                        }
                    })
                    .show();
            return;
        } else {
            //装置数据
            loadData(true, true);
        }
        // init fragment
        mFragments = new ArrayList<Fragment>(3);
        updateStatisticInfo(null);
        mScannerFragment = ScannerFragment.newInstance(mStatisticInfo);
        mFragments.add(mScannerFragment);

        Bundle dataBundle2 = new Bundle();
        dataBundle2.putLong(RecordFragment.sInventoryListIdLabel, mListId);
        dataBundle2.putString(RecordFragment.sInventoryListNoLabel, mInventory.list_no);
        mRecordFragment = RecordFragment.newInstance(RecordFragment.class, RecordRecyclerViewItemAdapter.class, new RecordListDataRequest(), dataBundle2);
        mFragments.add(mRecordFragment);

        Bundle dataBundle3 = new Bundle();
        dataBundle3.putLong(TotalRecordFragment.sInventoryListIdLabel, mListId);
        dataBundle3.putString(TotalRecordFragment.sInventoryListNoLabel, mInventory.list_no);
        mTotalRecordFragment = TotalRecordFragment.newInstance(TotalRecordFragment.class, TotalRecordRecyclerViewItemAdapter.class, new TotalRecordListDataRequest(), dataBundle3);
        mFragments.add(mTotalRecordFragment);
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3); // cache 3 pages
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private void loadData(boolean loadMaster, boolean loadDetail) {
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;
            if(loadMaster) {
                sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY)
                        .append(" where id=?").toString();
                cursor = db.rawQuery(sql, new String[]{Long.toString(mListId)});
                if (cursor.moveToFirst()) {
                    mInventory.id = cursor.getLong(cursor.getColumnIndex("id"));
                    mInventory.store_code = cursor.getString(cursor.getColumnIndex("store_code"));
                    mInventory.list_date = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("list_date")));
                    mInventory.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                    mInventory.username = cursor.getString(cursor.getColumnIndex("username"));
                    mInventory.list_no = cursor.getString(cursor.getColumnIndex("list_no"));
                    mInventory.create_time = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("create_time")));
                    mInventory.last_time = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("last_time")));
                } else {
                    mInventory.id = -1L;
                }
                cursor.close();
            }
            if(loadDetail) {    //读从表
                mInventoryDetailList.clear();
                sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                        .append(" where pid=?")
                        .append(" order by id desc")
                        .toString();
                cursor = db.rawQuery(sql, new String[]{Long.toString(mListId)});
                while (cursor.moveToNext()) {
                    InventoryDetail inventoryDetail = new InventoryDetail();
                    inventoryDetail.id = cursor.getLong(cursor.getColumnIndex("id"));
                    inventoryDetail.pid = cursor.getLong(cursor.getColumnIndex("pid"));
                    inventoryDetail.bin_coding = cursor.getString(cursor.getColumnIndex("bin_coding"));
                    inventoryDetail.barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                    inventoryDetail.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                    mInventoryDetailList.add(inventoryDetail);
                }
                cursor.close();
            }
        } catch (Throwable t) {
            Log.e("ERROR", t.getMessage(), t);
            Toast.makeText(MyApplication.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    /**
     * 重新统计盘点单信息
     * @param binCoding_lastSetNull 统计货位数量相关。未指定货位（NULL：统计最后货位；空白：统计无货位）
     */
    private void updateStatisticInfo(String binCoding_lastSetNull) {
        mStatisticInfo.id = mInventory.id;
        mStatisticInfo.listNo = mInventory.list_no;
        mStatisticInfo.quantity = 0;
        mStatisticInfo.totalQuantity = 0;
        mStatisticInfo.lastBarcode = "";
        mStatisticInfo.lastBinCoding = "";
        if(mInventoryDetailList.size() > 0) {
            mStatisticInfo.lastBarcode = mInventoryDetailList.get(0).barcode;
            mStatisticInfo.lastBinCoding = mInventoryDetailList.get(0).bin_coding;
            if(binCoding_lastSetNull == null)
                binCoding_lastSetNull = mStatisticInfo.lastBinCoding;
            int quantities = 0;
            int totalQuantities = 0;
            for(InventoryDetail detail: mInventoryDetailList) {
                if(detail.bin_coding.equals(binCoding_lastSetNull)) {
                    quantities = quantities + detail.quantity;
                }
                totalQuantities = totalQuantities + detail.quantity;
            }
            mStatisticInfo.quantity = quantities;
            mStatisticInfo.totalQuantity = totalQuantities;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 动态设置ToolBar菜单
        switch (mViewPager.getCurrentItem()) {
            case 0:
                menu.findItem(R.id.action_del_top_line).setVisible(false);
                menu.findItem(R.id.action_menu_more).setVisible(false);
                menu.findItem(R.id.action_locator_redo).setVisible(false);
                menu.findItem(R.id.action_del_inventory_list).setVisible(false);
                menu.findItem(R.id.action_upload_inventory_list).setVisible(false);
                menu.findItem(R.id.action_export_inventory_list).setVisible(false);
                break;
            case 1:
                menu.findItem(R.id.action_del_top_line).setVisible(true);
                menu.findItem(R.id.action_menu_more).setVisible(false);
                menu.findItem(R.id.action_locator_redo).setVisible(false);
                menu.findItem(R.id.action_del_inventory_list).setVisible(false);
                menu.findItem(R.id.action_upload_inventory_list).setVisible(false);
                menu.findItem(R.id.action_export_inventory_list).setVisible(false);
                break;
            case 2:
                menu.findItem(R.id.action_del_top_line).setVisible(false);
                menu.findItem(R.id.action_menu_more).setVisible(true);
                menu.findItem(R.id.action_locator_redo).setVisible(true);
                menu.findItem(R.id.action_del_inventory_list).setVisible(true);
                menu.findItem(R.id.action_upload_inventory_list).setVisible(true);
                menu.findItem(R.id.action_export_inventory_list).setVisible(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(4, intent);
                finish();
                return true;
            case R.id.action_del_top_line:
                new AlertDialog.Builder(MyApplication.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("删除提示")
                        .setMessage("是否删除最近的一条记录？")
                        .setCancelable(true)
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDetailRecordThenSubsequentActions(-1L);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("info", "没有删除记录");
                            }
                        })
                        .show();
                break;
            case R.id.action_locator_redo:
                Toast.makeText(MyApplication.getInstance(), "长按需要复盘的货位", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_del_inventory_list:
                new AlertDialog.Builder(MyApplication.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("删除提示")
                        .setMessage("是否删除盘点单（所有条目）？")
                        .setCancelable(true)
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delInventoryList();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("info", "没有删除盘点单");
                            }
                        })
                        .show();
                break;
            case R.id.action_upload_inventory_list:
                uploadInventoryList();
                break;
            case R.id.action_export_inventory_list:
                exportInventoryList();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(position);
            radioButton.setChecked(true);
            invalidateOptionsMenu();//重新加载菜单
            ///切换页面前刷新数据
            if(mScannerNeedRefresh || mRecordListNeedRefresh || mRecordTotalNeedRefresh) {
                switch (position) {
                    case 0:
                        if(mScannerNeedRefresh) {
                            mScannerFragment.updateStatisticDisplay(mStatisticInfo);
                            mScannerNeedRefresh = false;
                        }
                        break;
                    case 1:
                        if(mRecordListNeedRefresh) {
                            mRecordFragment.doRefreshOnRecyclerView();
                            mRecordListNeedRefresh = false;
                        }
                        break;
                    case 2:
                        if(mRecordTotalNeedRefresh) {
                            mTotalRecordFragment.doRefreshOnRecyclerView();
                            mRecordTotalNeedRefresh = false;
                        }
                        break;
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i).getId() == checkedId) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    };

    /**
     * 调用 insertDetailRecord 向本地数据库插入数据，
     * 后续动作：1、调整 mInventoryDetailList；2、更新统计；3、需要刷新的状态
     * @return 记录ID，-1出错
     */
    private long insertDetailRecordThenSubsequentActions(String barcode, String binCoding, int num) {
        long id = insertDetailRecord(barcode, binCoding, num);
        if(id != -1) {
            mInventoryDetailList.add(0, new InventoryDetail(id, mStatisticInfo.id, binCoding, barcode, num));
            updateStatisticInfo(binCoding);
            mScannerNeedRefresh = false;
            mRecordListNeedRefresh = true;
            mRecordTotalNeedRefresh = true;
        } else {
            Toast.makeText(MyApplication.getInstance(), "新增数据失败，请检查", Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    /**
     * 调用 deleteDetailRecord 向本地数据库删除数据（末条），
     * 后续动作：1、调整 mInventoryDetailList；2、更新统计；3、需要刷新的状态
     * @param id 待删除记录ID，-1L则删除最新一条记录
     * @return 成功
     */
    private boolean deleteDetailRecordThenSubsequentActions(long id) {
        boolean deleteSuccess = deleteDetailRecord(id);
        if(deleteSuccess) {
            if(id == -1L)
                mInventoryDetailList.remove(0);
            else {
                Iterator<InventoryDetail> iterator = mInventoryDetailList.iterator();
                while(iterator.hasNext()) {
                    InventoryDetail inventoryDetail = iterator.next();
                    if(inventoryDetail.id == id) {
                        iterator.remove();
                        break;
                    }
                }
            }
            updateStatisticInfo(mLastBinCoding);
            mRecordFragment.doRefreshOnRecyclerView();
            mScannerNeedRefresh = true;
            mRecordListNeedRefresh = false;
            mRecordTotalNeedRefresh = true;
            Toast.makeText(MyApplication.getInstance(), "记录已经删除", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MyApplication.getInstance(), "删除记录出错，请检查", Toast.LENGTH_SHORT).show();
        }
        return deleteSuccess;
    }

    /**
     * 向本地数据库插入盘点明细数据
     * @param barcode
     * @param binCoding
     * @param num
     * @return 记录ID，-1出错
     */
    private long insertDetailRecord(String barcode, String binCoding, int num) {
        long result = -1L;
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            InventoryDetail inventoryDetail = new InventoryDetail(-1L, mStatisticInfo.id, binCoding, barcode, num);
            ContentValues contentValues = SQLiteUtil.toContentValues(inventoryDetail, "id");
            result = db.insert(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "id", contentValues);
            if(result == -1L)
                throw new Exception("新增记录出错");
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e("ERROR", t.getMessage(), t);
            //Toast.makeText(MyApplication.getInstance(), "新增记录出错", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        return result;
    }

    /**
     * 向本地数据库删除盘点明细数据
     * @param id 待删除记录ID，-1L则删除最新一条记录
     * @return 成功
     */
    private boolean deleteDetailRecord(long id) {
        int result = 0; //影响的记录数，0出错
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            if(!mInventoryDetailList.isEmpty()) {
                if(id == -1L)
                    id = mInventoryDetailList.get(0).id;    //删除最新一条
                result = db.delete(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "id=?", new String[]{Long.toString(id)});
                if(result == 0)
                    throw new Exception("删除记录出错");
            }
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e("ERROR", t.getMessage(), t);
            //Toast.makeText(MyApplication.getInstance(), "删除记录出错", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        return result > 0;
    }

    /**
     * 向本地数据库删除盘点单全部数据
     * @param id 待删除盘点单ID
     * @return 成功
     */
    private boolean deleteInventory(long id) {
        int result = 0; //影响的记录数，0出错
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            result = db.delete(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "pid=?", new String[]{Long.toString(id)});
            if(result != 0)
                result = db.delete(SQLiteDbHelper.TABLE_INVENTORY, "id=?", new String[]{Long.toString(id)});
            if(result == 0)
                throw new Exception("删除盘点单出错");
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e("ERROR", t.getMessage(), t);
            //Toast.makeText(MyApplication.getInstance(), "删盘点单出错", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        return result > 0;
    }

    //复盘
    private void locatorRedo(String bin_coding) {

    }

    //删除盘存表
    private void delInventoryList() {
        if(!deleteInventory(mListId)) {
            Toast.makeText(MyApplication.getInstance(), "删盘点单出错，请检查", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(MyApplication.getInstance())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("盘点单已经删除")
                    .setMessage("单据尾号（" + StringUtil.right(mInventory.list_no, 4) + "），总数量（" + mInventoryDetailList.size() + "）")
                    .setCancelable(true)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            setResult(4, intent);
                            finish();
                        }
                    })
                    .show();
        }
    }

    //上传盘存表
    private void uploadInventoryList() {
        Toast.makeText(MyApplication.getInstance(), "待实现", Toast.LENGTH_SHORT).show();
    }

    //导出盘存表
    private void exportInventoryList() {
        if(!GlobalVars.permissionOfStorage) {
            Toast.makeText(MyApplication.getInstance(), "缺失文件读写权限，请在设置中修改", Toast.LENGTH_SHORT).show();
            return;
        }
        //输出Excel
        String filePath = Environment.getExternalStorageDirectory() + "/StoreHelperExport";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String excelFileName = "/Inventory" + DateUtil.toStr(new Date(), "yyyyMMddHHmmss") + ".xls";
        String[] title = {"标识1", "标识2", "货架编码", "商品条码", "数量"};
        String sheetName = "demoSheetName";
        filePath = filePath + excelFileName;
        if(ExcelUtil.initExcel(filePath, sheetName, title))
            ExcelUtil.writeObjListToExcel(mInventoryDetailList, filePath);
    }

    @Override
    public StatisticInfo onInventoryNewRecord(String binCoding, String barcode, int num) {
        mLastBinCoding = binCoding;
        //新增记录
        long id = insertDetailRecordThenSubsequentActions(barcode, binCoding, num);
        if(id == -1)
            return null;
        else
            return mStatisticInfo;
    }

    @Override
    public StatisticInfo onInventoryRecalculate(String specialBinCoding) {
        mLastBinCoding = specialBinCoding;
        updateStatisticInfo(specialBinCoding);
        return mStatisticInfo;
    }

    @Override
    public void onInventoryDeleteRecord(long id) {
        deleteDetailRecordThenSubsequentActions(id);
    }

    @Override
    public void onInventoryLocatorRedo(String bin_coding) {
        locatorRedo(bin_coding);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mList;

        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.mList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mList == null ? null : this.mList.get(position);
        }

        @Override
        public int getCount() {
            return this.mList == null ? 0 : this.mList.size();
        }
    }

}
