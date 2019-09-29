package com.tlg.storehelper.activity.common;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.R;
import com.tlg.storehelper.httprequest.net.entity.SimpleListMapEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

public class LoginActivity extends BaseRxAppCompatActivity {

    private EditText mEditTextName;
    private EditText mEditTextPwd;
    private Button mBtnLogin;

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected int setToolbarResourceID() {
        return 0;
    }

    @Override
    protected void initView() {
        // find view
        mEditTextName = findViewById(R.id.etUsername);
        mEditTextPwd = findViewById(R.id.etPwd);
        mBtnLogin = findViewById(R.id.btnLogin);

        // initialize controls
        hideKeyboard(true);
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_login;
    }

    public void btnLoginClick(View v) {
        String username = mEditTextName.getText().toString().toUpperCase();
        String password = mEditTextPwd.getText().toString();
        if(username.equals("")) {
            AndroidUtil.showToast("用户名不能为空");
            return;
        }
        RequestUtil.requestLogin(username, password, this, new RequestUtil.OnSuccessListener<SimpleListMapEntity<String>>() {
            @Override
            public void onSuccess(SimpleListMapEntity<String> response) {
                Intent intent = new Intent(_this, MainActivity.class);
                String[] array = response.list.toArray(new String[response.list.size()]);
                intent.putExtra("storeCodes", array);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

}
