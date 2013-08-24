package com.ciheul.dirbancollector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciheul.dirbancollector.lib.Geolocator;

public class BusinessDetailActivity extends Activity implements OnClickListener {
    private EditText etName;
    private EditText etAddress;
    private Button btnSubmit;
    private Button btnCancel;
    private Button btnLocation;
    private Button btnPhoto;

    private TextView tvLongitude;
    private TextView tvLatitude;

    private ImageView mImageView;
    private Bitmap mImageBitmap;

    private Uri businessUri;
    private Uri photoUri;

    private static final String CONTRIBUTOR = "masjat";

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_detail);
        Log.d(MainActivity.TAG, "BusinessDetailActivity: onCreate");

        etName = (EditText) findViewById(R.id.business_detail_et_name);
        etAddress = (EditText) findViewById(R.id.business_detail_et_address);

        tvLongitude = (TextView) findViewById(R.id.business_detail_tv_longitude);
        tvLatitude = (TextView) findViewById(R.id.business_detail_tv_latitude);

        btnLocation = (Button) findViewById(R.id.business_detail_btn_location);
        btnPhoto = (Button) findViewById(R.id.business_detail_btn_photo);
        btnSubmit = (Button) findViewById(R.id.business_detail_btn_submit);
        btnCancel = (Button) findViewById(R.id.business_detail_btn_cancel);

        mImageView = (ImageView) findViewById(R.id.business_detail_iv_photo);
        mImageBitmap = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            btnSubmit.setText(R.string.business_detail_btn_update);
        }

        btnLocation.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        businessUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                .getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);

        if (extras != null) {
            businessUri = extras.getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);
            populate_business_detail(businessUri);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
        /** Get current longitude and latitude and show them up on the UI */
        case R.id.business_detail_btn_location:
            Geolocator gps = new Geolocator(this);

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            String geolocation = "Lon=" + Double.toString(longitude) + " & Lat="
                    + Double.toString(latitude);
            Toast.makeText(BusinessDetailActivity.this, geolocation, Toast.LENGTH_LONG).show();

            tvLongitude.setText(Double.toString(longitude));
            tvLatitude.setText(Double.toString(latitude));
            break;
        /**
         * Take a photo, either capturing from camera or retrieving from storage
         */
        case R.id.business_detail_btn_photo:
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            break;
        /** Insert or update all inputed/given information to database */
        case R.id.business_detail_btn_submit:
            if (TextUtils.isEmpty(etName.getText().toString())
                    || TextUtils.isEmpty(etAddress.getText().toString())) {
                Toast.makeText(BusinessDetailActivity.this,
                        getResources().getString(R.string.business_detail_empty_form),
                        Toast.LENGTH_LONG).show();
            } else {
                if (businessUri == null) {
                    Toast.makeText(BusinessDetailActivity.this,
                            getResources().getString(R.string.business_detail_insert_successfully),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BusinessDetailActivity.this,
                            getResources().getString(R.string.business_detail_update_successfully),
                            Toast.LENGTH_LONG).show();
                }

                saveState();
                finish();
            }
            break;
        /** Cancel any change and put the focus back to MainActivity */
        case R.id.business_detail_btn_cancel:
            Toast.makeText(BusinessDetailActivity.this,
                    getResources().getString(R.string.business_detail_cancel), Toast.LENGTH_LONG)
                    .show();
            finish();
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Bundle extras = data.getExtras();
                // mImageBitmap = (Bitmap) extras.get("data");
                // mImageView.setImageBitmap(mImageBitmap);
                // mImageView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Foto disimpan di:\n" + photoUri.toString(), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    // disable back button
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onSaveInstanceState(Bundle out_state) {
        super.onSaveInstanceState(out_state);
        saveState();
        out_state.putParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE, businessUri);
    }

    private void populate_business_detail(Uri uri) {
        String[] projection = { DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
            etAddress.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
        }
        cursor.close();
    }

    private void saveState() {
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();

        double longitude = Double.parseDouble(tvLongitude.getText().toString());
        double latitude = Double.parseDouble(tvLatitude.getText().toString());

        if (name.length() == 0 && address.length() == 0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, name);
        values.put(DatabaseHelper.COL_ADDRESS, address);
        values.put(DatabaseHelper.COL_LON, longitude);
        values.put(DatabaseHelper.COL_LAT, latitude);
        values.put(DatabaseHelper.COL_CONTRIBUTOR, CONTRIBUTOR);

        if (businessUri == null) {
            getContentResolver().insert(BusinessContentProvider.CONTENT_URI, values);
        } else {
            getContentResolver().update(businessUri, values, null, null);
        }
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(MainActivity.TAG, "Failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp
                    + ".jpg");
            Log.d(MainActivity.TAG, mediaFile.toString());
        } else {
            return null;
        }

        return mediaFile;
    }
}
