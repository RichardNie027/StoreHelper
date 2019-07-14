package com.tlg.storehelper.activity.common;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nec.lib.application.MyApplication;
import com.nec.lib.grantor.PermissionListener;
import com.nec.lib.grantor.PermissionsUtil;
import com.nec.lib.base.BaseRxAppCompatActivity;
import com.nec.lib.httprequest.use.BaseObserver;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.nec.lib.utils.SQLiteUtil;
import com.tlg.storehelper.dao.GoodsBarcode;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.utils.UiUtil;
import com.tlg.storehelper.httprequest.net.api.RegentService;
import com.tlg.storehelper.httprequest.net.entity.GoodsBarcodeEntity;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseRxAppCompatActivity {

    private EditText mEditTextName;
    private EditText mEditTextPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true; //super前
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAccessNetwork();
        requestStorage();
        initView();
    }

    private void requestAccessNetwork() {
        PermissionsUtil.requestPermission(getApplication(), new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                GlobalVars.permissionOfNetwork = true;
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                Toast.makeText(MyApplication.getInstance(), "用户拒绝了访问网络", Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE);
    }

    private void requestStorage() {
        PermissionsUtil.requestPermission(getApplication(), new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                GlobalVars.permissionOfStorage = true;
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                Toast.makeText(MyApplication.getInstance(), "用户拒绝了读写文件", Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initView() {
        // find view
        mEditTextName = findViewById(R.id.etUsername);
        mEditTextPwd = findViewById(R.id.etPwd);

        // initialize controls
        setOnFocusChangeListener(mEditTextName, mEditTextPwd);
        //setHideInputViews(mEditTextName, mEditTextPwd);

        // 初始化系统的屏幕尺寸信息
        UiUtil.getAndroiodScreenProperty(this);

        //load data
        httpRequest();
        loadData();
    }

    private void httpRequest() {
        RegentService.getInstance()
                .getGoodsBarcodes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(new BaseObserver<GoodsBarcodeEntity>(this, true) {

                    @Override
                    public void onSuccess(GoodsBarcodeEntity response) {
                        Log.d(MainActivity.class.getName(), "请求成功");
                        Toast.makeText(MainActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onRequestStart() {
                        super.onRequestStart();
                        Log.d(MainActivity.class.getName(), "请求开始");
                    }

                    @Override
                    protected void onRequestEnd() {
                        super.onRequestEnd();
                        Log.d(MainActivity.class.getName(), "请求结束");
                    }
                });
    }

    private List<GoodsBarcode> analysisGoodsBarcodeList() {
        String jsonData = "["
                +"{\"id\":\"77F622D5F7504FBCA7C45DB924F4A09B\",\"goods_id\":\"46C14AD90D5A4FECBC595611FFDF13D0\",\"size_id\":\"9B9EB710F4374C4680F7002E25D735BE\",\"size_desc\":\"\",\"barcode\":\"DB5-12126-1106\"}"+","
                +"{\"id\":\"CE44C4B57D954262B8EC52ADF8652241\",\"goods_id\":\"46C14AD90D5A4FECBC595611FFDF13D0\",\"size_id\":\"BAEFEA6937B94F61A5C5465C89A52493\",\"size_desc\":\"\",\"barcode\":\"DB5-12126-1107\"}"+","
                +"{\"id\":\"43B1AE7FC32545E48A1C61252D6D7B0F\",\"goods_id\":\"46C14AD90D5A4FECBC595611FFDF13D0\",\"size_id\":\"48E079266721466CB5D50DF9E08DD289\",\"size_desc\":\"\",\"barcode\":\"DB5-12126-1108\"}"+","
                +"{\"id\":\"E2A54F4A6CDE45FDBDAEC4B600C3B357\",\"goods_id\":\"F66CC4CEE9354D67BB88849A49E06522\",\"size_id\":\"EEFBCF0C1C9C44D1A47FEBE528DBBEC7\",\"size_desc\":\"\",\"barcode\":\"TL1E401WOP001A1044\"}"+","
                +"{\"id\":\"FC475A7E28C040159340B5D961BE1D99\",\"goods_id\":\"F66CC4CEE9354D67BB88849A49E06522\",\"size_id\":\"4DCDB679B1414219AAC6EEE0BEDBCF3E\",\"size_desc\":\"\",\"barcode\":\"TL1E401WOP001A1055\"}"+","
                +"{\"id\":\"E9D1DA30066A45FA83CF2D1D4A94F789\",\"goods_id\":\"F66CC4CEE9354D67BB88849A49E06522\",\"size_id\":\"69C3177A249447BD97A1E5A751E9F58E\",\"size_desc\":\"\",\"barcode\":\"TL1E401WOP001A1066\"}"+"]";
        Type listType = new TypeToken<LinkedList<GoodsBarcode>>(){}.getType();
        Gson gson = new Gson();
        LinkedList<GoodsBarcode> goodsBarcodeList = gson.fromJson(jsonData, listType);
        return goodsBarcodeList;
    }

    private boolean loadData() {
        boolean result = false;
        List<GoodsBarcode> goodsBarcodeList = analysisGoodsBarcodeList();

        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            db.delete(SQLiteDbHelper.TABLE_GOODS_BARCODE, null, null);
            for (Iterator iterator = goodsBarcodeList.iterator(); iterator.hasNext(); ) {
                GoodsBarcode goodsBarcode = (GoodsBarcode) iterator.next();
                ContentValues contentValues = SQLiteUtil.toContentValues(goodsBarcode);
                db.insert(SQLiteDbHelper.TABLE_GOODS_BARCODE, null, contentValues);
            }
            result = true;
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            Toast.makeText(MyApplication.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            db.close();
        }
        return result;
    }

    public void btnLoginClick(View v) {
        String username = mEditTextName.getText().toString();
        if(username.equals("")) {
            Toast.makeText(MyApplication.getInstance(), "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        GlobalVars.username = username;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.putExtra("username", );
        startActivity(intent);
    }
}
