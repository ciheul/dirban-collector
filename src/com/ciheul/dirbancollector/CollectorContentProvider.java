package com.ciheul.dirbancollector;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class CollectorContentProvider extends ContentProvider {

    private DatabaseHelper db;

    private static final int ALL_BUSINESS = 10;
    private static final int BUSINESS = 20;
    private static final String AUTHORITY = "com.ciheul.dirbancollector.CollectorContentProvider";
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
        return null;
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
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selection_args) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
