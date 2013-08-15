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

    private static final int ALL_BUSINESS = 10;
    private static final int BUSINESS = 20;
    private static final String AUTHORITY = "com.ciheul.dirbancollector.BusinessContentProvider";
    private static final String BASE_PATH = "all_business";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/all_business";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/business";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ALL_BUSINESS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BUSINESS);
    }

    @Override
    public boolean onCreate() {
        db = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selection_args,
            String sort_order) {
        SQLiteQueryBuilder query_builder = new SQLiteQueryBuilder();
        query_builder.setTables(DatabaseHelper.TABLE_BUSINESS);

        int uri_type = sURIMatcher.match(uri);
        switch (uri_type) {
        case ALL_BUSINESS:
            Log.d(MainActivity.TAG, "BusinessContentProvider: query: all_business");
            break;
        case BUSINESS:
            Log.d(MainActivity.TAG, "BusinessContentProvider: query: business");
            query_builder.appendWhere(DatabaseHelper.COL_BUSINESS_ID + "="
                    + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = query_builder.query(db.getWritableDatabase(), projection, selection,
                selection_args, null, null, sort_order);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uri_type = sURIMatcher.match(uri);
        long id = 0;
        switch (uri_type) {
        case ALL_BUSINESS:
            id = db.getWritableDatabase().insert(DatabaseHelper.TABLE_BUSINESS, null, values);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selection_args) {
        int uri_type = sURIMatcher.match(uri);
        int rows_updated = 0;
        switch (uri_type) {
        case ALL_BUSINESS:
            rows_updated = db.getWritableDatabase().update(DatabaseHelper.TABLE_BUSINESS, values,
                    selection, selection_args);
            break;
        case BUSINESS:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                String full_selection = DatabaseHelper.TABLE_BUSINESS + "=" + id;
                rows_updated = db.getWritableDatabase().update(DatabaseHelper.TABLE_BUSINESS,
                        values, full_selection, null);
            } else {
                String full_selection = DatabaseHelper.TABLE_BUSINESS + "=" + id + " and "
                        + selection;
                rows_updated = db.getWritableDatabase().update(DatabaseHelper.TABLE_BUSINESS,
                        values, full_selection, selection_args);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows_updated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selection_args) {
        int uri_type = sURIMatcher.match(uri);
        int rows_deleted = 0;
        switch (uri_type) {
        case ALL_BUSINESS:
            Log.d(MainActivity.TAG, "BusinessContentProvider: delete: all_business");
            rows_deleted = db.getWritableDatabase().delete(DatabaseHelper.TABLE_BUSINESS,
                    selection, selection_args);
            break;
        case BUSINESS:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                Log.d(MainActivity.TAG, "BusinessContentProvider: delete: selection is empty");
                String full_selection = DatabaseHelper.COL_BUSINESS_ID + "=" + id;
                rows_deleted = db.getWritableDatabase().delete(DatabaseHelper.TABLE_BUSINESS,
                        full_selection, null);
            } else {
                Log.d(MainActivity.TAG, "BusinessContentProvider: delete: selection is filled");
                String full_selection = DatabaseHelper.COL_BUSINESS_ID + "=" + id + " and "
                        + selection;
                rows_deleted = db.getWritableDatabase().delete(DatabaseHelper.TABLE_BUSINESS,
                        full_selection, selection_args);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows_deleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
