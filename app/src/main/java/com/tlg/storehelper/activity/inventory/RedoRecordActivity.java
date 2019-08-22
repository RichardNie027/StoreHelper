package com.tlg.storehelper.activity.inventory;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;

public class RedoRecordActivity extends BaseRxAppCompatActivity {

    private Toolbar mToolbar;

    /**调用者传递过来的“盘点ID”，-1L为错误*/
    private String mListId;
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
        mListId = intent.getStringExtra(RedoRecordFragment.sInventoryListIdLabel);
        mBinCoding = intent.getStringExtra(RedoRecordFragment.sInventoryBinCodingLabel);
        initView();
    }

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        //toolbar
        setSupportActionBar(mToolbar);
        // initialize controls
        hideKeyboard(true);
        mToolbar.setTitle("复盘：" + mBinCoding);
        //处理异常
        if(mListId.isEmpty() || mBinCoding.isEmpty()) {
            new AlertDialog.Builder(MyApp.getInstance())
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
        dataBundle.putString(RedoRecordFragment.sInventoryListIdLabel, mListId);
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
                new AlertDialog.Builder(MyApp.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("更新提示")
                        .setMessage("用复盘数据更新盘点单？")
                        .setCancelable(true)
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!mRedoRecordFragment.doUpdteInventory()) {
                                    Toast.makeText(MyApp.getInstance(), "更新货位盘点数据失败，请检查", Toast.LENGTH_SHORT).show();
                                } else {
                                    mInventoryUpdated = true;
                                    Toast.makeText(MyApp.getInstance(), "货位盘点数据已更新", Toast.LENGTH_SHORT).show();
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
