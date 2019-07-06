package com.tlg.storehelper;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nec.application.MyApplication;
import com.nec.utils.DateUtil;
import com.nec.utils.ExcelUtil;
import com.nec.utils.SQLiteUtil;
import com.nec.utils.StringUtil;
import com.tlg.storehelper.base.BaseAppCompatActivity;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.Inventory;
import com.tlg.storehelper.dao.InventoryDetail;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.tlg.storehelper.vo.InventoryRedoDetailVo;
import com.tlg.storehelper.vo.StatisticInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RedoRecordActivity extends BaseAppCompatActivity {

    private Toolbar mToolbar;

    /**调用者传递过来的“盘点ID”，-1L为错误*/
    private long mListId;
    /**调用者传递过来的“盘点货位”，空白为错误*/
    private String mBinCoding;

    /**数据发生变化，盘点页面信息需要刷新*/
    private boolean mParentNeedRefresh = false;

    /**复盘页面*/
    private RedoRecordFragment mRedoRecordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redo_record);
        //接收参数
        Intent intent =getIntent();
        mListId = intent.getLongExtra(RedoRecordFragment.sInventoryListIdLabel, -1L);
        mBinCoding = intent.getStringExtra(RedoRecordFragment.sInventoryBinCodingLabel);
        initView();
    }

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        //toolbar
        setSupportActionBar(mToolbar);
        //处理异常
        if(mListId == -1L || mBinCoding.isEmpty()) {
            new AlertDialog.Builder(MyApplication.getInstance())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("内部错误")
                    .setMessage("盘点单关键信息丢失")
                    .setCancelable(true)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            setResult(6, intent);
                            finish();
                        }
                    })
                    .show();
            return;
        }
        // 初始化并动态添加 fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(RedoRecordFragment.sInventoryListIdLabel, mListId);
        dataBundle.putString(RedoRecordFragment.sInventoryBinCodingLabel, mBinCoding);
        RedoRecordListDataRequest redoRecordListDataRequest = new RedoRecordListDataRequest();
        mRedoRecordFragment = RedoRecordFragment.newInstance(RedoRecordFragment.class, RedoRecordRecyclerViewItemAdapter.class, redoRecordListDataRequest, dataBundle);
        redoRecordListDataRequest.setmListener(mRedoRecordFragment);
        fragmentTransaction.add(R.id.fragment_container, mRedoRecordFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_list_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 动态设置ToolBar菜单
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean mInventoryUpdated = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                if(mInventoryUpdated)
                    setResult(7, intent);
                else
                    setResult(0, intent);
                finish();
                return true;
            case R.id.action_save:
                new AlertDialog.Builder(MyApplication.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("更新提示")
                        .setMessage("用复盘数据更新盘点单？")
                        .setCancelable(true)
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!mRedoRecordFragment.doUpdteInventory()) {
                                    Toast.makeText(MyApplication.getInstance(), "更新货位盘点数据失败，请检查", Toast.LENGTH_SHORT).show();
                                } else {
                                    mInventoryUpdated = true;
                                    Toast.makeText(MyApplication.getInstance(), "货位盘点数据已更新", Toast.LENGTH_SHORT).show();
                                    mRedoRecordFragment.doRefreshOnRecyclerView();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(RedoRecordActivity.class.getName(), "复盘数据没有更新");
                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
