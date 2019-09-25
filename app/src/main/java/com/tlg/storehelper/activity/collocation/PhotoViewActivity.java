package com.tlg.storehelper.activity.collocation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.photoview.PhotoView;
import com.nec.lib.android.utils.ImageUtil;
import com.tlg.storehelper.R;

import java.io.File;

public class PhotoViewActivity extends BaseRxAppCompatActivity {

    private PhotoView ivPhotoView;        //图片
    private ImageView ivBack;

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
        ivPhotoView = findViewById(R.id.ivPhotoView);
        ivBack = findViewById(R.id.ivBack);

        // initialize controls
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //接收参数
        Intent intent =getIntent();
        String filename2find = intent.getStringExtra("file_path");
        File file2find = new File(filename2find);
        if(file2find.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(filename2find, ImageUtil.getBitmapOption(1));
            ivPhotoView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_photo_view;
    }
}
