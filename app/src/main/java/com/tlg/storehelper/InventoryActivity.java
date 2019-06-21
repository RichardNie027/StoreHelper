package com.tlg.storehelper;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.tlg.storehelper.base.BaseAppCompatActivity;
import com.tlg.storehelper.loadmorerecycler.ListFragment;
import com.tlg.storehelper.utils.DateUtil;
import com.tlg.storehelper.dao.Inventory;
import com.tlg.storehelper.dao.InventoryDetail;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.tlg.storehelper.utils.SQLiteUtil;
import com.tlg.storehelper.vo.StatisticInfo;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends BaseAppCompatActivity
        implements ScannerFragment.OnFragmentInteractionListener, RecordFragment.OnFragmentInteractionListener {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;

    private long mListId; //调用者传递过来的“盘点ID”，-1L为错误
    private Inventory mInventory = new Inventory();
    private List<InventoryDetail> mInventoryDetailList = new ArrayList<>();

    private StatisticInfo mStatisticInfo = new StatisticInfo();  //盘点单统计信息

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
            Toast.makeText(this, "出错了，盘点单信息丢失", Toast.LENGTH_SHORT).show();
            //todo: disable controls.
            return;
        } else {
            //装置数据
            loadData(true, true);
        }
        // init fragment
        mFragments = new ArrayList<Fragment>(3);
        updateStatisticInfo(null);
        mFragments.add(ScannerFragment.newInstance(mStatisticInfo));
        mFragments.add(ListFragment.newInstance(RecordRecyclerViewItemAdapter.class, new SimpleDataRequest(), ListFragment.DisplayMode.LINEAR, 3));
        mFragments.add(RecordFragment.newInstance("3","33"));
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
            System.out.println(t.getMessage());
        } finally {
            db.close();
        }
    }

    /**
     * 重新统计盘点单信息
     * @param specailBinCoding 统计货位数量相关。未指定货位（NULL：统计最后货位；空白：统计无货位）
     */
    private void updateStatisticInfo(String specailBinCoding) {
        mStatisticInfo.id = mInventory.id;
        mStatisticInfo.listNo = mInventory.list_no;
        mStatisticInfo.quantity = 0;
        mStatisticInfo.totalQuantity = 0;
        mStatisticInfo.lastBarcode = "";
        mStatisticInfo.lastBinCoding = "";
        if(mInventoryDetailList.size() > 0) {
            mStatisticInfo.lastBarcode = mInventoryDetailList.get(0).barcode;
            mStatisticInfo.lastBinCoding = mInventoryDetailList.get(0).bin_coding;
            if(specailBinCoding == null)
                specailBinCoding = mStatisticInfo.lastBinCoding;
            int quantities = 0;
            int totalQuantities = 0;
            for(InventoryDetail detail: mInventoryDetailList) {
                if(detail.bin_coding.equals(specailBinCoding)) {
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
                menu.findItem(R.id.action_locator_redo).setVisible(false);
                menu.findItem(R.id.action_del_inventory_list).setVisible(false);
                menu.findItem(R.id.action_upload_inventory_list).setVisible(false);
                menu.findItem(R.id.action_export_inventory_list).setVisible(false);
                break;
            case 1:
                menu.findItem(R.id.action_del_top_line).setVisible(true);
                menu.findItem(R.id.action_locator_redo).setVisible(false);
                menu.findItem(R.id.action_del_inventory_list).setVisible(false);
                menu.findItem(R.id.action_upload_inventory_list).setVisible(false);
                menu.findItem(R.id.action_export_inventory_list).setVisible(false);
                break;
            case 2:
                menu.findItem(R.id.action_del_top_line).setVisible(false);
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
//            case R.id.action_save:
//                Toast.makeText(this, "保存完成", Toast.LENGTH_SHORT).show();
//                break;
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
     * 向本地数据库插入数据
     * @param barcode
     * @param binCoding
     * @param num
     * @return
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
                throw new Exception("新增数据记录出错");
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        return result;
    }

    @Override
    public StatisticInfo onInventoryNewRecord(String binCoding, String barcode, int num) {
        //新增记录
        long id = insertDetailRecord(barcode, binCoding, num);
        if(id == -1) {
            Toast.makeText(this, "新增数据失败，请检查", Toast.LENGTH_SHORT).show();
            return null;
        }
        mInventoryDetailList.add(0, new InventoryDetail(id, mStatisticInfo.id, binCoding, barcode, num));
        updateStatisticInfo(binCoding);
        return mStatisticInfo;
    }

    @Override
    public StatisticInfo onInventoryRecalculate(String specailBinCoding) {
        updateStatisticInfo(specailBinCoding);
        return mStatisticInfo;
    }

    @Override
    public void onInventoryDeleteRecord(long id) {
        Toast.makeText(this, id+" was deleted.", Toast.LENGTH_SHORT).show();
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
