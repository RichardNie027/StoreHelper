package com.tlg.storehelper.activity.collocation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.nec.lib.android.utils.ImageUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.net.entity.CollocationEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

import java.util.ArrayList;
import java.util.List;

public class CollocationActivity extends BaseRxAppCompatActivity {

    private EditText mEtBarcode;
    private TextView tvGoodsName;   //商品名称
    private TextView tvGoodsNo;     //商品货号
    private TextView tvPrice;       //吊牌价
    private ImageView ivPic;        //图片
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private List<CollocationEntity.DetailBean> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collocation);
        initView();
    }

    @Override
    protected void onRestart() {    //返回刷新数据
        super.onRestart();
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

    private void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEtBarcode = findViewById(R.id.etBarcode);
        tvGoodsName = findViewById(R.id.tvGoodsName);
        tvGoodsNo = findViewById(R.id.tvGoodsNo);
        tvPrice = findViewById(R.id.tvPrice);
        ivPic = findViewById(R.id.ivPic);

        //toolbar
        setSupportActionBar(mToolbar);

        // initialize controls
        hideKeyboard(true);

        //设置RecycleView的布局方式，线性布局默认垂直1
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, 0, false));

        RecyclerViewAdapter recycleViewAdapter = new RecyclerViewAdapter(CollocationActivity.this, mDatas);
        mRecyclerView.setAdapter(recycleViewAdapter);
        recycleViewAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                Intent intent = new Intent(CollocationActivity.this, CollocationActivity.class);
                intent.putExtra("goodsNo", view.getTag().toString());
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, int postion) {
                //Toast.makeText(MyApp.getInstance(), "long click " + postion, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        ///设置“条形码”控件
        //回车键响应
        mEtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    onScanBarcode(mEtBarcode.getText().toString());
                    //mEtBarcode.requestFocus();
                    mEtBarcode.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEtBarcode.requestFocus();
                        }
                    }, 500);
                    return true;
                }
                return false;
            }
        });
        //获得焦点全选
        mEtBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtBarcode.selectAll();
                }
            }
        });
        //Touch清空条形码
        mEtBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mEtBarcode.setText("");
                return false;
            }
        });
    }

    public void onScanBarcode(String barcode) {
        barcode = barcode.toUpperCase();
        if(barcode.length() > 0) {
            if (DbUtil.checkGoodsBarcode(barcode, false)) {
                mEtBarcode.setText("");
            } else {                    //错误
                Toast.makeText(MyApp.getInstance(), "货号 / 条码不存在", Toast.LENGTH_SHORT).show();
                mEtBarcode.selectAll();
                return;
            }
        } else
            return;
        RequestUtil.requestCollocation(barcode, _this, new RequestUtil.OnSuccessListener<CollocationEntity>() {

            @Override
            public void onSuccess(CollocationEntity response) {
                tvGoodsName.setText(response.goodsName);
                tvGoodsNo.setText(response.goodsNo);
                tvPrice.setText(String.valueOf(response.price));
                RequestUtil.downloadPic(MyApp.baseUrl + "pre_api/pic/" + response.pic, _this, new RequestUtil.OnFileDownloadedListener() {

                    @Override
                    public void onSuccess(String pathFile) {
                        Bitmap bitmap = BitmapFactory.decodeFile(pathFile, ImageUtil.getBitmapOption(2));
                        ivPic.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFail() {

                    }
                });
            }
        });
    }

    ///自定义类继承RecycleView.Adapter类作为数据适配器
    class RecyclerViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<CollocationEntity.DetailBean> mDatas;
        private RecycleViewItemClickListener mClickListener;

        public RecyclerViewAdapter(Context context, List<CollocationEntity.DetailBean> datas) {
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
            CollocationActivity.RecyclerViewAdapter.RecyclerViewHolder recyclerViewHolder = (CollocationActivity.RecyclerViewAdapter.RecyclerViewHolder) holder;
            recyclerViewHolder.itemView.setTag(mDatas.get(position).goodsNo);
            recyclerViewHolder.tvGoodsNo.setText(mDatas.get(position).goodsNo);
            recyclerViewHolder.tvFrequency.setText(String.valueOf(mDatas.get(position).frequency));
            recyclerViewHolder.ivPic.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_view));
        }

        @Override
        public CollocationActivity.RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_collocation_recyclerview, parent, false);
            CollocationActivity.RecyclerViewAdapter.RecyclerViewHolder recyclerViewHolder = new CollocationActivity.RecyclerViewAdapter.RecyclerViewHolder(view, mClickListener);
            return recyclerViewHolder;
        }

        public void setOnItemClickListener(RecycleViewItemClickListener listener) {
            this.mClickListener = listener;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            TextView tvGoodsNo, tvFrequency;
            ImageView ivPic;
            private RecycleViewItemClickListener mListener;

            public RecyclerViewHolder(View view, RecycleViewItemClickListener listener) {
                super(view);
                mListener = listener;
                //为ItemView添加点击事件
                view.setOnClickListener(this);
                //view.setOnLongClickListener(this);
                //实例化自定义对象
                tvGoodsNo = view.findViewById(R.id.tvGoodsNo);
                tvFrequency = view.findViewById(R.id.tvFrequency);
                ivPic = view.findViewById(R.id.ivIcon);
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

}
