package com.tlg.storehelper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.nec.utils.ResUtil;
import com.tlg.storehelper.base.BaseAppCompatActivity;
import com.tlg.storehelper.base.RecycleViewItemClickListener;
import com.tlg.storehelper.stickheaderview.StickHeaderDecoration;
import com.tlg.storehelper.stickheaderview.StickHeaderRecyclerViewAdapter;
import com.tlg.storehelper.stickheaderview.StickHeaderViewGroupData;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseAppCompatActivity {

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
                Log.d(SettingsActivity.class.getName(), "clicked");
            }

            @Override
            public boolean onItemLongClick(View view, int postion) {
                //Toast.makeText(MyApplication.getInstance(), "long click " + postion, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void loadData() {
        mDatas.add(new MyStickHeaderViewGroupData(0, false, "应用名称", "店铺助手", "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(1, false, "版本", "1.0", "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(2, true, "下载", "最新版本 1.1", "基本信息"));
        mDatas.add(new MyStickHeaderViewGroupData(101, true, "缓存清理", "0M 缓存占用，点击清理", "应用管理"));
        mDatas.add(new MyStickHeaderViewGroupData(102, true, "数据清理", "12M 盘点数据，点击清理", "应用管理"));
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
