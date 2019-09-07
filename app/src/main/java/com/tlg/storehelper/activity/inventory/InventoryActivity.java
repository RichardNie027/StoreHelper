package com.tlg.storehelper.activity.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.ExcelUtil;
import com.nec.lib.android.utils.StringUtil;
import com.nec.lib.android.utils.DateUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.dao.Inventory;
import com.tlg.storehelper.dao.InventoryDetail;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.android.utils.SQLiteUtil;
import com.tlg.storehelper.httprequest.net.entity.InventoryEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;
import com.tlg.storehelper.vo.StatisticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class InventoryActivity extends BaseRxAppCompatActivity
        implements ScannerFragment.OnFragmentInteractionListener, RecordFragment.OnFragmentInteractionListener, TotalRecordFragment.OnFragmentInteractionListener {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    /**调用者传递过来的“盘点ID”，""为错误*/
    private String mListId = "";
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
    /**当前盘点单明细最大序号，加载数据时获取*/
    private int mMaxDetailIdx = 0;

    private ImageView mIvSeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        //接收参数
        Intent intent =getIntent();
        mListId = intent.getStringExtra("list_id");
        initView();
    }

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.vpFragment);
        mTabRadioGroup = findViewById(R.id.rgTabs);
        mIvSeal = findViewById(R.id.ivSeal);

        //toolbar
        setSupportActionBar(mToolbar);
        // initialize controls
        hideKeyboard(true);
        //处理异常
        if(mListId.isEmpty()) {
            new AlertDialog.Builder(MyApp.getInstance())
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
            loadData(true, true);   //装载数据
        }
        // initialize controls
        mFragments = new ArrayList<Fragment>(3);
        updateStatisticInfo(null);
        mScannerFragment = ScannerFragment.newInstance(mStatisticInfo);
        mFragments.add(mScannerFragment);

        Bundle dataBundle2 = new Bundle();
        dataBundle2.putString(RecordFragment.sInventoryListIdLabel, mListId);
        dataBundle2.putString(RecordFragment.sInventoryListNoLabel, mInventory.listNo);
        mRecordFragment = RecordFragment.newInstance(RecordFragment.class, RecordRecyclerViewItemAdapter.class, new RecordListDataRequest(), dataBundle2);
        mFragments.add(mRecordFragment);

        Bundle dataBundle3 = new Bundle();
        dataBundle3.putString(TotalRecordFragment.sInventoryListIdLabel, mListId);
        dataBundle3.putString(TotalRecordFragment.sInventoryListNoLabel, mInventory.listNo);
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
                new AlertDialog.Builder(MyApp.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("删除提示")
                        .setMessage("是否删除最近的一条记录？")
                        .setCancelable(true)
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDetailRecordThenSubsequentActions("");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(InventoryActivity.class.getName(), "没有删除记录");
                            }
                        })
                        .show();
                break;
            case R.id.action_locator_redo:
                if(mInventoryDetailList.size() == 0)
                    AndroidUtil.showToast("没有盘点内容");
                else
                    AndroidUtil.showToast("长按需要复盘的货位");
                break;
            case R.id.action_del_inventory_list:
                new AlertDialog.Builder(MyApp.getInstance())
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
                                Log.d(InventoryActivity.class.getName(), "没有删除盘点单");
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
                            updateStatisticInfo(mLastBinCoding);
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
            if(position == 0)
                mScannerFragment.switched();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==5 && resultCode == 6 && data != null) { //失败，因关键信息缺失
            finish();
        } else if(requestCode==5 && resultCode == 7 && data != null) {  //更新成功
            loadData(true, true);   //装载数据
            mScannerNeedRefresh = true;
            mRecordListNeedRefresh = true;
            mTotalRecordFragment.doRefreshOnRecyclerView();
            mRecordTotalNeedRefresh = false;
        }
    }

    /**从数据库加载盘点单*/
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
                cursor = db.rawQuery(sql, new String[]{mListId});
                if (cursor.moveToFirst()) {
                    mInventory.id = cursor.getString(cursor.getColumnIndex("id"));
                    mInventory.storeCode = cursor.getString(cursor.getColumnIndex("storeCode"));
                    mInventory.listDate = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("listDate")));
                    mInventory.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                    mInventory.username = cursor.getString(cursor.getColumnIndex("username"));
                    mInventory.listNo = cursor.getString(cursor.getColumnIndex("listNo"));
                    mInventory.status = cursor.getString(cursor.getColumnIndex("status"));
                    mInventory.createTime = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("createTime")));
                    mInventory.lastTime = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("lastTime")));
                } else {
                    mInventory.id = "";
                }
                cursor.close();
            }
            if(loadDetail) {    //读从表
                mInventoryDetailList.clear();
                sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                        .append(" where pid=?")
                        .append(" order by idx desc")
                        .toString();
                cursor = db.rawQuery(sql, new String[]{mListId});
                while (cursor.moveToNext()) {
                    InventoryDetail inventoryDetail = new InventoryDetail();
                    inventoryDetail.id = cursor.getString(cursor.getColumnIndex("id"));
                    inventoryDetail.pid = cursor.getString(cursor.getColumnIndex("pid"));
                    inventoryDetail.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                    inventoryDetail.binCoding = cursor.getString(cursor.getColumnIndex("binCoding"));
                    inventoryDetail.barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                    inventoryDetail.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                    mInventoryDetailList.add(inventoryDetail);
                    //加载数据时，取得最大序号
                    mMaxDetailIdx = inventoryDetail.idx > mMaxDetailIdx ? inventoryDetail.idx : mMaxDetailIdx;
                }
                cursor.close();
            }
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            AndroidUtil.showToast("加载数据失败");
        } finally {
            db.close();
        }
        mIvSeal.setVisibility(mInventory.status.equals("U") ? View.VISIBLE : View.GONE);
    }

    /**
     * 重新统计盘点单信息
     * @param binCoding_lastSetNull 统计货位数量相关。未指定货位（NULL：统计最后货位；空白：统计无货位）
     */
    private void updateStatisticInfo(String binCoding_lastSetNull) {
        mStatisticInfo.id = mInventory.id;
        mStatisticInfo.listNo = mInventory.listNo;
        mStatisticInfo.quantity = 0;
        mStatisticInfo.totalQuantity = 0;
        mStatisticInfo.lastBarcode = "";
        mStatisticInfo.lastBinCoding = "";
        if(mInventoryDetailList.size() > 0) {
            mStatisticInfo.lastBarcode = mInventoryDetailList.get(0).barcode;
            mStatisticInfo.lastBinCoding = mInventoryDetailList.get(0).binCoding;
            if(binCoding_lastSetNull == null)
                binCoding_lastSetNull = mStatisticInfo.lastBinCoding;
            int quantities = 0;
            int totalQuantities = 0;
            for(InventoryDetail detail: mInventoryDetailList) {
                if(detail.binCoding.equals(binCoding_lastSetNull)) {
                    quantities = quantities + detail.quantity;
                }
                totalQuantities = totalQuantities + detail.quantity;
            }
            mStatisticInfo.quantity = quantities;
            mStatisticInfo.totalQuantity = totalQuantities;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  盘点单操作  ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 调用 insertDetailRecord 向本地数据库插入数据，
     * 后续动作：1、调整 mInventoryDetailList；2、更新统计；3、需要刷新的状态
     *
     * 只被ScannerFragment的onInventoryNewRecord接口调用
     *
     * @return 记录ID，"" 出错
     */
    private String insertDetailRecordThenSubsequentActions(int idx, String barcode, String binCoding, int num) {
        if(mInventory.status.equals("U")) {
            AndroidUtil.showToast("盘点单已上传，被锁定");
            return "";
        }
        InventoryDetail inventoryDetail = insertDetailRecord(idx, barcode, binCoding, num);
        if(inventoryDetail != null) {
            mInventoryDetailList.add(0, new InventoryDetail(inventoryDetail.id, mStatisticInfo.id, idx, binCoding, barcode, num));
            updateStatisticInfo(binCoding);
            mScannerNeedRefresh = false;
            mRecordListNeedRefresh = true;
            mRecordTotalNeedRefresh = true;
        } else {
            AndroidUtil.showToast("新增数据失败，请检查");
        }
        return inventoryDetail.id;
    }

    /**
     * 调用 deleteDetailRecord 向本地数据库删除数据（末条），
     * 后续动作：1、调整 mInventoryDetailList；2、更新统计；3、需要刷新的状态
     * @param id 待删除记录ID，-1L则删除最新一条记录
     * @return 成功
     */
    private boolean deleteDetailRecordThenSubsequentActions(String id) {
        if(mInventory.status.equals("U")) {
            AndroidUtil.showToast("盘点单已上传，被锁定");
            return false;
        }
        boolean deleteSuccess = deleteDetailRecord(id);
        if(deleteSuccess) {
            if(id.isEmpty())
                mInventoryDetailList.remove(0);
            else {
                Iterator<InventoryDetail> iterator = mInventoryDetailList.iterator();
                while(iterator.hasNext()) {
                    InventoryDetail inventoryDetail = iterator.next();
                    if(inventoryDetail.id.equals(id)) {
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
            AndroidUtil.showToast("记录已经删除");
        } else {
            AndroidUtil.showToast("删除记录出错，请检查");
        }
        return deleteSuccess;
    }

    //复盘
    private void locatorRedo(String binCoding) {
        if(mInventory.status.equals("U")) {
            AndroidUtil.showToast("盘点单已上传，被锁定");
            return;
        }
        Intent intent = new Intent(this, RedoRecordActivity.class);
        intent.putExtra(RedoRecordFragment.sInventoryListIdLabel, mListId);
        intent.putExtra(RedoRecordFragment.sInventoryBinCodingLabel, binCoding);
        startActivityForResult(intent, 5);
    }

    //删除盘存表
    private void delInventoryList() {
        if(!deleteInventory(mListId)) {
            AndroidUtil.showToast("盘点单未被删除，请检查");
        } else {
            new AlertDialog.Builder(MyApp.getInstance())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("盘点单已经删除")
                    .setMessage("单据尾号（" + StringUtil.right(mInventory.listNo, 4) + "），总数量（" + mInventoryDetailList.size() + "）")
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
        if(mInventory.status.equals("U")) {
            AndroidUtil.showToast("盘点单已上传，被锁定");
            return;
        }
        if(mInventoryDetailList.size() == 0) {
            AndroidUtil.showToast("没有内容需要上传");
            return;
        }
        InventoryEntity inventoryEntity = new InventoryEntity();
        inventoryEntity.id = mInventory.id;
        inventoryEntity.storeCode = mInventory.storeCode;
        inventoryEntity.listDate = mInventory.listDate;
        inventoryEntity.idx = mInventory.idx;
        inventoryEntity.username = mInventory.username;
        inventoryEntity.listNo = mInventory.listNo;
        inventoryEntity.status = mInventory.status;
        inventoryEntity.createTime = mInventory.createTime;
        inventoryEntity.lastTime = mInventory.lastTime;
        for(InventoryDetail detail: mInventoryDetailList) {
            InventoryEntity.DetailBean bean = new InventoryEntity.DetailBean();
            bean.id = detail.id;
            bean.pid = detail.pid;
            bean.idx = detail.idx;
            bean.binCoding = detail.binCoding;
            bean.barcode = detail.barcode;
            bean.quantity = detail.quantity;
            inventoryEntity.detail.add(bean);
        }
        RequestUtil.requestUploadInventory(inventoryEntity, _this, new RequestUtil.OnSuccessListener() {
            @Override
            public void onSuccess(BaseResponseEntity response) {
                Inventory inventory = DbUtil.getInventory(null, mInventory.id);
                inventory.status = "U";
                if(DbUtil.saveInventoty(null, inventory)) {
                    mInventory.status = inventory.status;
                    mIvSeal.setVisibility(mInventory.status.equals("U") ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    //导出盘存表
    private void exportInventoryList() {
        if(mInventoryDetailList.size() == 0) {
            AndroidUtil.showToast("没有内容需要导出");
            return;
        }
        //输出Excel
        String filePath = Environment.getExternalStorageDirectory() + "/StoreHelper/export";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String excelFileName = "/INV" + DateUtil.toStr(new Date(), "yyyyMMddHHmmss") + ".xls";
        String[] title = {"标识", "货架编码", "商品条码", "数量"};
        String sheetName = mInventory.listNo;
        filePath = filePath + excelFileName;
        if(ExcelUtil.initExcel(filePath, sheetName, title))
            ExcelUtil.writeObjListToExcel(mInventoryDetailList, filePath, new String[] {"id","binCoding","barcode","quantity"});
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  内调用函数  ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 向本地数据库插入盘点明细数据，只被insertDetailRecordThenSubsequentActions调用
     * @param barcode
     * @param binCoding
     * @param num
     * @return 记录ID，-1出错
     */
    private InventoryDetail insertDetailRecord(int idx, String barcode, String binCoding, int num) {
        InventoryDetail inventoryDetail = null;
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            inventoryDetail = new InventoryDetail(null, mStatisticInfo.id, idx, binCoding, barcode, num);
            ContentValues contentValues = SQLiteUtil.toContentValues(inventoryDetail);
            long result = db.insert(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, null, contentValues);
            if(result == -1L)
                throw new Exception("新增记录出错");
            Inventory inventory = DbUtil.getInventory(db, mStatisticInfo.id);
            inventory.lastTime = new Date();
            ContentValues contentValues2 = SQLiteUtil.toContentValues(inventory);
            long result2 = db.update(SQLiteDbHelper.TABLE_INVENTORY, contentValues2, "id=?", new String[]{mStatisticInfo.id});
            if(result2 <= 0L)
                throw new Exception("新增记录出错");
            else
                mInventory.lastTime = inventory.lastTime;
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            inventoryDetail = null;
            //AndroidUtil.showToast("新增记录出错");
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return inventoryDetail;
    }

    /**
     * 向本地数据库删除盘点明细数据，只被deleteDetailRecordThenSubsequentActions调用
     * @param id 待删除记录ID，""则删除最新一条记录
     * @return
     */
    private boolean deleteDetailRecord(String id) {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            if(!mInventoryDetailList.isEmpty()) {
                if(id.isEmpty())
                    id = mInventoryDetailList.get(0).id;    //删除最新一条
                long _result = db.delete(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "id=?", new String[]{id});
                if(_result == 0)
                    throw new Exception("没有记录被删除");
                Inventory inventory = DbUtil.getInventory(db, mStatisticInfo.id);
                inventory.lastTime = new Date();
                ContentValues contentValues2 = SQLiteUtil.toContentValues(inventory);
                long result2 = db.update(SQLiteDbHelper.TABLE_INVENTORY, contentValues2, "id=?", new String[]{mStatisticInfo.id});
                if(result2 <= 0L)
                    throw new Exception("记录删除出错");
                else
                    mInventory.lastTime = inventory.lastTime;
            }
            result = true;
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            result = false;
            //AndroidUtil.showToast("删除记录出错");
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }

    /**
     * 向本地数据库，删除盘点单，只被delInventoryList调用
     * @param id 待删除盘点单ID
     * @return 成功
     */
    private boolean deleteInventory(String id) {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            db.delete(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, "pid=?", new String[]{id});
            long _result = db.delete(SQLiteDbHelper.TABLE_INVENTORY, "id=?", new String[]{id});
            if(_result == 0)
                throw new Exception("盘点单未被删除");
            result = true;
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            result = false;
            //AndroidUtil.showToast("删盘点单出错");
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////  接口及实现  ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public StatisticInfo onInventoryNewRecord(String binCoding, String barcode, int num) {
        mLastBinCoding = binCoding;
        //新增记录
        String id = insertDetailRecordThenSubsequentActions(++mMaxDetailIdx, barcode, binCoding, num);
        if(id.isEmpty())
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
    public void onInventoryDeleteRecord(String id) {
        deleteDetailRecordThenSubsequentActions(id);
    }

    @Override
    public void onInventoryLocatorRedo(String binCoding) {
        locatorRedo(binCoding);
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
