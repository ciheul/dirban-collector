package com.ciheul.dirbancollector;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class BusinessContentProvider extends ContentProvider {

    private DatabaseHelper db;

    private static final int BUSINESSES = 10;
    private static final int BUSINESS_ID = 20;

    private static final String AUTHORITY = "com.ciheul.dirbancollector.BusinessContentProvider";
    private static final String BASE_PATH = "businesses";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/businesses";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/business";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, BUSINESSES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BUSINESS_ID);
    }

    @Override
    public boolean onCreate() {
        db = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DatabaseHelper.TABLE_BUSINESS);

        int uriType = sURIMatcher.match(uri);
        Log.d(MainActivity.TAG, uri.toString());
        Log.d(MainActivity.TAG, String.valueOf(uriType));

        switch (uriType) {
        case BUSINESSES:
            Log.d(MainActivity.TAG, "BusinessContentProvider: query: businesses");
            break;
        case BUSINESS_ID:
            Log.d(MainActivity.TAG, "BusinessContentProvider: query: business_id");
            queryBuilder.appendWhere(DatabaseHelper.COL_BUSINESS_ID + "=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db.getWritableDatabase(), projection, selection, selectionArgs, null, null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;

        switch (uriType) {
        case BUSINESSES:
            Log.d(MainActivity.TAG, "BusinessContentProvider: insert: all_business");
            id = db.getWritableDatabase().insert(DatabaseHelper.TABLE_BUSINESS, null, values);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsUpdated = 0;

        switch (uriType) {

        case BUSINESSES:
            Log.d(MainActivity.TAG, "BusinessContentProvider: update: businesses");
            rowsUpdated = db.getWritableDatabase().update(DatabaseHelper.TABLE_BUSINESS, values, selection,
                    selectionArgs);
            break;
        case BUSINESS_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                Log.d(MainActivity.TAG, "BusinessContentProvider: update: business_id: selection is empty");
                String full_selection = DatabaseHelper.COL_BUSINESS_ID + "=" + id;
                rowsUpdated = db.getWritableDatabase().update(DatabaseHelper.TABLE_BUSINESS, values, full_selection,
                        null);
            } else {
                Log.d(MainActivity.TAG, "BusinessContentProvider: update: business_id: selection is filled");
                String full_selection = DatabaseHelper.COL_BUSINESS_ID + "=" + id + " and " + selection;
                rowsUpdated = db.getWritableDatabase().update(DatabaseHelper.TABLE_BUSINESS, values, full_selection,
                        selectionArgs);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsDeleted = 0;

        switch (uriType) {
        case BUSINESSES:
            Log.d(MainActivity.TAG, "BusinessContentProvider: delete: businesses");
            rowsDeleted = db.getWritableDatabase().delete(DatabaseHelper.TABLE_BUSINESS, selection, selectionArgs);
            break;
        case BUSINESS_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                Log.d(MainActivity.TAG, "BusinessContentProvider: delete: business_id: selection is empty");
                String full_selection = DatabaseHelper.COL_BUSINESS_ID + "=" + id;
                rowsDeleted = db.getWritableDatabase().delete(DatabaseHelper.TABLE_BUSINESS, full_selection, null);
            } else {
                Log.d(MainActivity.TAG, "BusinessContentProvider: delete: business_id: selection is filled");
                String full_selection = DatabaseHelper.COL_BUSINESS_ID + "=" + id + " and " + selection;
                rowsDeleted = db.getWritableDatabase().delete(DatabaseHelper.TABLE_BUSINESS, full_selection,
                        selectionArgs);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
