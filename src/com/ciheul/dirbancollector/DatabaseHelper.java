package com.ciheul.dirbancollector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // database information
    private static final String DATABASE_NAME = "ciheul.db";
    private static final int DATABASE_VERSION = 1;

    // table: business
    public static final String TABLE_BUSINESS = "business";
    public static final String COL_BUSINESS_ID = "_id";
    public static final String COL_BUSINESS_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_BUSINESS_TYPE = "business_type";
    public static final String COL_LON = "longitude";
    public static final String COL_LAT = "latitude";
    public static final String COL_CONTRIBUTOR = "contributor";
    public static final String COL_BUSINESS_UPLOAD_STATUS = "status";

    // table: images
    public static final String TABLE_IMAGE = "image";
    public static final String COL_IMAGE_ID = "_id";
    public static final String COL_IMAGE_NAME = "name";
    public static final String COL_BUSINESS_PK = "business_id";
    public static final String COL_IMAGE_UPLOAD_STATUS = "status";
    
    // upload status
    public static final int NOT_YET = 0;
    public static final int UPLOADED = 1;
    public static final int MODIFIED = 2;

    // create business table
    private static final String CREATE_TABLE_BUSINESS = "CREATE TABLE " + TABLE_BUSINESS + "(" + COL_BUSINESS_ID
            + " integer primary key autoincrement, " + COL_BUSINESS_NAME + " text not null, " + COL_ADDRESS
            + " text not null, " + COL_BUSINESS_TYPE + " text not null, " + COL_LON + " real not null, " + COL_LAT
            + " real not null, " + COL_CONTRIBUTOR + " text not null, " + COL_BUSINESS_UPLOAD_STATUS
            + " integer not null);";

    // create images table
    private static final String CREATE_TABLE_IMAGES = "CREATE TABLE " + TABLE_IMAGE + "(" + COL_IMAGE_ID
            + " integer primary key autoincrement, " + COL_IMAGE_NAME + " text not null, " + COL_BUSINESS_PK
            + " integer not null, " + COL_IMAGE_UPLOAD_STATUS + " integer not null);";

    // drop business table
    private static final String DROP_TABLE_BUSINESS = "DROP TABLE IF EXISTS " + TABLE_BUSINESS;

    // drop images table
    private static final String DROP_TABLE_IMAGES = "DROP TABLE IF EXISTS " + TABLE_IMAGE;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BUSINESS);
        db.execSQL(CREATE_TABLE_IMAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgrade_message = "Upgrade database v" + oldVersion + " to v" + newVersion + ".";
        Log.w(DatabaseHelper.class.getName(), upgrade_message);
        db.execSQL(DROP_TABLE_BUSINESS);
        db.execSQL(DROP_TABLE_IMAGES);
        onCreate(db);
    }
}
