package com.tlg.storehelper.activity.common;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nec.lib.grantor.PermissionListener;
import com.nec.lib.grantor.PermissionsUtil;
import com.nec.lib.base.BaseRxAppCompatActivity;
import com.nec.lib.httprequest.net.dialog.CustomProgressDialogUtils;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.utils.UiUtil;
import com.tlg.storehelper.httprequest.net.AppBaseObserver;
import com.tlg.storehelper.httprequest.net.api.RegentService;
import com.tlg.storehelper.httprequest.net.entity.SimpleListEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseRxAppCompatActivity {

    private EditText mEditTextName;
    private EditText mEditTextPwd;
    private Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true; //super前
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void requestAccessNetworkAndStorage() {
        PermissionsUtil.requestPermission(getApplication(), new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                GlobalVars.permissionOfNetworkAndStroage = true;
                afterGrantPermission();
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                Toast.makeText(MyApp.getInstance(), "访问网络和读写文件被拒绝", Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initView() {
        // find view
        mEditTextName = findViewById(R.id.etUsername);
        mEditTextPwd = findViewById(R.id.etPwd);
        mBtnLogin = findViewById(R.id.btnLogin);

        // initialize controls
        setOnFocusChangeListener(mEditTextName, mEditTextPwd);
        //setHideInputViews(mEditTextName, mEditTextPwd);

        // 初始化系统的屏幕尺寸信息
        UiUtil.getAndroiodScreenProperty(this);

        //load data
        beforeGrantPermission();
        requestAccessNetworkAndStorage();
    }

    private void beforeGrantPermission() {
        mBtnLogin.setVisibility(View.GONE);
    }

    private void afterGrantPermission() {
        if(GlobalVars.permissionOfNetworkAndStroage)
            httpRequestToken();
    }

    private void httpRequestToken() {
        RegentService.getInstance()
                .getToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleMapEntity>(this, true) {

                    @Override
                    public void onSuccess(SimpleMapEntity response) {
                        Log.d(MainActivity.class.getName(), "请求成功");
                        GlobalVars.token = response.result.get("token").toString();
                        mBtnLogin.setVisibility(View.VISIBLE);
                        httpRequestGoodBarcodes();
                    }
                });
    }

    private void httpRequestGoodBarcodes() {
        RegentService.getInstance()
                .getGoodsBarcodes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListEntity<String>>(this, true,"正在同步商品资料") {

                    @Override
                    public void onSuccess(SimpleListEntity<String> response) {
                        Log.d(MainActivity.class.getName(), "请求成功");

                        mEditTextName.getRootView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                saveGoodsBarcodes(response.result);
                            }
                        }, 200);
                    }
                });
    }

    private void httpRequestLogin(String username, String password) {
        RegentService.getInstance()
                .loginValidation(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(new AppBaseObserver<SimpleListEntity<String>>(this, true, "登录验证中") {

                    @Override
                    public void onSuccess(SimpleListEntity<String> response) {
                        Log.d(MainActivity.class.getName(), "请求成功");

                        GlobalVars.username = username;
                        Intent intent = new Intent(_this, HomeActivity.class);
                        String[] array = response.result.toArray(new String[response.result.size()]);
                        intent.putExtra("storeCodes", array);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    private void saveGoodsBarcodes(List<String> goodsBarcodeList) {

        //在新线程保存数据
        new AsyncTask<List<String>, Integer, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                CustomProgressDialogUtils.getInstance().showProgress(MyApp.getInstance(), "正在保存商品资料");
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                CustomProgressDialogUtils.getInstance().dismissProgress();
                if(aBoolean)
                    Toast.makeText(MyApp.getInstance(), "商品资料同步完成", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyApp.getInstance(), "商品资料保存失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Boolean doInBackground(List<String>... lists) {
                boolean result = false;
                SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
                SQLiteDatabase db = null;
                try {
                    db = helper.getWritableDatabase();
                    db.beginTransaction();
                    db.delete(SQLiteDbHelper.TABLE_GOODS_BARCODE, null, null);
                    for (String goodsBarcode : goodsBarcodeList) {
                        String sql = new StringBuffer().append("insert into ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE).append(" values(?)").toString();
                        db.execSQL(sql, new Object[] {goodsBarcode});
                    }
                    result = true;
                    db.setTransactionSuccessful();
                } catch (Throwable t) {
                    Log.e(this.getClass().getName(), t.getMessage(), t);
                } finally {
                    if (db != null) {
                        db.endTransaction();
                    }
                    db.close();
                }
                return result;
            }
        }.execute();
    }

    public void btnLoginClick(View v) {
        String username = mEditTextName.getText().toString();
        String password = mEditTextPwd.getText().toString();
        if(username.equals("")) {
            Toast.makeText(MyApp.getInstance(), "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        httpRequestLogin(username, password);
    }
}
