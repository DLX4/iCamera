package me.shouheng.icamerasample.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;

import me.shouheng.icamerasample.R;
import me.shouheng.utils.BuildConfig;
import me.shouheng.utils.app.ResUtils;
import me.shouheng.utils.stability.L;
import me.shouheng.utils.store.FileUtils;
import me.shouheng.utils.store.PathUtils;

public class FileHelper {

    public static void saveImageToGallery(Context context, File file, String fileName) {
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            L.d("saveImageToGallery: FileNotFoundException MediaStore");
            e.printStackTrace();
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, getUriFromFile(context, file)));
    }

    public static Uri getUriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static void saveVideoToGallery(Context context, File file, String fileName) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, getUriFromFile(context, file)));
    }

    public static File getSavedFile(String appendix) {
        File appDir = new File(PathUtils.getExternalPicturesPath(), ResUtils.getString(R.string.app_name));
        FileUtils.createOrExistsDir(appDir.getPath());
        String fileName = "${System.currentTimeMillis()}.${appendix}";
        return new File(appDir, fileName);
    }

}
