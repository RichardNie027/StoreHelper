package com.tlg.storehelper.activity.common;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nec.lib.grantor.PermissionListener;
import com.nec.lib.grantor.PermissionsUtil;
import com.nec.lib.base.BaseRxAppCompatActivity;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.nec.lib.utils.UiUtil;
import com.tlg.storehelper.httprequest.net.entity.SimpleListEntity;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

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
            RequestUtil.requestToken(this, new RequestUtil.OnSuccessListener<SimpleMapEntity>() {
                @Override
                public void onSuccess(SimpleMapEntity response) {
                    mBtnLogin.setVisibility(View.VISIBLE);
                    RequestUtil.requestGoodBarcodes(_this, null);
                }
            });
    }

    public void btnLoginClick(View v) {
        String username = mEditTextName.getText().toString().toUpperCase();
        String password = mEditTextPwd.getText().toString();
        if(username.equals("")) {
            Toast.makeText(MyApp.getInstance(), "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestUtil.requestLogin(username, password, this, new RequestUtil.OnSuccessListener<SimpleListEntity<String>>() {
            @Override
            public void onSuccess(SimpleListEntity<String> response) {
                Intent intent = new Intent(_this, HomeActivity.class);
                String[] array = response.result.toArray(new String[response.result.size()]);
                intent.putExtra("storeCodes", array);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
