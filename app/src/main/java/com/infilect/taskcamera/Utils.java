package com.infilect.taskcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;

import androidx.camera.core.CameraSelector;
import androidx.exifinterface.media.ExifInterface;

import com.infilect.taskcamera.helper.location.CustomLocationManager;
import com.wooplr.spotlight.SpotlightView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static void openSetting(Activity activity) {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(myAppSettings);

    }

    public static File rotateImage(Context context, File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Bitmap rotatedBitmap = flip(bitmap);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = getFilePath(context);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getFilePath(Context context) {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String app_folder_path = "";
        app_folder_path = context.getExternalCacheDir().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {
        }
        return new File(app_folder_path, mDateFormat.format(new Date()) + ".jpg");
    }

    public static Bitmap flip(Bitmap src) {

        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        matrix.postRotate(90.f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static void showOnboardingScreen(Activity activity, View view) {
        new SpotlightView.Builder(activity)
                .enableDismissAfterShown(false)
                .introAnimationDuration(400)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(R.color.purple_700)
                .headingTvSize(32)
                .headingTvText(activity.getString(R.string.on_board_heading))
                .subHeadingTvColor(R.color.white)
                .subHeadingTvSize(16)
                .subHeadingTvText(activity.getString(R.string.on_board_contents))
                .target(view)
                .lineAnimDuration(400)
                .maskColor(R.color.red)
                .lineAndArcColor(R.color.white)
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .show();
    }

    public static File addLocationDetails(File file, Location currentLocation) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.toString());
            exif.setGpsInfo(currentLocation);
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File processCapturedImage(Context context,
                                            File file,
                                            boolean isRecord,
                                            CustomLocationManager locationManager,
                                            int cameraFacing) {
        File savedFile =
                cameraFacing == CameraSelector.LENS_FACING_FRONT ?
                        Utils.rotateImage(context, file) : file;
        if (isRecord) {
            savedFile = Utils.addLocationDetails(savedFile, locationManager.getCurrentLocation());
        }
        return savedFile;

    }

    public static double[] getLatLongFromFile(String imageFilePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imageFilePath);
            double[] latLong = exif.getLatLong();
            if (latLong != null && latLong.length == 0) {
                return null;
            }
            return latLong;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
