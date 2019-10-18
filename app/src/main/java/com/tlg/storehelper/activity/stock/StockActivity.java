package com.tlg.storehelper.activity.stock;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.nec.lib.android.boost.BottomFlexboxDialogFragment;
import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.activity.common.GoodsSearchingFragment;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.DbUtil;
import com.tlg.storehelper.httprequest.net.entity.SimpleListMapResponseVo;
import com.tlg.storehelper.httprequest.utils.RequestUtil;
import com.tlg.storehelper.vo.StockVo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StockActivity extends BaseRxAppCompatActivity {

    private EditText mEtBarcode = null;
    private TextView mTvGoodsNo = null;
    private TextView mTvGoodsName = null;
    private RecyclerView mRecyclerView;
    private List<StockVo> mDatas = new ArrayList<>();

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected int setToolbarResourceID() {
        return R.id.toolbar;
    }

    @Override
    protected void initView() {
        // find view
        mToolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.recyclerView);
        mEtBarcode = findViewById(R.id.etBarcode);
        mTvGoodsNo = findViewById(R.id.tvGoodsNo);
        mTvGoodsName = findViewById(R.id.tvGoodsName);

        hideKeyboard(true);

        //setup view
        mTvGoodsNo.setVisibility(View.GONE);
        mTvGoodsName.setVisibility(View.GONE);
        mTvGoodsNo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AndroidUtil.copyText(mTvGoodsNo.getText().toString());
                AndroidUtil.showToast("货号已复制");
                return true;
            }
        });

        ///设置“条形码”控件
        //回车键响应
        mEtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    String barcode = mEtBarcode.getText().toString().toUpperCase();
                    String goodsNo = "";
                    if(barcode.length() > 0) {
                        goodsNo = DbUtil.checkGoodsBarcode(barcode, true);
                        if (!goodsNo.isEmpty()) {
                            mEtBarcode.setText("");
                            loadData(GlobalVars.storeCode, goodsNo);
                            return true;
                        } else {
                            searchingGoods(barcode);
                        }
                    }
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
                //mEtBarcode.setText("");
                return false;
            }
        });
        //焦点控制
        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtBarcode.requestFocus();
                }
            }
        });


        //设置RecycleView的布局方式，线性布局默认垂直
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this, mDatas);
        mRecyclerView.setAdapter(recyclerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerViewAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(!mDatas.get(position).storeList.isEmpty())
                    popupDialog(mDatas.get(position).storeList);
            }

            @Override
            public boolean onItemLongClick(View view, int position) {
                //AndroidUtil.showToast("long click " + position);
                return true;
            }
        });
    }

    private void searchingGoods(String input) {
        LinkedHashMap<String, String> goodsMap = DbUtil.checkGoodsNoList(GlobalVars.storeCode.substring(0,1), input, 20);
        if(goodsMap.isEmpty()) {
            AndroidUtil.showToast("货号 / 条码不存在");
            mEtBarcode.selectAll();
            mEtBarcode.requestFocus();
            return;
        } else if(goodsMap.size() == 1) {
            bottomDialogItemClick(goodsMap.keySet().iterator().next());
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
        //mEtBarcode.setText("");
        loadData(GlobalVars.storeCode, goodsNo);
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_stock;
    }

    private void loadData(String storeCode, String goodsNo) {
        //获得库存
        RequestUtil.requestStoreStock(storeCode, goodsNo, _this, new RequestUtil.OnSuccessListener<SimpleListMapResponseVo<StockVo>>() {
            @Override
            public void onSuccess(SimpleListMapResponseVo<StockVo> response) {
                mTvGoodsNo.setVisibility(View.VISIBLE);
                mTvGoodsName.setVisibility(View.VISIBLE);
                mTvGoodsNo.setText(response.map.get("goodsNo").toString());
                mTvGoodsName.setText(response.map.get("goodsName").toString());

                mDatas.clear();
                mDatas.addAll(response.list);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });

    }

    private void popupDialog(List<String> datas) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for(String data: datas)
            map.put(data, data);
        new StockChecklistBottomFragment(map).showDialog(getSupportFragmentManager());
    }

    ///自定义类继承RecycleView.Adapter类作为数据适配器
    class RecyclerViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<StockVo> mDatas;
        private RecycleViewItemClickListener mClickListener;

        public RecyclerViewAdapter(Context context, List<StockVo> datas) {
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
            recyclerViewHolder.tvSize.setTag(mDatas.get(position).size);
            recyclerViewHolder.tvSize.setText(mDatas.get(position).size);
            recyclerViewHolder.tvStock.setText(String.valueOf(mDatas.get(position).stock));
            recyclerViewHolder.tvSales.setText(String.valueOf(mDatas.get(position).sales));
            recyclerViewHolder.tvStocksAll.setText(String.valueOf(mDatas.get(position).stocksAll + " 家"));
            recyclerViewHolder.ivIcon.setVisibility(mDatas.get(position).stocksAll>0 ? View.VISIBLE : View.INVISIBLE);
            recyclerViewHolder.ivIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_view));
            if(mDatas.get(position).stock==0 && mDatas.get(position).sales==0 && mDatas.get(position).stocksAll==0)
                holder.itemView.setAlpha(0.5f);
            else
                holder.itemView.setAlpha(1f);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_stock_lists_recyclerview, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view, mClickListener);
            return recyclerViewHolder;
        }

        public void setOnItemClickListener(RecycleViewItemClickListener listener) {
            this.mClickListener = listener;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            TextView tvSize, tvStock, tvSales, tvStocksAll;
            private RecycleViewItemClickListener mListener;
            ImageView ivIcon;

            public RecyclerViewHolder(View view, RecycleViewItemClickListener listener) {
                super(view);
                mListener = listener;
                //为ItemView添加点击事件
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
                //实例化自定义对象
                tvSize = view.findViewById(R.id.tvSize);
                tvStock = view.findViewById(R.id.tvStock);
                tvSales = view.findViewById(R.id.tvSales);
                tvStocksAll = view.findViewById(R.id.tvStocksAll);
                ivIcon = view.findViewById(R.id.ivIcon);
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
                    return false;
            }
        }
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

    /**库存速览，底部弹窗*/
    public static class StockChecklistBottomFragment extends BottomFlexboxDialogFragment {

        @Override
        protected int setLayoutResourceID() {
            return R.layout.fragment_stock_checklist_bottom;
        }

        @Override
        protected int setRecyclerViewResourceID() {
            return R.id.recyclerView;
        }

        public StockChecklistBottomFragment(LinkedHashMap<String, String> datas) {
            super();
            this.setDataList(datas);
        }

    }
}
