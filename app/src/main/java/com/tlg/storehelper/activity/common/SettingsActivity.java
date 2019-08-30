package com.tlg.storehelper.activity.common;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.ResUtil;
import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.activity.inventory.InventoryActivity;
import com.tlg.storehelper.R;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.android.stickheaderview.StickHeaderDecoration;
import com.nec.lib.android.stickheaderview.StickHeaderRecyclerViewAdapter;
import com.nec.lib.android.stickheaderview.StickHeaderViewGroupData;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseRxAppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private List<MyStickHeaderViewGroupData> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //toolbar
        setSupportActionBar(mToolbar);

        // initialize controls
        hideKeyboard(true);

        //设置RecycleView的布局方式，线性布局默认垂直
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadData();

        SettingsActivity.MyStickHeaderRecyclerViewAdapter recycleViewAdapter = new MyStickHeaderRecyclerViewAdapter(mDatas);
        recycleViewAdapter.setViewHolderClass(recycleViewAdapter, MyStickHeaderRecyclerViewAdapter.MyViewHolder.class);
        mRecyclerView.addItemDecoration(new StickHeaderDecoration(this, 20, 16, 14, ResUtil.getColor("white"), ResUtil.getColor("colorPrimaryLight"), ResUtil.getColor("silver")));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(recycleViewAdapter);

        //mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recycleViewAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                switch (Integer.parseInt(view.getTag().toString())) {
                    case 101:
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("lastModDate", "2000-01-01 00:00:00");
                        editor.commit();
                        RequestUtil.requestGoodBarcodes(_this, null);
                        break;
                    case 102:
                        String filesSizeDesc = calculateCacheSize(true);
                        loadData();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        AndroidUtil.showToast(filesSizeDesc + "缓存已经清理");
                        break;
                    case 103:
                        new AlertDialog.Builder(MyApp.getInstance())
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setTitle("删除提示")
                                .setMessage("是否删除所有的盘点单？")
                                .setCancelable(true)
                                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int del_result = deleteAllInventoty();
                                        if (del_result < 0)
                                            AndroidUtil.showToast("盘点数据清除失败");
                                        else if (del_result == 0)
                                            AndroidUtil.showToast("没有盘点数据需要清除");
                                        else
                                            AndroidUtil.showToast("盘点数据已经全部清除");
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
                    default:
                        ;
                }
                Log.d(SettingsActivity.class.getName(), "clicked");
            }

            @Override
            public boolean onItemLongClick(View view, int postion) {
                //AndroidUtil.showToast("long click " + postion);
                return true;
            }
        });
    }

    private String calculateCacheSize(boolean deletion) {
        long filesSize = 0;
        //更新包
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "Download/StoreHelper.apk");
        if(file.exists()) {
            filesSize += file.length();
            if(deletion)
                file.delete();
        }
        //Excel导出
        file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "StoreHelper/export");
        if(file.exists() && file.isDirectory()) {
            for (File _file : file.listFiles()) {
                filesSize += _file.length();
                if(deletion)
                    _file.delete();
            }
        }
        //HTTP缓存
        file = new File(Environment.getDataDirectory().getAbsoluteFile(), "data/com.tlg.storehelper/cache/HttpCache");
        if(file.exists() && file.isDirectory()) {
            for (File _file : file.listFiles()) {
                filesSize += _file.length();
                if(deletion)
                    _file.delete();
            }
        }
        //图片
        file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "StoreHelper/pic");
        if(file.exists() && file.isDirectory()) {
            for (File _file : file.listFiles()) {
                filesSize += _file.length();
                if(deletion)
                    _file.delete();
            }
        }
        //输出清理结果
        DecimalFormat formatter = new DecimalFormat("#.#");
        double _G = 1024*1024*1024.0;
        double _M = 1024*1024.0;
        double _K = 1024.0;
        double _filesSize = filesSize;
        String filesSizeDesc = _filesSize >= _G ? formatter.format(_filesSize/_G)+"G" :
                _filesSize >= _M ? formatter.format(_filesSize/_M)+"M" : formatter.format(_filesSize/_K)+"K";
        return filesSizeDesc;
    }

    private void loadData() {
        mDatas.clear();
        mDatas.add(new MyStickHeaderViewGroupData(0, false, "应用名称", "店铺助手", "基本信息"));
        PackageManager manager = MyApp.getInstance().getPackageManager();
        String version = "";
        try {
            PackageInfo info = manager.getPackageInfo(MyApp.getInstance().getPackageName(),0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        mDatas.add(new MyStickHeaderViewGroupData(1, false, "版本", version, "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(101, true, "更新商品资料", "点击更新", "应用管理"));
        String filesSizeDesc = calculateCacheSize(false);
        filesSizeDesc = filesSizeDesc.startsWith("0K") ? "缓存已清空" : filesSizeDesc + "缓存，点击清理";
        mDatas.add(new MyStickHeaderViewGroupData(102, true, "缓存清理", filesSizeDesc, "应用管理"));
        mDatas.add(new MyStickHeaderViewGroupData(103, true, "数据清空", "点击清空", "应用管理"));
    }

    /**
     * 向本地数据库删除全部盘点单
     * @return >0成功 =0无数据 <0失败
     */
    private int deleteAllInventoty() {
        int result = 0; //影响的记录数，0出错
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            String sql = new StringBuffer().append("select count(*)").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .toString();
            Cursor cursor = db.rawQuery(sql, null);
            int recordCount = cursor.moveToFirst() ? cursor.getInt(0) : 0;
            cursor.close();
            if(recordCount > 0) {
                db.delete(SQLiteDbHelper.TABLE_INVENTORY_DETAIL, null, null);
                result = db.delete(SQLiteDbHelper.TABLE_INVENTORY, null, null);
                if (result == 0)
                    throw new Exception("没有盘点单被删除");
                db.setTransactionSuccessful();
            }
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            result = -1;
            //AndroidUtil.showToast("删盘点单出错");
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        return result;
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

    class MyStickHeaderViewGroupData extends StickHeaderViewGroupData {
        public int id;
        public boolean clickable;
        public String caption;
        public String content;

        public MyStickHeaderViewGroupData(int id, boolean clickable, String caption, String content, String groupName) {
            this.id = id;
            this.clickable = clickable;
            this.caption = caption;
            this.content = content;
            this.groupName = groupName;
        }
    }


    class MyStickHeaderRecyclerViewAdapter extends StickHeaderRecyclerViewAdapter<MyStickHeaderViewGroupData> {

        private RecycleViewItemClickListener mClickListener;

        public MyStickHeaderRecyclerViewAdapter(List<MyStickHeaderViewGroupData> list) {
            super(list);
            sLayoutOfRecyclerView = "activity_settings_recyclerview";
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MyViewHolder mHolder = (MyViewHolder) holder;
            mHolder.itemView.setTag(mValues.get(position).id);
            mHolder.mTvCaption.setText(mValues.get(position).caption);
            mHolder.mTvContent.setText(mValues.get(position).content);
            mHolder.mView.setClickable(mValues.get(position).clickable);
        }

        public class MyViewHolder extends StickHeaderViewHolder implements View.OnClickListener {
            public TextView mTvCaption;
            public TextView mTvContent;

            public MyViewHolder(View itemView) {
                super(itemView);
                //为ItemView添加点击事件
                itemView.setOnClickListener(this);
                //实例化自定义对象
                mTvCaption = itemView.findViewById(R.id.tvCaption);
                mTvContent = itemView.findViewById(R.id.tvContent);
            }

            @Override
            public void onClick(View view) {
                if(mClickListener != null)
                    mClickListener.onItemClick(view, getPosition());
            }
        }

        public void setOnItemClickListener(RecycleViewItemClickListener listener) {
            this.mClickListener = listener;
        }

    }
}
