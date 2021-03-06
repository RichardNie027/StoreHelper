package com.tlg.storehelper.activity.collocation;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.R;
import com.tlg.storehelper.activity.common.GoodsSearchingFragment;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.net.entity.CollocationVo;
import com.tlg.storehelper.httprequest.utils.PicUtil;
import com.tlg.storehelper.httprequest.utils.RequestUtil;
import com.tlg.storehelper.vo.GoodsSimpleVo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CollocationActivity extends BaseRxAppCompatActivity {

    private EditText mEtBarcode;
    private TextView tvGoodsName;   //商品名称
    private TextView tvGoodsNo;     //商品货号
    private TextView tvPrice;       //吊牌价
    private ImageView ivPic;        //图片
    private RecyclerView mRecyclerView;
    private List<GoodsSimpleVo> mDatas = new ArrayList<>();
    private String mGoodsNo;        //预设货号

    private String localPicPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/StoreHelper/pic/";

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected int setToolbarResourceID() {
        return R.id.toolbar;
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

    @Override
    protected void initView() {
        //接收参数
        Intent intent =getIntent();
        mGoodsNo = intent.getStringExtra("goodsNo");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEtBarcode = findViewById(R.id.etBarcode);
        tvGoodsName = findViewById(R.id.tvGoodsName);
        tvGoodsNo = findViewById(R.id.tvGoodsNo);
        tvPrice = findViewById(R.id.tvPrice);
        ivPic = findViewById(R.id.ivPic);

        hideKeyboard(true);

        //setup view
        tvGoodsNo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AndroidUtil.copyText(tvGoodsNo.getText().toString());
                AndroidUtil.showToast("货号已复制");
                return true;
            }
        });
        ivPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag() == null)
                    return;
                Intent intent = new Intent(CollocationActivity.this, PhotoViewActivity.class);
                intent.putExtra("file_path", view.getTag().toString());
                startActivity(intent);
            }
        });

        //设置RecycleView的布局方式，线性布局默认垂直1
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

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
        if (mGoodsNo!=null && !mGoodsNo.isEmpty()) {
            mEtBarcode.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onScanBarcode(mGoodsNo);
                    mEtBarcode.requestFocus();
                }
            }, 500);
        }
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_collocation;
    }

    public void onScanBarcode(String barcode) {
        barcode = barcode.toUpperCase();
        String goodsNo = "";
        if(barcode.length() > 0) {
            goodsNo = DbUtil.checkGoodsBarcode(barcode, true);
            if (!goodsNo.isEmpty()) {
                mEtBarcode.setText("");
                showGoodsCollocation(goodsNo);
            } else {
                //货号模糊查找
                searchingGoods(barcode);
            }
        }
    }

    private void searchingGoods(String input) {
        LinkedHashMap<String, String> goodsMap = DbUtil.checkGoodsNoList(GlobalVars.storeCode.substring(0,1), input, 20);
        if(goodsMap.isEmpty()) {
            AndroidUtil.showToast("货号 / 条码不存在");
            mEtBarcode.selectAll();
            mEtBarcode.requestFocus();
            return;
        } else if(goodsMap.size() == 1) {
            showGoodsCollocation(goodsMap.keySet().iterator().next());
            return;
        }
        GoodsSearchingFragment fragment = new GoodsSearchingFragment(goodsMap);
        fragment.setTextViewOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.getDialog().dismiss();
                bottomDialogItemClick(view.getTag().toString());
            }
        });
        fragment.showDialog(getSupportFragmentManager());
    }

    private void bottomDialogItemClick(String goodsNo) {
        showGoodsCollocation(goodsNo);
    }

    private void showGoodsCollocation(String goodsNo) {
        RequestUtil.requestCollocation(goodsNo, GlobalVars.storeCode.substring(0,1), _this, new RequestUtil.OnSuccessListener<CollocationVo>() {

            @Override
            public void onSuccess(CollocationVo response) {
                tvGoodsName.setText(response.goods.goodsName);
                tvGoodsNo.setText(response.goods.goodsNo);
                tvPrice.setText(new DecimalFormat("￥,###").format(response.goods.price));
                PicUtil.loadPic(ivPic, localPicPath, response.goods.goodsNo, 2);
                ivPic.setTag(localPicPath + response.goods.goodsNo);
                RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(CollocationActivity.this, response.detail);
                mRecyclerView.setAdapter(recyclerViewAdapter);
                recyclerViewAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showGoodsCollocation(view.getTag().toString());
                    }

                    @Override
                    public boolean onItemLongClick(View view, int position) {
                        AndroidUtil.copyText(view.getTag().toString());
                        AndroidUtil.showToast("货号已复制");
                        return true;
                    }
                });
            }
        });
    }

    ///自定义类继承RecycleView.Adapter类作为数据适配器
    class RecyclerViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<GoodsSimpleVo> mDatas;
        private RecycleViewItemClickListener mClickListener;

        public RecyclerViewAdapter(Context context, List<GoodsSimpleVo> datas) {
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
            recyclerViewHolder.itemView.setTag(mDatas.get(position).goodsNo);
            recyclerViewHolder.tvGoodsNo.setText(mDatas.get(position).goodsNo);
            recyclerViewHolder.tvInfo.setText("搭配"+mDatas.get(position).sales+" 存"+mDatas.get(position).stock);
            PicUtil.loadPic(recyclerViewHolder.ivPic, localPicPath, mDatas.get(position).goodsNo, 2);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_collocation_recyclerview, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view, mClickListener);
            return recyclerViewHolder;
        }

        public void setOnItemClickListener(RecycleViewItemClickListener listener) {
            this.mClickListener = listener;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            TextView tvGoodsNo, tvInfo;
            ImageView ivPic;
            private RecycleViewItemClickListener mListener;

            public RecyclerViewHolder(View view, RecycleViewItemClickListener listener) {
                super(view);
                mListener = listener;
                //为ItemView添加点击事件
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
                //实例化自定义对象
                tvGoodsNo = view.findViewById(R.id.tvGoodsNo);
                tvInfo = view.findViewById(R.id.tvInfo);
                ivPic = view.findViewById(R.id.ivPic);
            }

            @Override
            public void onClick(View view) {
                if(mListener != null)
                    mListener.onItemClick(view, getPosition());
            }

            @Override
            public boolean onLongClick(View view) {
                if(mListener != null)
                    return mListener.onItemLongClick(view, getPosition());
                else
                    return true;
            }
        }
    }

}
