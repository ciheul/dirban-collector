package com.ciheul.dirbancollector;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BusinessDetailActivity extends Activity {
    private EditText et_name;
    private EditText et_address;
    private Button button_submit;

    private final static String CONTRIBUTOR = "masjat";

    private Uri business_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_detail);

        et_name = (EditText) findViewById(R.id.business_detail_et_name);
        et_address = (EditText) findViewById(R.id.business_detail_et_address);

        button_submit = (Button) findViewById(R.id.business_detail_btn_submit);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(et_name.getText().toString())
                        && TextUtils.isEmpty(et_address.getText().toString())) {
                    Toast.makeText(BusinessDetailActivity.this, "Lengkapi isian yang kosong.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BusinessDetailActivity.this, "Berhasil ditambahkan.",
                            Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            business_uri = extras.getParcelable(BusinessContentProvider.CONTENT_ITEM_TYPE);
            populate_business_detail(business_uri);
        }
    }

    private void populate_business_detail(Uri uri) {
        String[] projection = { DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            et_name.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
            et_address.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
        }
        cursor.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
    }

    private void saveState() {
        String name = et_name.getText().toString();
        String address = et_address.getText().toString();
        double longitude = 0.0;
        double latitude = 0.0;

        if (name.length() == 0 && address.length() == 0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, name);
        values.put(DatabaseHelper.COL_ADDRESS, address);
        values.put(DatabaseHelper.COL_LON, longitude);
        values.put(DatabaseHelper.COL_LAT, latitude);
        values.put(DatabaseHelper.COL_CONTRIBUTOR, CONTRIBUTOR);

        getContentResolver().insert(BusinessContentProvider.CONTENT_URI, values);
    }

}
