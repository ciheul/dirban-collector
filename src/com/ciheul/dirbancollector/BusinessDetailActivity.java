package com.ciheul.dirbancollector;

import android.app.Activity;
import android.content.ContentValues;
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
