package com.tlg.storehelper.activity.calculator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.Calculator;
import com.nec.lib.android.utils.ConstraintUtil;
import com.tlg.storehelper.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CalculatorActivity extends BaseRxAppCompatActivity {

    private ImageView mIvBackground;
    private RecyclerView mRecyclerView1;
    private RecyclerView mRecyclerView2;
    private List<CalculatorVo> mDatas = new ArrayList<>();

    private FlexboxLayoutManager mLayoutManager1;
    private LinearLayoutManager mLayoutManager2;

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
        mRecyclerView1.getAdapter().notifyDataSetChanged();
        mRecyclerView2.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculator_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_calculator:
                showCalculator();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView() {
        // find view
        mRecyclerView1 = (RecyclerView) findViewById(R.id.recyclerView1);
        mRecyclerView2 = (RecyclerView) findViewById(R.id.recyclerView2);
        mIvBackground = findViewById(R.id.ivBackground);

        // initialize controls
        hideKeyboard(true);

        //设置背景图平铺
        //BitmapDrawable drawable = (BitmapDrawable) ImageUtil.getDrawableFromResources(this, R.drawable.frosted_glass);
        //drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT );
        //drawable.setDither(true);
        //mRecyclerView.setBackground(drawable);

        //设置RecycleView的布局方式，FlexboxLayoutManager
        mLayoutManager1 = new FlexboxLayoutManager(_this);
        mLayoutManager1.setFlexWrap(FlexWrap.WRAP); //设置是否换行
        mLayoutManager1.setFlexDirection(FlexDirection.ROW); // 设置主轴排列方式
        mLayoutManager1.setAlignItems(AlignItems.STRETCH);
        mLayoutManager1.setJustifyContent(JustifyContent.FLEX_START);
        mRecyclerView1.setLayoutManager(mLayoutManager1);
        //设置RecycleView的布局方式，线性布局，默认垂直
        mLayoutManager2 = new LinearLayoutManager(this);
        mRecyclerView2.setLayoutManager(mLayoutManager2);

        RecyclerViewAdapter1 recyclerViewAdapter1 = new RecyclerViewAdapter1(CalculatorActivity.this, mDatas);
        mRecyclerView1.setAdapter(recyclerViewAdapter1);
        RecyclerViewAdapter2 recyclerViewAdapter2 = new RecyclerViewAdapter2(CalculatorActivity.this, mDatas);
        mRecyclerView2.setAdapter(recyclerViewAdapter2);
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_calculator;
    }

    ///自定义类继承RecycleView.Adapter类作为数据适配器
    class RecyclerViewAdapter1 extends RecyclerView.Adapter {

        private Context mContext;
        private List<CalculatorVo> mDatas;

        public RecyclerViewAdapter1(Context context, List<CalculatorVo> datas) {
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
            recyclerViewHolder.tvExpression.setText(mDatas.get(position).expression);
            if(! mDatas.get(position).calculationLine) {
                recyclerViewHolder.tvExpression.setBackgroundResource(R.drawable.edittext_bordered_white_bg);
                recyclerViewHolder.tvExpression.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
            } else {
                recyclerViewHolder.tvExpression.setBackgroundResource(R.drawable.edittext_bordered_color_bg);
                recyclerViewHolder.tvExpression.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }

            //recyclerViewHolder.ivIcon.setTag(String.valueOf(position));
            if (position == mDatas.size() - 1)
                recyclerViewHolder.ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.delete22));
            else
                recyclerViewHolder.ivIcon.setImageDrawable(null);

            //定义Flexbox布局的元素特征
            ViewGroup.LayoutParams lp = recyclerViewHolder.layoutRecylerviewItem.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
                flexboxLp.setFlexGrow(0.0f);
                flexboxLp.setAlignSelf(AlignItems.FLEX_END);
            }
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_calculator_recyclerview, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);
            return recyclerViewHolder;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView tvExpression;
            ImageView ivIcon;
            ConstraintLayout layoutRecylerviewItem;

            public RecyclerViewHolder(View view) {
                super(view);
                //实例化自定义对象
                tvExpression = view.findViewById(R.id.tvExpression);
                ivIcon = view.findViewById(R.id.ivIcon);
                layoutRecylerviewItem = view.findViewById(R.id.layoutRecylerviewItem);
                ivIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnRemoveClick(view);
                    }
                });
            }
        }
    }

    class RecyclerViewAdapter2 extends RecyclerView.Adapter {

        private Context mContext;
        private List<CalculatorVo> mDatas;

        public RecyclerViewAdapter2(Context context, List<CalculatorVo> datas) {
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
            recyclerViewHolder.tvExpression.setText(mDatas.get(position).expression);
            if(! mDatas.get(position).calculationLine) {
                recyclerViewHolder.tvExpression.setBackgroundResource(R.color.white);
                recyclerViewHolder.tvExpression.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
            } else {
                recyclerViewHolder.tvExpression.setBackgroundResource(R.color.lightyellow);
                recyclerViewHolder.tvExpression.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
            recyclerViewHolder.vDivider.setVisibility(mDatas.get(position).calculationLine?View.VISIBLE:View.INVISIBLE);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_calculator_list_recyclerview, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);
            return recyclerViewHolder;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView tvExpression;
            View vDivider;
            ConstraintLayout layoutRecylerviewItem;

            public RecyclerViewHolder(View view) {
                super(view);
                //实例化自定义对象
                tvExpression = view.findViewById(R.id.tvExpression);
                vDivider = view.findViewById(R.id.vDivider);
                layoutRecylerviewItem = view.findViewById(R.id.layoutRecylerviewItem);
            }
        }
    }

    private void billClick() {
        for(View view: AndroidUtil.getAllViews(this, true)) {
            if (view instanceof Button)
                view.setVisibility(View.GONE);
        }
        mToolbar.setVisibility(View.VISIBLE);
        mRecyclerView1.setVisibility(View.GONE);
        mRecyclerView2.setVisibility(View.VISIBLE);

        ConstraintLayout constraintLayout = findViewById(R.id.layoutRecylerview);
        ConstraintUtil constraintUtil = new ConstraintUtil(constraintLayout);
        ConstraintUtil.ConstraintBegin begin = constraintUtil.beginWithAnim();

        begin.clear(R.id.ivBackground, ConstraintSet.BOTTOM);
        begin.Bottom_toBottomOf(R.id.ivBackground, ConstraintLayout.LayoutParams.PARENT_ID);
        begin.commit();

        mRecyclerView1.getAdapter().notifyDataSetChanged();
        mRecyclerView2.getAdapter().notifyDataSetChanged();
    }

    private void showCalculator() {
        for(View view: AndroidUtil.getAllViews(this, true)) {
            if (view instanceof Button)
                view.setVisibility(View.VISIBLE);
        }
        mToolbar.setVisibility(View.GONE);
        mRecyclerView1.setVisibility(View.VISIBLE);
        mRecyclerView2.setVisibility(View.GONE);

        ConstraintLayout constraintLayout = findViewById(R.id.layoutRecylerview);
        ConstraintUtil constraintUtil = new ConstraintUtil(constraintLayout);
        ConstraintUtil.ConstraintBegin begin = constraintUtil.beginWithAnim();

        begin.clear(R.id.ivBackground, ConstraintSet.BOTTOM);
        begin.Bottom_toTopOf(R.id.ivBackground, R.id.btnAC);
        begin.setMarginBottom(R.id.ivBackground, 4);
        begin.commit();

        mRecyclerView1.getAdapter().notifyDataSetChanged();
        mRecyclerView2.getAdapter().notifyDataSetChanged();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////    Calculation  ///////////////////////////////////////

    public void btnRemoveClick(View view) {
        ////没有记录
        if (mDatas.size() > 0) {
            mDatas.remove(mDatas.size() - 1);
            mRecyclerView1.getAdapter().notifyDataSetChanged();
            mRecyclerView2.getAdapter().notifyDataSetChanged();
        }
    }

    public void btnClick(View view) {
        String tagStr = view.getTag().toString();
        boolean needReload = false;
        if(tagStr.equals("AC")) {
            needReload = acClick();
        }
        else if(tagStr.equals("DEL")) {
            needReload = delClick();
        }
        else if(tagStr.equals("dis")) {
            needReload = discountClick();
        }
        else if(tagStr.equals("bill")) {
            billClick();
        }
        else if(tagStr.equals("equal")) {
            needReload = equalClick();
        }
        else if(tagStr.equals("plus")) {
            needReload = operatorClick("＋");
        }
        else if(tagStr.equals("minus")) {
            needReload = operatorClick("—");
        }
        else if(tagStr.equals("multiply")) {
            needReload = operatorClick("×");
        }
        else if(tagStr.equals("divide")) {
            needReload = operatorClick("÷");
        }
        else {
            needReload = numberClick(tagStr);
        }
        if (needReload) {
            mRecyclerView1.getAdapter().notifyDataSetChanged();
            mRecyclerView2.getAdapter().notifyDataSetChanged();
            //scrollToBottom();
        }
    }

//    private void scrollToBottom() {
//        mRecyclerView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(mRecyclerView.canScrollVertically(-1))
//                    mRecyclerView.smoothScrollToPosition(mDatas.size() - 1);
//            }
//        }, 1000);
//    }

    private boolean acClick() {    //复位
        ////没有记录
        if (mDatas.size() == 0) {
            return false;
        } else {
            mDatas.clear();
            return true;
        }
    }

    private boolean delClick() {    //退格/删除
        final String spaces = " ";
        final int lenOfOperatorWithSpace = 3;
        ////没有记录
        if (mDatas.size() == 0) {
            return false;
        }
        ////已有记录
        if(mDatas.get(mDatas.size() - 1).calculationLine) {     //末行是计算行
            return false;
        }
        String str = mDatas.get(mDatas.size()-1).expression;
        String trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        boolean endWithOperator = trimStr.endsWith("＋") || trimStr.endsWith("—") || trimStr.endsWith("×") || trimStr.endsWith("÷");
        if(trimStr.isEmpty())
            return false;
        int _len = mDatas.get(mDatas.size() - 1).expression.length();
        int len2Remove = 1;
        if(str.length() >= lenOfOperatorWithSpace && endWithOperator && str.endsWith(spaces)) {    //末行以操作符结尾
            len2Remove = lenOfOperatorWithSpace;
        }
        mDatas.set(mDatas.size() - 1, new CalculatorVo(mDatas.get(mDatas.size() - 1).expression.substring(0, _len - len2Remove), false));
        return true;
    }

    private boolean discountClick() {    //折扣
        final String spaces = " ";
        final int lenOfOperatorWithSpace = 3;
        final String operatorWithSpace = spaces + "×" + spaces + "0.9";
        ////没有记录
        if (mDatas.size() == 0) {
            mDatas.add(new CalculatorVo("", false));
            return true;
        }
        ////已有记录
        String trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        if(trimStr.endsWith(".")) {  //去除结尾的小数点
            int _len = mDatas.get(mDatas.size() - 1).expression.length();
            mDatas.set(mDatas.size() - 1, new CalculatorVo(mDatas.get(mDatas.size() - 1).expression.substring(0, _len - 1), false));
            trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        }
        boolean onlyOperator = trimStr.equals("＋") || trimStr.equals("—") || trimStr.equals("×") || trimStr.equals("÷");
        boolean endWithOperator = trimStr.endsWith("＋") || trimStr.endsWith("—") || trimStr.endsWith("×") || trimStr.endsWith("÷");

        if(mDatas.size()==1 && (onlyOperator || trimStr.isEmpty())) {   //只有一行，为空或单运算符
            if (trimStr.isEmpty())
                return false;
            else {
                mDatas.set(0, new CalculatorVo("", false));
                return true;
            }
        } else if(mDatas.size()>1 && (onlyOperator || trimStr.isEmpty())) {   //非首行，只有操作符，则更新操作符
            mDatas.set(mDatas.size()-1, new CalculatorVo(operatorWithSpace, false));
            return true;
        } else {                    //（首行或非首行，）有数字
            if(! mDatas.get(mDatas.size()-1).calculationLine) {
                String _str = mDatas.get(mDatas.size()-1).expression;
                if(endWithOperator) {   //以操作符结尾，则更新操作符
                    _str = _str.substring(0, _str.length() - lenOfOperatorWithSpace);
                }
                mDatas.set(mDatas.size() - 1, new CalculatorVo(_str + operatorWithSpace, false));
                return true;
            } else {    //是计算行
                mDatas.add(new CalculatorVo(operatorWithSpace, false));
                return true;
            }
        }
    }

    private boolean operatorClick(String operator) {    //加减乘除
        final String spaces = " ";
        final int lenOfOperatorWithSpace = 3;
        ////没有记录
        if (mDatas.size() == 0) {
            mDatas.add(new CalculatorVo("", false));
            return true;
        }
        ////已有记录
        String trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        if(trimStr.endsWith(".")) {  //去除结尾的小数点
            int _len = mDatas.get(mDatas.size() - 1).expression.length();
            mDatas.set(mDatas.size() - 1, new CalculatorVo(mDatas.get(mDatas.size() - 1).expression.substring(0, _len - 1), false));
            trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        }
        boolean onlyOperator = trimStr.equals("＋") || trimStr.equals("—") || trimStr.equals("×") || trimStr.equals("÷");
        boolean endWithOperator = trimStr.endsWith("＋") || trimStr.endsWith("—") || trimStr.endsWith("×") || trimStr.endsWith("÷");

        if(mDatas.size()==1 && (onlyOperator || trimStr.isEmpty())) {   //只有一行，为空或单运算符
            if (trimStr.isEmpty())
                return false;
            else {
                mDatas.set(0, new CalculatorVo("", false));
                return true;
            }
        } else if(mDatas.size()>1 && (onlyOperator || trimStr.isEmpty())) {   //非首行，只有操作符，则更新操作符
            mDatas.set(mDatas.size()-1, new CalculatorVo(spaces + operator + spaces, false));
            return true;
        } else {                    //（首行或非首行，）有数字
            if(! mDatas.get(mDatas.size()-1).calculationLine) {
                String _str = mDatas.get(mDatas.size()-1).expression;
                //若允许删除中间的行，保证下一行不能以符号开头//
                if(endWithOperator) {   //以操作符结尾，则更新操作符
                    _str = _str.substring(0, _str.length() - lenOfOperatorWithSpace);
                }
                mDatas.set(mDatas.size() - 1, new CalculatorVo(_str + spaces + operator + spaces, false));
                return true;
            } else {    //是计算行
                mDatas.add(new CalculatorVo(spaces + operator + spaces, false));
                return true;
            }
        }
    }

    private boolean equalClick() {    //等号
        final String spaces = " ";
        final int lenOfOperatorWithSpace = 3;
        ////没有记录
        if (mDatas.size() == 0) {
            return false;
        }
        ////已有记录
        if(mDatas.get(mDatas.size()-1).calculationLine) {   //最后一行是计算行
            return false;
        }
        String trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        if(trimStr.endsWith(".")) {  //去除结尾的小数点
            int _len = mDatas.get(mDatas.size() - 1).expression.length();
            mDatas.set(mDatas.size() - 1, new CalculatorVo(mDatas.get(mDatas.size() - 1).expression.substring(0, _len - 1), false));
            trimStr = mDatas.get(mDatas.size()-1).expression.trim();
        }

        boolean onlyOperator = trimStr.equals("＋") || trimStr.equals("—") || trimStr.equals("×") || trimStr.equals("÷");
        boolean endWithOperator = trimStr.endsWith("＋") || trimStr.endsWith("—") || trimStr.endsWith("×") || trimStr.endsWith("÷");

        if(endWithOperator) {   //以操作符结尾，则去除多余的结尾操作符
            String _str = mDatas.get(mDatas.size()-1).expression;
            _str = _str.substring(0, _str.length() - lenOfOperatorWithSpace);
            mDatas.set(mDatas.size()-1, new CalculatorVo(_str, false));
        }

        boolean needCalc = false;
        if(onlyOperator || trimStr.isEmpty()) {     //最后一行为空或单运算符
            mDatas.remove(mDatas.size() - 1);
            if(mDatas.size() == 0 || mDatas.get(mDatas.size()-1).calculationLine)
                return true;
            else
                needCalc = true;
        } else                                      //最后一行为有数字的行
            needCalc = true;
        if(needCalc) {
            mDatas.add(new CalculatorVo(spaces + "＝" + spaces+calc(), true));
            return true;
        } else
            return false;
    }

    private String calc() {
        final int lenOfOperatorWithSpace = 3;
        if(mDatas.size() == 0)
            return "0";
        int idx = 0;
        for(int i=mDatas.size()-1; i>=0; i--) {
            if (mDatas.get(i).calculationLine) {
                idx = i;
                break;
            }
        }
        //若删除中间的行，保证下一行不能以符号开头//
        StringBuilder formulaAll = new StringBuilder();
        for(int i=idx; i<mDatas.size(); i++) {
            String expression0 = mDatas.get(i).expression;
            String expression1 = mDatas.get(i).calculationLine ? expression0.substring(lenOfOperatorWithSpace) : expression0;
            formulaAll.append(expression1);
        }
        String expression = formulaAll.toString().replaceAll("＋","+")
                .replaceAll("—","-")
                .replaceAll("×","*")
                .replaceAll("÷","/")
                .replaceAll(" ","");
        double result = Calculator.conversion(expression);
        return new DecimalFormat("#.0").format(result);
    }

    private boolean numberClick(String num) {    //数字,小数点
        final String spaces = " ";
        final int lenOfOperatorWithSpace = 3;
        ////没有记录
        if (mDatas.size() == 0) {
            mDatas.add(new CalculatorVo(num, false));
            return true;
        }
        ////已有记录
        //最后一行是计算行
        if(mDatas.get(mDatas.size()-1).calculationLine) {
            mDatas.add(new CalculatorVo(spaces + "＋" + spaces + num, false));
            return true;
        }
        //最后一行是有数字的非计算行
        String str = mDatas.get(mDatas.size()-1).expression;
        if (num.equals(".")) {
            int idx = str.lastIndexOf(" ");
            idx = idx < 0 ? 0 : idx+1;
            boolean hasDotAlready = str.indexOf(".", idx) >= 0;
            if (hasDotAlready)  //忽略多余小数点
                return false;
        }
        if(num.equals("0")) {
            int idx = str.lastIndexOf(" ");
            idx = idx < 0 ? 0 : idx+1;
            if(str.substring(idx).equals("0"))  //忽略多余0(首位0重复)
                return false;
        }
        mDatas.set(mDatas.size()-1, new CalculatorVo(str+num, false));
        return true;
    }

    class CalculatorVo {
        public String expression;
        public boolean calculationLine;

        public CalculatorVo(String expression, boolean calculationLine) {
            this.expression = expression;
            this.calculationLine = calculationLine;
        }
    }
}
