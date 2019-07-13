package com.tlg.storehelper.activity.inventory;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.lib.application.MyApplication;
import com.nec.lib.base.BaseAppCompatActivity;
import com.nec.lib.base.RecycleViewItemClickListener;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.tlg.storehelper.vo.InventoryListVo;

import java.util.ArrayList;
import java.util.List;

public class InventoryListsActivity extends BaseAppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private List<InventoryListVo> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_lists);
        initView();
    }

    @Override
    protected void onRestart() {    //返回刷新数据
        super.onRestart();
        if(loadData())
            mRecyclerView.getAdapter().notifyDataSetChanged();
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
        RecyclerViewAdapter recycleViewAdapter = new RecyclerViewAdapter(InventoryListsActivity.this, mDatas);
        mRecyclerView.setAdapter(recycleViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recycleViewAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                Intent intent = new Intent(InventoryListsActivity.this, InventoryActivity.class);
                //intent.putExtra("list_id", mDatas.get(postion).id);
                intent.putExtra("list_id", ((Long)view.getTag()).longValue());
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, int postion) {
                //Toast.makeText(MyApplication.getInstance(), "long click " + postion, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private boolean loadData() {
        boolean result = false;
        List<InventoryListVo> datas = new ArrayList<>();
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select a.id, a.list_no, sum(b.quantity) as quantity").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY)
                    .append(" a left join ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL).append(" b on a.id=b.pid")
                    .append(" where a.store_code=?")
                    .append(" group by a.id,a.list_no order by a.id desc")
                    .append(" limit 0,50")
                    .toString();
            Cursor cursor = db.rawQuery(sql, new String[] {GlobalVars.storeCode});
            while(cursor.moveToNext()){
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                String list_no = cursor.getString(cursor.getColumnIndex("list_no"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                InventoryListVo inventoryListVo = new InventoryListVo(id, list_no, quantity);
                datas.add(inventoryListVo);
            }
            cursor.close();
            result = true;
        } catch (Throwable t) {
            Toast.makeText(MyApplication.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
            datas.clear();
        } finally {
            db.close();
        }
        if(result) {
            mDatas.clear();
            mDatas.addAll(datas);
        }
        return result;
    }

    ///自定义类继承RecycleView.Adapter类作为数据适配器
    class RecyclerViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<InventoryListVo> mDatas;
        private RecycleViewItemClickListener mClickListener;

        public RecyclerViewAdapter(Context context, List<InventoryListVo> datas) {
            this.mContext = context;
            this.mDatas = datas;
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        ///对控件赋值
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
            recyclerViewHolder.itemView.setTag(mDatas.get(position).id);
            recyclerViewHolder.tvInventoryListNo.setText(mDatas.get(position).list_no);
            recyclerViewHolder.tvQuantity.setText(String.valueOf(mDatas.get(position).quantity));
            recyclerViewHolder.ivIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_view));
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_inventory_lists_recyclerview, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view, mClickListener);
            return recyclerViewHolder;
        }

        public void setOnItemClickListener(RecycleViewItemClickListener listener) {
            this.mClickListener = listener;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            TextView tvInventoryListNo, tvQuantity;
            ImageView ivIcon;
            private RecycleViewItemClickListener mListener;

            public RecyclerViewHolder(View view, RecycleViewItemClickListener listener) {
                super(view);
                mListener = listener;
                //为ItemView添加点击事件
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
                //实例化自定义对象
                tvInventoryListNo = view.findViewById(R.id.tvInventoryListNo);
                tvQuantity = view.findViewById(R.id.tvQuantity);
                ivIcon = view.findViewById(R.id.ivIcon);
            }

            @Override
            public void onClick(View view) {
                mListener.onItemClick(view, getPosition());
            }

            @Override
            public boolean onLongClick(View view) {
                return mListener.onItemLongClick(view, getPosition());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_lists_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_new:
                Intent intent = new Intent(this, InventoryListActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}