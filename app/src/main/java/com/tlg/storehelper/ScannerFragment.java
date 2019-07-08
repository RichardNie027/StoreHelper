package com.tlg.storehelper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.application.MyApplication;
import com.nec.boost.CustomDialog;
import com.nec.utils.ResUtil;
import com.nec.utils.UiUtil;
import com.tlg.storehelper.base.BaseFragment;
import com.tlg.storehelper.vo.StatisticInfo;

public class ScannerFragment extends BaseFragment {

    private static final String ARG1_NAME = "list_id";
    private static final String ARG2_NAME = "list_no";
    private static final String ARG3_NAME = "quantity";
    private static final String ARG4_NAME = "total_quantity";
    private static final String ARG5_NAME = "last_barcode";
    private static final String ARG6_NAME = "last_bin_coding";

    private OnFragmentInteractionListener mListener;

    private TextView mTvListNo;
    private Spinner mSpnBinType;
    private TextView mTvConnector;
    private Spinner mSpnBinCoding;
    private EditText mEtBinCoding;
    private TextView mTvQuantity;
    private TextView mTvTotalQuantity;
    private EditText mEtBarcode;
    private TextView mTvBatchScanQuantity;
    private Button mBtnBatchScan;
    private TextView mTvLastBarcode;
    private TextView mTvLastBinCoding;

    private String[] mBinTypes = null;
    private String mBinType = "";       //货位类型
    private String[] mBinCodings = null;
    private String mBinCoding = "";     //货位序号
    private String mFullBinCoding = ""; //货位类型+货位序号，或者自定义货位名称

    private int mBatchScanQuantity = 1; //批量扫描的件数
    private StatisticInfo mStatisticInfo = new StatisticInfo();  //盘点单统计信息

    public ScannerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScannerFragment.
     */
    public static ScannerFragment newInstance(StatisticInfo statisticInfo) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle args = new Bundle();
        args.putLong(ARG1_NAME, statisticInfo.id);
        args.putString(ARG2_NAME, statisticInfo.listNo);
        args.putInt(ARG3_NAME, statisticInfo.quantity);
        args.putInt(ARG4_NAME, statisticInfo.totalQuantity);
        args.putString(ARG5_NAME, statisticInfo.lastBarcode);
        args.putString(ARG6_NAME, statisticInfo.lastBinCoding);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatisticInfo.id = getArguments().getLong(ARG1_NAME);
            mStatisticInfo.listNo = getArguments().getString(ARG2_NAME);
            mStatisticInfo.quantity = getArguments().getInt(ARG3_NAME);
            mStatisticInfo.totalQuantity = getArguments().getInt(ARG4_NAME);
            mStatisticInfo.lastBarcode = getArguments().getString(ARG5_NAME);
            mStatisticInfo.lastBinCoding = getArguments().getString(ARG6_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scanner, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        // find view
        mTvListNo = rootView.findViewById(R.id.tvListNo);
        mSpnBinType = rootView.findViewById(R.id.spnBinType);
        mTvConnector = rootView.findViewById(R.id.tvConnector);
        mSpnBinCoding = rootView.findViewById(R.id.spnBinCoding);
        mEtBinCoding = rootView.findViewById(R.id.etBinCoding);
        mTvQuantity = rootView.findViewById(R.id.tvQuantity);
        mTvTotalQuantity = rootView.findViewById(R.id.tvTotalQuantity);
        mEtBarcode = rootView.findViewById(R.id.etBarcode);
        mTvBatchScanQuantity = rootView.findViewById(R.id.tvBatchScanQuantity);
        mBtnBatchScan = rootView.findViewById(R.id.btnBatchScan);
        mTvLastBarcode = rootView.findViewById(R.id.tvLastBarcode);
        mTvLastBinCoding = rootView.findViewById(R.id.tvLastBinCoding);

        // initial controls
        //((BaseAppCompatActivity)this.getActivity()).setOnFocusChangeListener(mEtBarcode, mEtBinCoding);
        //mBtnBatchScan.setFocusable(false);

        mBinTypes = new String[]{"外杆", "内杆", "外格", "内格", "其它", "自定义"};
        ArrayAdapter<String> spinner1Adapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.spinner_item_select, mBinTypes);
        spinner1Adapter.setDropDownViewResource(R.layout.spinner_item_drop);
        Spinner spinner1 = (Spinner) rootView.findViewById(R.id.spnBinType);
        spinner1.setAdapter(spinner1Adapter);
        spinner1.setOnItemSelectedListener(new BinTypeOnItemSelectedListener());
        mBinCodings = new String[30];
        for(int i = 0; i < 30; i++)
            mBinCodings[i] = String.valueOf(i+1);
        ArrayAdapter<String> spinner2Adapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.spinner_item_select, mBinCodings);
        spinner2Adapter.setDropDownViewResource(R.layout.spinner_item_drop);
        Spinner spinner2 = (Spinner) rootView.findViewById(R.id.spnBinCoding);
        spinner2.setAdapter(spinner2Adapter);
        spinner2.setOnItemSelectedListener(new BinCodingOnItemSelectedListener());

        rootView.findViewById(R.id.btnBatchScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputBatchNumber();
            }
        });

        ///设置“货位”控件
        //回车键响应
        mEtBinCoding.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    mEtBarcode.requestFocus();
                    _this.hideKeyboard(view);
                    return true;
                }
                return false;
            }
        });
        //获得焦点全选条形码
        mEtBinCoding.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus)
                    mEtBinCoding.selectAll();
                else {
                    String _mFullBinCoding = mEtBinCoding.getText().toString();
                    validateBinCodingChanged(_mFullBinCoding);
                }
                _this.hideKeyboard(view);
            }
        });

        ///设置“条形码”控件
        //回车键响应
        mEtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    onScanBarcodeAsNewRecord(mEtBarcode.getText().toString(), mFullBinCoding, mBatchScanQuantity);
                    mEtBarcode.requestFocus();
                    _this.hideKeyboard(view);
                    return true;
                }
                return false;
            }
        });
        //获得焦点全选货位
        mEtBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    mEtBarcode.selectAll();
                }
                _this.hideKeyboard(view);
            }
        });
        //Touch清空条形码
        mEtBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mEtBarcode.setText("");
                _this.hideKeyboard(view);
                return false;
            }
        });
    }

    /**如果货位改变，重新统计，并更新显示*/
    private void validateBinCodingChanged(String _mFullBinCoding) {
        if(!_mFullBinCoding.equals(mFullBinCoding)) {   //货位改变
            mFullBinCoding = _mFullBinCoding;
            recalculateQuantity();
            Log.d(this.getClass().getName(), "货位改变" + _mFullBinCoding);
        }
    }

    /**根据统计信息，更新显示
     * @param statisticInfo 按形参更新，NULL则取类属性mStatisticInfo(通过接口交互已经有值)
     */
    public void updateStatisticDisplay(StatisticInfo statisticInfo) {
        if(statisticInfo == null)
            statisticInfo = mStatisticInfo;
        mTvListNo.setText(statisticInfo.listNo);
        mTvQuantity.setText(Integer.toString(statisticInfo.quantity));
        mTvTotalQuantity.setText(Integer.toString(statisticInfo.totalQuantity));
        mTvLastBarcode.setText(statisticInfo.lastBarcode);
        mTvLastBinCoding.setText(statisticInfo.lastBinCoding);
    }

    /**重新统计，并更新显示*/
    private void recalculateQuantity() {
        if(mListener != null)
            mStatisticInfo = mListener.onInventoryRecalculate(mFullBinCoding);
        if(!mStatisticInfo.lastBinCoding.equals("")) {

        }
        updateStatisticDisplay(mStatisticInfo);
    }

    private class BinTypeOnItemSelectedListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            String item  = adapterView.getItemAtPosition(position).toString();
            mBinType = mBinTypes[position];
            if(position == 5) {
                mEtBinCoding.setVisibility(View.VISIBLE);
                mSpnBinCoding.setVisibility(View.INVISIBLE);
                mTvConnector.setVisibility(View.INVISIBLE);
                mEtBinCoding.requestFocus();
                String _mFullBinCoding = mEtBinCoding.getText().toString();
                validateBinCodingChanged(_mFullBinCoding);
            } else {
                mEtBinCoding.setVisibility(View.INVISIBLE);
                mSpnBinCoding.setVisibility(View.VISIBLE);
                mTvConnector.setVisibility(View.VISIBLE);
                mEtBarcode.requestFocus();
                String _mFullBinCoding = mBinType + "-" + mBinCoding;
                if(!mBinCoding.equals(""))
                    validateBinCodingChanged(_mFullBinCoding);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class BinCodingOnItemSelectedListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            String item  = adapterView.getItemAtPosition(position).toString();
            mBinCoding = mBinCodings[position];
            String _mFullBinCoding = mBinType + "-" + mBinCoding;
            if(!mBinType.equals(""))
                validateBinCodingChanged(_mFullBinCoding);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void onScanBarcodeAsNewRecord(String barcode, String binCoding, int num) {
        if(mEtBinCoding.getVisibility()==View.VISIBLE && mEtBinCoding.getText().toString().trim().length() == 0) {
            Toast.makeText(MyApplication.getInstance(), "货位不能为空", Toast.LENGTH_SHORT).show();
            mEtBinCoding.requestFocus();
            return;
        }
        if(barcode.length() > 1) {  //模拟正确
            mEtBarcode.setText("");
        } else {                    //模拟错误
            mEtBarcode.selectAll();
            return;
        }
        if (mListener != null) {
            StatisticInfo _statisticInfo = mListener.onInventoryNewRecord(binCoding, barcode, num);
            if(_statisticInfo == null) {
                //扫码增加记录失败
                return;
            }
            mStatisticInfo = _statisticInfo;
            //更新界面显示
            mTvLastBinCoding.setText(mStatisticInfo.lastBinCoding);
            mTvLastBarcode.setText(mStatisticInfo.lastBarcode);
            mTvQuantity.setText(Integer.toString(mStatisticInfo.quantity));
            mTvTotalQuantity.setText(Integer.toString(mStatisticInfo.totalQuantity));
            mBatchScanQuantity = 1;
            mTvBatchScanQuantity.setText("1");
        }
    }


    private void inputBatchNumber() {
        CustomDialog dialog = new CustomDialog(MyApplication.getInstance(), R.layout.dialog_fragment_scanner_quantity, R.style.Custom_Dialog, new int[] {R.id.btnDialogOk});
        dialog.drawable = ResUtil.getDrawable(ResUtil.getColor("snow"), ResUtil.getColor("colorPrimaryLight"), UiUtil.dp2px(0.8f), UiUtil.dp2px(15f));
        //dialog.modal = true;
        dialog.setOnDialogItemClickListener(new CustomDialog.OnDialogItemClickListener() {
            @Override
            public void OnDialogItemClick(CustomDialog dialog, View view) {
                switch (view.getId()){
                    case R.id.btnDialogOk:
                        EditText etQuantity = view.getRootView().findViewById(R.id.etQuantity);
                        int quantity = etQuantity==null ? 1 : Integer.parseInt(etQuantity.getText().toString());
                        mBatchScanQuantity = quantity;
                        mTvBatchScanQuantity.setText(String.valueOf(mBatchScanQuantity));
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * 扫描条形码触发，保存新记录，返回盘点单统计信息
         * @param binCoding
         * @param barcode
         * @param num
         */
        StatisticInfo onInventoryNewRecord(String binCoding, String barcode, int num);

        /**
         * 重新统计盘点单信息
         * @param specailBinCoding
         */
        StatisticInfo onInventoryRecalculate(String specailBinCoding);
    }
}
