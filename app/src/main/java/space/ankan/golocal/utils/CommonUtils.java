package space.ankan.golocal.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import space.ankan.golocal.core.AppConstants;

/**
 * Created by Ankan.
 * Commonly used helper methods
 */

public class CommonUtils implements AppConstants {

    @SuppressWarnings("unused")
    public static void imagePickerForResult(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        activity.startActivityForResult(Intent.createChooser(intent, "Complete action using"), AppConstants.RC_PHOTO_PICKER);
    }

    public static void imagePickerForResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        fragment.startActivityForResult(Intent.createChooser(intent, "Complete action using"), AppConstants.RC_PHOTO_PICKER);
    }

    public static void closeKeyBoard(Activity activity) {

        if (activity == null) return;
        View view = activity.getCurrentFocus();
        if (view != null)

        {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    public static Double getLastLocationLatitude(SharedPreferences sharedPreferences) {
        double lat;
        try {
            lat = Double.parseDouble(sharedPreferences.getString(CACHED_LAST_LOCATION_LAT, ""));
        } catch (NumberFormatException nfe) {
            return AppConstants.DEFAULT_LATITUDE;
        }
        return lat;
    }

    public static Double getLastLocationLongitude(SharedPreferences sharedPreferences) {
        double lon;
        try {
            lon = Double.parseDouble(sharedPreferences.getString(CACHED_LAST_LOCATION_LON, ""));
        } catch (NumberFormatException nfe) {
            return AppConstants.DEFAULT_LONGITUDE;
        }
        return lon;
    }


    public static void cacheLocation(SharedPreferences sharedPreferences, Location location) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(CACHED_LAST_LOCATION_LAT, String.valueOf(location.getLatitude()));
        edit.putString(CACHED_LAST_LOCATION_LON, String.valueOf(location.getLongitude()));
        edit.apply();
    }

    public static void cacheLocationAddress(SharedPreferences sharedPreferences, String address) {
        if (TextUtils.isEmpty(address)) return;
        sharedPreferences.edit().putString(CACHED_LAST_ADDRESS, address).apply();
    }

    public static void removeViews(View... views) {
        if (views == null || views.length == 0) return;
        for (View v : views)
            if (v != null)
                v.setVisibility(View.GONE);
    }

    public static void showViews(View... views) {
        if (views == null || views.length == 0) return;
        for (View v : views) {
            if (v != null)
                v.setVisibility(View.VISIBLE);
        }
    }

    public static void hideViews(View... views) {
        if (views == null || views.length == 0) return;
        for (View v : views) {
            if (v != null)
                v.setVisibility(View.INVISIBLE);
        }
    }

    public static void setupTextRemoveIfEmpty(TextView view, String text, View optionalHideView) {
        if (view == null) return;
        if (TextUtils.isEmpty(text)) {
            removeViews(view, optionalHideView);
        } else {
            view.setText(text);
            showViews(view, optionalHideView);
        }

    }

    @SuppressWarnings("unused")
    public static void setupTextRemoveIfEmpty(TextView view, String text) {
        setupTextRemoveIfEmpty(view, text, null);

    }

    @SuppressWarnings("unused")
    public static void setupTextRemoveIfEmpty(TextView view, @StringRes int text) {
        setupTextRemoveIfEmpty(view, text, null);
    }

    public static void setupTextRemoveIfEmpty(TextView view, @StringRes int text, View optionalHideView) {
        if (view == null) return;
        view.setText(text);
        showViews(view, optionalHideView);
    }

    public static void updateWidgets(Context context) {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    public static Uri compressImage(Context context, Uri imageUri) {
        Bitmap realImage = getBitmapFromUri(context.getContentResolver(), imageUri);
        if (realImage == null) return null;

        float ratio = Math.min(
                AppConstants.IMAGE_MAX_SIZE / realImage.getWidth(),
                AppConstants.IMAGE_MAX_SIZE / realImage.getHeight());
        if (ratio > 1) return imageUri;

        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, true);
        Log.i(AppConstants.TAG, "Compressed image from size " + realImage.getAllocationByteCount() + " to " + newBitmap.getAllocationByteCount());
        return getImageUri(newBitmap);
    }

    private static Bitmap getBitmapFromUri(ContentResolver contentResolver, Uri uri) {

        try {
            return MediaStore.Images.Media.getBitmap(contentResolver, uri);
        } catch (IOException io) {
            return null;
        }

    }

    private static Uri getImageUri(Bitmap inImage) {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        tempDir.mkdir();
        try {


            File tempFile = File.createTempFile("Kitchen", ".jpg", tempDir);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] bitmapData = bytes.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return Uri.fromFile(tempFile);
        } catch (IOException io) {
            return null;
        }
    }

    public static boolean hasPermission(Fragment fragment, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        if (fragment.getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        fragment.requestPermissions(new String[]{permission}, AppConstants.PERMISSION_REQUEST_CODE);
        return false;
    }

}
