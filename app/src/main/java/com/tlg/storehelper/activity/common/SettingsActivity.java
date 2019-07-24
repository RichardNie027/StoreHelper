package com.tlg.storehelper.activity.common;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseRxAppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private List<MyStickHeaderViewGroupData> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);

        //toolbar
        setSupportActionBar(mToolbar);

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
                    case 2:
                        //TODO: download new app file in new thread.
                        break;
                    case 101:
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("lastModDate", "2000-01-01 00:00:00");
                        editor.commit();
                        RequestUtil.requestGoodBarcodes(_this, null);
                        break;
                    case 102:
                        Toast.makeText(MyApp.getInstance(), "缓存已经清理", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(MyApp.getInstance(), "盘点数据清除失败", Toast.LENGTH_SHORT).show();
                                        else if (del_result == 0)
                                            Toast.makeText(MyApp.getInstance(), "没有盘点数据需要清除", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(MyApp.getInstance(), "盘点数据已经全部清除", Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(MyApp.getInstance(), "long click " + postion, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void loadData() {
        mDatas.add(new MyStickHeaderViewGroupData(0, false, "应用名称", "店铺助手", "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(1, false, "版本", "1.0", "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(2, true, "下载", "最新版本 1.1", "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(101, true, "更新商品资料", "点击更新", "应用管理"));
        mDatas.add(new MyStickHeaderViewGroupData(102, true, "缓存清理", "点击清理", "应用管理"));
        mDatas.add(new MyStickHeaderViewGroupData(103, true, "数据清理", "点击清理", "应用管理"));
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
            //Toast.makeText(MyApp.getInstance(), "删盘点单出错", Toast.LENGTH_SHORT).show();
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
