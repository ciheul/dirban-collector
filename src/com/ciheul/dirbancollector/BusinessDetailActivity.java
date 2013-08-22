package com.ciheul.dirbancollector;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ciheul.dirbancollector.lib.Geolocator;

public class BusinessDetailActivity extends Activity implements OnClickListener {
    private EditText et_name;
    private EditText et_address;
    private Button button_submit;
    private Button button_cancel;
    private Button button_location;
    private TextView tv_longitude;
    private TextView tv_latitude;

    private final static String CONTRIBUTOR = "masjat";

    private Uri business_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_detail);

        et_name = (EditText) findViewById(R.id.business_detail_et_name);
        et_address = (EditText) findViewById(R.id.business_detail_et_address);

        tv_longitude = (TextView) findViewById(R.id.business_detail_tv_longitude);
        tv_latitude = (TextView) findViewById(R.id.business_detail_tv_latitude);
        
        button_location = (Button) findViewById(R.id.business_detail_btn_location);
        button_submit = (Button) findViewById(R.id.business_detail_btn_submit);
        button_cancel = (Button) findViewById(R.id.business_detail_btn_cancel);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            button_submit.setText(R.string.business_detail_btn_update);
        }

        button_location.setOnClickListener(this);
        button_submit.setOnClickListener(this);
        button_cancel.setOnClickListener(this);

        business_uri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                .getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);

        if (extras != null) {
            business_uri = extras.getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);
            populate_business_detail(business_uri);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.business_detail_btn_submit:
            if (TextUtils.isEmpty(et_name.getText().toString())
                    || TextUtils.isEmpty(et_address.getText().toString())) {
                Toast.makeText(BusinessDetailActivity.this,
                        getResources().getString(R.string.business_detail_empty_form),
                        Toast.LENGTH_LONG).show();
            } else {
                if (business_uri == null) {
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
        case R.id.business_detail_btn_cancel:
            Toast.makeText(BusinessDetailActivity.this,
                    getResources().getString(R.string.business_detail_cancel), Toast.LENGTH_LONG)
                    .show();
            finish();
            break;
        case R.id.business_detail_btn_location:
            Geolocator gps = new Geolocator(this);
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String geolocation = "Lon=" + Double.toString(longitude) + " & Lat=" + Double.toString(latitude);
            Toast.makeText(BusinessDetailActivity.this, geolocation, Toast.LENGTH_LONG)
                    .show();
            tv_longitude.setText(Double.toString(longitude));
            tv_latitude.setText(Double.toString(latitude));
            break;
        }
    }

    // disable back button
    @Override
    public void onBackPressed() {
    }

    private void populate_business_detail(Uri uri) {
        String[] projection = { DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            et_name.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
            et_address.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
        }
        cursor.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle out_state) {
        super.onSaveInstanceState(out_state);
        saveState();
        out_state.putParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE, business_uri);
    }

    private void saveState() {
        String name = et_name.getText().toString();
        String address = et_address.getText().toString();
        
        double longitude = Double.parseDouble(tv_longitude.getText().toString());
        double latitude = Double.parseDouble(tv_latitude.getText().toString());;

        if (name.length() == 0 && address.length() == 0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, name);
        values.put(DatabaseHelper.COL_ADDRESS, address);
        values.put(DatabaseHelper.COL_LON, longitude);
        values.put(DatabaseHelper.COL_LAT, latitude);
        values.put(DatabaseHelper.COL_CONTRIBUTOR, CONTRIBUTOR);

        if (business_uri == null) {
            getContentResolver().insert(BusinessContentProvider.CONTENT_URI, values);
        } else {
            getContentResolver().update(business_uri, values, null, null);
        }
    }
}
