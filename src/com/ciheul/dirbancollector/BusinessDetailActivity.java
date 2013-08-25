package com.ciheul.dirbancollector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    private Uri businessUri;

    private String mCurrentPhotoPath;

    private static final String CONTRIBUTOR = "masjat";

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_detail);
        Log.d(MainActivity.TAG, "BusinessDetailActivity: onCreate");

        Bundle extras = getIntent().getExtras();

        etName = (EditText) findViewById(R.id.business_detail_et_name);
        etAddress = (EditText) findViewById(R.id.business_detail_et_address);

        tvLongitude = (TextView) findViewById(R.id.business_detail_tv_longitude);
        tvLatitude = (TextView) findViewById(R.id.business_detail_tv_latitude);

        btnLocation = (Button) findViewById(R.id.business_detail_btn_location);
        btnPhoto = (Button) findViewById(R.id.business_detail_btn_photo);
        btnSubmit = (Button) findViewById(R.id.business_detail_btn_submit);
        btnCancel = (Button) findViewById(R.id.business_detail_btn_cancel);

        if (extras != null) {
            btnSubmit.setText(R.string.business_detail_btn_update);
        }

        mImageView = (ImageView) findViewById(R.id.business_detail_iv_photo);

        btnLocation.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        businessUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                .getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);

        if (businessUri != null) {
            Log.d(MainActivity.TAG, "businessUri=" + businessUri.toString());
        } else {
            Log.d(MainActivity.TAG, "businessUri=null");
        }

        if (extras != null) {
            businessUri = extras.getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);
            Log.d(MainActivity.TAG, "(if extras is not null: businessUri=" + businessUri.toString());
            populateBusinessDetail(businessUri);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
        /** Get current longitude and latitude and show them up on the UI */
        case R.id.business_detail_btn_location:
            Geolocator gps = new Geolocator(this);

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            String geolocation = "Lon=" + Double.toString(longitude) + " & Lat=" + Double.toString(latitude);
            Toast.makeText(BusinessDetailActivity.this, geolocation, Toast.LENGTH_LONG).show();

            tvLongitude.setText(Double.toString(longitude));
            tvLatitude.setText(Double.toString(latitude));
            break;
        /**
         * Take a photo, either capturing from camera or retrieving from storage
         */
        case R.id.business_detail_btn_photo:
            // create a photo file handler and its path
            File photoFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            mCurrentPhotoPath = photoFile.getAbsolutePath();

            // send intent to call built-in camera app
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            break;
        /** Insert or update all inputed/given information to database */
        case R.id.business_detail_btn_submit:
            if (TextUtils.isEmpty(etName.getText().toString()) || TextUtils.isEmpty(etAddress.getText().toString())) {
                Toast.makeText(BusinessDetailActivity.this,
                        getResources().getString(R.string.business_detail_empty_form), Toast.LENGTH_LONG).show();
            } else {
                if (businessUri == null) {
                    Toast.makeText(BusinessDetailActivity.this,
                            getResources().getString(R.string.business_detail_insert_successfully), Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(BusinessDetailActivity.this,
                            getResources().getString(R.string.business_detail_update_successfully), Toast.LENGTH_LONG)
                            .show();
                }

                saveState();
                finish();
            }
            break;
        /** Cancel any change and put the focus back to MainActivity */
        case R.id.business_detail_btn_cancel:
            Toast.makeText(BusinessDetailActivity.this, getResources().getString(R.string.business_detail_cancel),
                    Toast.LENGTH_LONG).show();
            finish();
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                setPic();
                galleryAddPic();
                Toast.makeText(this, "Foto disimpan di:\n" + mCurrentPhotoPath, Toast.LENGTH_LONG).show();
            }
        }
    }

    // disable back button
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(MainActivity.TAG, "onSaveInstanceState");
//        saveState();
        outState.putParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE, businessUri);
    }

    private void populateBusinessDetail(Uri uri) {
        String[] projection = { DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
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

    /* taken from Android Developer documentation */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
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
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            Log.d(MainActivity.TAG, mediaFile.toString());
        } else {
            return null;
        }

        return mediaFile;
    }

    /* taken from Android Developer documentation */
    private void setPic() {
        /* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

        /* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        Log.d(MainActivity.TAG, "width image  = " + String.valueOf(targetW));
        Log.d(MainActivity.TAG, "height image = " + String.valueOf(targetH));

        /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.d(MainActivity.TAG, "width photo  = " + String.valueOf(photoW));
        Log.d(MainActivity.TAG, "height photo = " + String.valueOf(photoH));
        
        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        /* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        /* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
        Log.d(MainActivity.TAG, "set ImageView");
    }

    /* taken from Android Developer documentation */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
