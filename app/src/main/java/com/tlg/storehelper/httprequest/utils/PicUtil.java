package com.tlg.storehelper.httprequest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.nec.lib.android.utils.DownloadUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;

import java.io.File;

public class PicUtil {

    /**加载图片控件，优先用本地文件*/
    public static void loadPic(ImageView ivPicture, String filePath, String filename, int sampleSize) {
        if(filename == null || filename.isEmpty()) {
            ivPicture.setImageBitmap(com.nec.lib.android.utils.ImageUtil.getBitmap(MyApp.getInstance(), R.drawable.nopic));
            return;
        }
        String filename2find = filePath + filename;
        File file2find = new File(filename2find);
        if(file2find.exists() && file2find.length() > 0) {
            Bitmap bitmap = BitmapFactory.decodeFile(filename2find, com.nec.lib.android.utils.ImageUtil.getBitmapOption(sampleSize));
            if(bitmap != null) {
                ivPicture.setImageBitmap(bitmap);
                return;
            }
        }
        ivPicture.setImageBitmap(com.nec.lib.android.utils.ImageUtil.getBitmap(MyApp.getInstance(), R.drawable.pic_loading));
        new DownloadUtil(filePath, filename, MyApp.baseUrl + "pre_api/pic/" + filename, false, new DownloadUtil.DownloadListener() {
            @Override
            public void onDownloadFail(String localFilePath, String msg) {
                ivPicture.setImageBitmap(com.nec.lib.android.utils.ImageUtil.getBitmap(MyApp.getInstance(), R.drawable.nopic));
            }
            @Override
            public void onDownloaded(String localFilePath) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFilePath, com.nec.lib.android.utils.ImageUtil.getBitmapOption(2));
                ivPicture.setImageBitmap(bitmap);
            }
        }).executeDownload();

        /** 废除
        RequestUtil.downloadPic(MyApp.baseUrl + "pre_api/pic/" + filename, new RequestUtil.OnFileDownloadedListener() {

            @Override
            public void onSuccess(String pathFile) {
                Bitmap bitmap = BitmapFactory.decodeFile(pathFile, com.nec.lib.android.utils.ImageUtil.getBitmapOption(2));
                ivPicture.setImageBitmap(bitmap);
            }

            @Override
            public void onFail() {
                ivPicture.setImageBitmap(com.nec.lib.android.utils.ImageUtil.getBitmap(MyApplication.getInstance(), R.drawable.nopic));
            }
        });*/
    }

}
