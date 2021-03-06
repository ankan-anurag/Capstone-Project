package space.ankan.golocal.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Set;

import space.ankan.golocal.core.AppConstants;
import space.ankan.golocal.model.kitchens.Kitchen;
import space.ankan.golocal.persistence.DBContract;
import space.ankan.golocal.persistence.DBContract.KitchenEntry;

import static space.ankan.golocal.core.AppConstants.SHARED_PREF_FILE_NAME;

/**
 * Created by ankan.
 * Helper class to help with db operations
 */

public class DBUtils {

    public static final String[] KITCHEN_PROJECTION = new String[]{
            KitchenEntry._ID,
            KitchenEntry.COLUMN_KITCHEN_ID,
            KitchenEntry.COLUMN_TITLE,
            KitchenEntry.COLUMN_USER_ID,
            KitchenEntry.COLUMN_DESCRIPTION,
            KitchenEntry.COLUMN_LATITUDE,
            KitchenEntry.COLUMN_LONGITUDE,
            KitchenEntry.COLUMN_IMAGE_URL,
            KitchenEntry.COLUMN_RATED_USER_COUNT,
            KitchenEntry.COLUMN_OVERALL_RATING,
            KitchenEntry.COLUMN_IS_FAVOURITE,
            KitchenEntry.COLUMN_USER_RATING,
            KitchenEntry.COLUMN_ADDRESS};

    public static ContentValues getContentValuesFromKitchen(Kitchen kitchen) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KitchenEntry.COLUMN_KITCHEN_ID, kitchen.key);
        contentValues.put(KitchenEntry.COLUMN_TITLE, kitchen.name);
        contentValues.put(KitchenEntry.COLUMN_USER_ID, kitchen.userId);
        contentValues.put(KitchenEntry.COLUMN_DESCRIPTION, kitchen.description);
        contentValues.put(KitchenEntry.COLUMN_LATITUDE, kitchen.latitude);
        contentValues.put(KitchenEntry.COLUMN_LONGITUDE, kitchen.longitude);
        contentValues.put(KitchenEntry.COLUMN_IMAGE_URL, kitchen.imageUrl);
        contentValues.put(KitchenEntry.COLUMN_RATED_USER_COUNT, kitchen.ratedUserCount);
        contentValues.put(KitchenEntry.COLUMN_OVERALL_RATING, kitchen.overallRating);
        contentValues.put(KitchenEntry.COLUMN_IS_FAVOURITE, 1);
        contentValues.put(KitchenEntry.COLUMN_USER_RATING, 2);
        contentValues.put(KitchenEntry.COLUMN_ADDRESS, kitchen.address);
        return contentValues;
    }

    public static Kitchen getKitchenFromCursor(Cursor c) {
        if (c.getCount() == 0) return null;
        return new Kitchen(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getString(7), c.getInt(8), c.getDouble(9), c.getInt(10), c.getString(12));

    }

    public static Uri insertKitchen(ContentResolver contentResolver, Kitchen kitchen) {
        if (contentResolver == null) return null;
        Log.i("DBProvider", "inserting kitchen with id " + kitchen.key);
        return contentResolver.insert(DBContract.KitchenEntry.CONTENT_URI, getContentValuesFromKitchen(kitchen));

    }

    public static int deleteKitchen(ContentResolver contentResolver, String id) {
        if (contentResolver == null) return 0;
        Log.i("DBProvider", "deleting kitchen with id " + id);
        return contentResolver.delete(DBContract.KitchenEntry.CONTENT_URI, DBContract.KitchenEntry.COLUMN_KITCHEN_ID + " = ?", new String[]{id});

    }

    public static Kitchen queryKitchenById(ContentResolver contentResolver, String id) {
        Cursor c = contentResolver.query(DBContract.KitchenEntry.CONTENT_URI, KITCHEN_PROJECTION, DBContract.KitchenEntry.COLUMN_KITCHEN_ID + " = ?", new String[]{id}, null);
        if (c == null || !c.moveToFirst()) return null;
        return getKitchenFromCursor(c);

    }

    public static Cursor queryFavouriteKitchens(ContentResolver contentResolver) {
        return contentResolver.query(DBContract.KitchenEntry.CONTENT_URI, KITCHEN_PROJECTION, KitchenEntry.COLUMN_IS_FAVOURITE + " = ?", new String[]{"1"}, null);
        //Log.v("DBProvider", c.getCount() + " favorite kitchens found");
    }

    public static void queryFavouriteKitchenIDList(ContentResolver contentResolver, Set<String> keys) {
        Cursor c = queryFavouriteKitchens(contentResolver);
        keys.clear();

        if (!c.moveToFirst()) return;

        do {
            keys.add(c.getString(1));
        } while (c.moveToNext());


    }

    @SuppressWarnings("unused")
    public static void persistUserId(Context c, String userId) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(AppConstants.USER_ID, userId).apply();
    }

    @SuppressWarnings("unused")
    public static String getUserId(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstants.USER_ID, null);
    }


}
