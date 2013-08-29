package com.ciheul.dirbancollector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ciheul.dirbancollector.lib.GPSTracker;
import com.ciheul.dirbancollector.lib.Geolocator;

public class BusinessDetailActivity extends Activity implements OnClickListener {
    private EditText etName;
    private EditText etAddress;

    private Button btnLocation;
    private Button btnImage;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnSubmit;
    private Button btnCancel;

    private ArrayAdapter<CharSequence> adapterBusinessType;
    private Spinner spBusinessType;

    private TextView tvLongitude;
    private TextView tvLatitude;

    private ImageView mImageView;

    private Uri imageUri;
    private Uri businessUri;
    private int businessId;

    private File imageFile;
    public ArrayList<String> imageList;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int GALLERY_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final String IMAGE_LIST = "image_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_detail);
        Log.d(MainActivity.TAG, "BusinessDetailActivity: onCreate");

        // the intent from previous activity might bring extra information, such
        // as Business URI
        Bundle extras = getIntent().getExtras();

        etName = (EditText) findViewById(R.id.business_detail_et_name);
        etAddress = (EditText) findViewById(R.id.business_detail_et_address);

        spBusinessType = (Spinner) findViewById(R.id.business_detail_sp_business_type);

        tvLongitude = (TextView) findViewById(R.id.business_detail_tv_longitude);
        tvLatitude = (TextView) findViewById(R.id.business_detail_tv_latitude);

        btnLocation = (Button) findViewById(R.id.business_detail_btn_location);
        btnCamera = (Button) findViewById(R.id.business_detail_btn_camera);
        btnImage = (Button) findViewById(R.id.business_detail_btn_image);
        btnGallery = (Button) findViewById(R.id.business_detail_btn_gallery);
        btnSubmit = (Button) findViewById(R.id.business_detail_btn_submit);
        btnCancel = (Button) findViewById(R.id.business_detail_btn_cancel);

        // if updating business detail, change the text inside
        if (extras != null) {
            btnSubmit.setText(R.string.business_detail_btn_update);
        }

        mImageView = (ImageView) findViewById(R.id.business_detail_iv_photo);

        btnLocation.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        adapterBusinessType = ArrayAdapter.createFromResource(this, R.array.business_type,
                android.R.layout.simple_spinner_item);
        adapterBusinessType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBusinessType.setAdapter(adapterBusinessType);
        spBusinessType.setSelection(0);

        businessUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                .getParcelable(BusinessContentProvider.BUSINESS_CONTENT_ITEM_TYPE);

        // set businessUri from caller's intent
        if (extras != null) {
            businessUri = extras.getParcelable(BusinessContentProvider.BUSINESS_CONTENT_ITEM_TYPE);
            populateBusinessDetail(businessUri);
            Log.d(MainActivity.TAG, "(if extras is not null: businessUri=" + businessUri.toString());
        }

        // set businessId
        if (businessUri != null) {
            businessId = Integer.parseInt(businessUri.getLastPathSegment());

            // set imageUri
            imageUri = Uri.parse(BusinessContentProvider.IMAGE_CONTENT_URI + "/" + businessId);
            imageList = getImageList(imageUri);
        }

        // if a business already has picture(s), show the image collection's
        // button up
        if (getNumOfImages() != 0) {
            btnImage.setText(getNumOfImages() + " Foto");
            btnImage.setVisibility(View.VISIBLE);
            btnImage.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MainActivity.TAG, "onResume");

        // at the first time, imageList is null
        if (imageList != null) {
            // update the number of photos in image button after any delete
            // operation in ImageListActivity
            imageList = getImageList(imageUri);
            if (getNumOfImages() != 0) {
                btnImage.setText(getNumOfImages() + " Foto");
                btnImage.setVisibility(View.VISIBLE);
                btnImage.setOnClickListener(this);
            } else {
                btnImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
        /** Get current longitude and latitude and show them up on the UI */
        case R.id.business_detail_btn_location:
//            Geolocator gps = new Geolocator(this);
//
//            double latitude = gps.getLatitude();
//            double longitude = gps.getLongitude();
//
//            String geolocation = "Lon=" + Double.toString(longitude) + " & Lat=" + Double.toString(latitude);
//            Toast.makeText(BusinessDetailActivity.this, geolocation, Toast.LENGTH_LONG).show();

//            tvLongitude.setText(Double.toString(longitude));
//            tvLatitude.setText(Double.toString(latitude));
            
            GPSTracker gps = new GPSTracker(this);
            if (gps.canGetLocation()) {
                tvLongitude.setText(Double.toString(gps.getLongitude()));
                tvLatitude.setText(Double.toString(gps.getLatitude()));
            } else {
                gps.showSettingsAlert();
            }
            
            
            break;
        /**
         * Take a photo, either capturing from camera or retrieving from storage
         */
        case R.id.business_detail_btn_camera:
            // create a photo file handler and its path
            imageFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            // send intent to call built-in camera app
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            break;
        case R.id.business_detail_btn_gallery:
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_ACTIVITY_REQUEST_CODE);
            break;
        case R.id.business_detail_btn_image:
            Intent imageIntent = new Intent(this, ImageListActivity.class);
            imageIntent.putExtra(BusinessContentProvider.IMAGE_CONTENT_ITEM_TYPE, imageUri);
            imageIntent.putStringArrayListExtra(IMAGE_LIST, imageList);
            startActivity(imageIntent);
            break;
        /** Insert or update all inputed/given information to database */
        case R.id.business_detail_btn_submit:
            if (TextUtils.isEmpty(etName.getText().toString()) || TextUtils.isEmpty(etAddress.getText().toString())) {
                Toast.makeText(BusinessDetailActivity.this,
                        getResources().getString(R.string.business_detail_empty_form), Toast.LENGTH_LONG).show();
            } else if (spBusinessType.getSelectedItemPosition() == 0) {
                Toast.makeText(BusinessDetailActivity.this,
                        getResources().getString(R.string.business_detail_unselected_business_type), Toast.LENGTH_LONG)
                        .show();
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
                setImage();
                galleryAddImage();
                Toast.makeText(this, "Foto disimpan di:\n" + imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                break;
            case GALLERY_ACTIVITY_REQUEST_CODE:
                if (data != null) {
                    mImageView.setImageURI(data.getData());
                    imageFile = new File(getRealPathFromURI(data.getData()));
                }
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
        // saveState();
        outState.putParcelable(BusinessContentProvider.BUSINESS_CONTENT_ITEM_TYPE, businessUri);
    }

    private void populateBusinessDetail(Uri uri) {
        String[] projection = { DatabaseHelper.COL_BUSINESS_NAME, DatabaseHelper.COL_ADDRESS,
                DatabaseHelper.COL_BUSINESS_TYPE, DatabaseHelper.COL_LON, DatabaseHelper.COL_LAT };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUSINESS_NAME)));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));

            String businessType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUSINESS_TYPE));
            spBusinessType.setSelection(adapterBusinessType.getPosition(businessType));

            tvLongitude.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LON)));
            tvLatitude.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT)));
        }
        cursor.close();
    }

    private void saveState() {
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();

        if (name.length() == 0 && address.length() == 0) {
            return;
        }

        String businessType = spBusinessType.getSelectedItem().toString();

        double latitude = 0.0;
        double longitude = 0.0;

        try {
            longitude = Double.parseDouble(tvLongitude.getText().toString());
            latitude = Double.parseDouble(tvLatitude.getText().toString());
        } catch (NumberFormatException e) {
            // textview has not been set
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUSINESS_NAME, name);
        values.put(DatabaseHelper.COL_ADDRESS, address);
        values.put(DatabaseHelper.COL_BUSINESS_TYPE, businessType);
        values.put(DatabaseHelper.COL_LON, longitude);
        values.put(DatabaseHelper.COL_LAT, latitude);
        values.put(DatabaseHelper.COL_CONTRIBUTOR, DatabaseHelper.CONTRIBUTOR);
        values.put(DatabaseHelper.COL_BUSINESS_UPLOAD_STATUS, DatabaseHelper.NOT_YET);

        // insert a new business detail
        if (businessUri == null) {
            Uri uri = getContentResolver().insert(BusinessContentProvider.BUSINESS_CONTENT_URI, values);
            Log.d(MainActivity.TAG, "saveState: businessId=" + uri.getLastPathSegment());

            // insert picture if attached
            if (mImageView.getDrawable() != null) {
                int businessId = Integer.parseInt(uri.getLastPathSegment());
                insertImage(businessId);
            }
        }
        // update business detail
        else {
            getContentResolver().update(businessUri, values, null, null);
            Log.d(MainActivity.TAG, "saveState: businessId=" + businessId);

            // insert picture if attached
            if (mImageView.getDrawable() != null) {
                insertImage(businessId);
            }
        }
    }

    /* taken from Android Developer documentation */
    private static File getOutputMediaFile(int type) {
        // constructed path: /storage/sdcard/Pictures/MyCameraApp
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");

        // check whether sdcard exists or not
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

    // TODO refactor any image operation to a new class
    /* taken from Android Developer documentation */
    private void setImage() {
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
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
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
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

        /* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
        Log.d(MainActivity.TAG, "set ImageView");
    }

    /* taken from Android Developer documentation */
    private void galleryAddImage() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        // File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void insertImage(int businessId) {
        String imageName = Uri.fromFile(imageFile).getLastPathSegment();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_IMAGE_NAME, imageName);
        values.put(DatabaseHelper.COL_BUSINESS_FK, businessId);
        values.put(DatabaseHelper.COL_IMAGE_UPLOAD_STATUS, DatabaseHelper.NOT_YET);

        getContentResolver().insert(BusinessContentProvider.IMAGE_CONTENT_URI, values);
    }

    /*
     * taken from Stackoverflow:
     * http://stackoverflow.com/questions/2789276/android
     * -get-real-path-by-uri-getpath
     */
    // TODO cursor needs to be closed
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int id = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(id);
        }
    }

    // TODO this method needs to be public
    private ArrayList<String> getImageList(Uri imageUri) {
        String[] projection = { DatabaseHelper.COL_IMAGE_NAME };
        String selection = DatabaseHelper.COL_BUSINESS_FK + "=" + businessId;

        Cursor cursor = getContentResolver().query(imageUri, projection, selection, null, null);

        ArrayList<String> imageList = new ArrayList<String>();
        int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_NAME);
        while (cursor.moveToNext()) {
            imageList.add(cursor.getString(nameIndex));
        }
        cursor.close();
        System.out.println(imageList);
        return imageList;
    }

    private int getNumOfImages() {
        if (imageList == null) {
            return 0;
        }
        return imageList.size();
    }

}