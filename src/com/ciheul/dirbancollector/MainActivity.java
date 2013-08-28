package com.ciheul.dirbancollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "dirban";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private SimpleCursorAdapter adapter;

    private File mediaStorageDir = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

    private String URL = "http://192.168.1.101:8000/dirban/1.0/business/";
//    private String URL = "http://10.0.2.2:8000/dirban/1.0/business";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_list);
        populateList();
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add "Tambah Baru" button on the top bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.actionbar_add:
            Intent i = new Intent(this, BusinessDetailActivity.class);
            startActivity(i);
            return true;
        case R.id.actionbar_upload:
            AlertDialog.Builder uploadDialog = new AlertDialog.Builder(this);

            uploadDialog.setMessage(R.string.actionbar_upload_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadData(BusinessContentProvider.BUSINESS_CONTENT_URI);
                            Toast.makeText(getApplicationContext(), "Unggah data", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // remove is dismissed
                            dialog.dismiss();
                        }
                    });

            uploadDialog.create().show();
            break;
        case R.id.actionbar_delete:
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);

            deleteDialog.setMessage(R.string.actionbar_delete_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteData();
                            // tell user
                            Toast.makeText(getApplicationContext(), "Hapus data", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // remove is dismissed
                            dialog.dismiss();
                        }
                    });

            deleteDialog.create().show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(MainActivity.TAG, "an item is clicked");
        Intent i = new Intent(this, BusinessDetailActivity.class);
        Uri businessUri = Uri.parse(BusinessContentProvider.BUSINESS_CONTENT_URI + "/" + id);
        i.putExtra(BusinessContentProvider.BUSINESS_CONTENT_ITEM_TYPE, businessUri);
        startActivity(i);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case DELETE_ID:
            Log.d(TAG, "context selected: delete_id");
            AdapterContextMenuInfo context_info = (AdapterContextMenuInfo) item.getMenuInfo();
            Uri uri = Uri.parse(BusinessContentProvider.BUSINESS_CONTENT_URI + "/" + context_info.id);
            getContentResolver().delete(uri, null, null);
            populateList();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "create loader");
        String[] projection = { DatabaseHelper.COL_BUSINESS_ID, DatabaseHelper.COL_BUSINESS_NAME,
                DatabaseHelper.COL_ADDRESS, DatabaseHelper.COL_BUSINESS_TYPE, DatabaseHelper.COL_LON,
                DatabaseHelper.COL_LAT, DatabaseHelper.COL_BUSINESS_UPLOAD_STATUS };
        CursorLoader cursor_loader = new CursorLoader(this, BusinessContentProvider.BUSINESS_CONTENT_URI, projection,
                null, null, null);
        return cursor_loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "loader finish");
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "loader reset");
        adapter.swapCursor(null);
    }

    // disable back button
    @Override
    public void onBackPressed() {
    }

    private void populateList() {
        // Log.d(TAG, "populateList");

        String[] from = new String[] { DatabaseHelper.COL_BUSINESS_NAME, DatabaseHelper.COL_ADDRESS,
                DatabaseHelper.COL_BUSINESS_TYPE, DatabaseHelper.COL_LON, DatabaseHelper.COL_LAT,
                DatabaseHelper.COL_BUSINESS_UPLOAD_STATUS };
        int[] to = new int[] { R.id.business_row_name, R.id.business_row_address, R.id.business_row_business_type,
                R.id.business_row_longitude, R.id.business_row_latitude, R.id.business_row_upload_status };

        getLoaderManager().initLoader(0, null, this);
        // Log.d(TAG, "populateList: getLoader");

        adapter = new SimpleCursorAdapter(this, R.layout.business_row, null, from, to, 0);
        setListAdapter(adapter);
        // Log.d(TAG, "populateList: setListAdapter");
    }

    private void uploadData(Uri uri) {
        String[] projection = { DatabaseHelper.COL_BUSINESS_ID, DatabaseHelper.COL_BUSINESS_NAME,
                DatabaseHelper.COL_ADDRESS, DatabaseHelper.COL_BUSINESS_TYPE, DatabaseHelper.COL_LON,
                DatabaseHelper.COL_LAT };

        // query records which have not been uploaded
        String selection = DatabaseHelper.COL_BUSINESS_UPLOAD_STATUS + "=" + DatabaseHelper.NOT_YET;

        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    JSONObject businessDetail = new JSONObject();

                    businessDetail.put(DatabaseHelper.COL_BUSINESS_NAME,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUSINESS_NAME)));
                    businessDetail.put(DatabaseHelper.COL_ADDRESS,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
                    businessDetail.put(DatabaseHelper.COL_BUSINESS_TYPE,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUSINESS_TYPE)));

                    // construct coordinates's value: [lon, lat]
                    double longitude = (double) cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LON));
                    double latitude = (double) cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT));
                    List<Double> lonLat = new ArrayList<Double>();
                    lonLat.add(longitude);
                    lonLat.add(latitude);
                    businessDetail.put(DatabaseHelper.COORDINATES, new JSONArray(lonLat));

                    // get image names
                    int businessId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUSINESS_ID));
                    Uri imageUri = Uri.parse(BusinessContentProvider.IMAGE_CONTENT_URI + "/" + businessId);
                    ArrayList<String> imageList = new ArrayList<String>();
                    imageList = getImageList(imageUri, businessId);

                    // construct an array of base64 string of images
                    if (imageList.size() != 0) {
                        List<String> b64images = new ArrayList<String>();
                        for (String imageName : imageList) {
                            String absolutePath = mediaStorageDir.toString() + "/" + imageName;
                            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                            String b64image = b64encodeImage(bitmap);
                            b64images.add(b64image);
                        }
                        businessDetail.put(DatabaseHelper.IMAGES, new JSONArray(b64images));
                    } else {
                        businessDetail.put(DatabaseHelper.IMAGES, new JSONArray());
                    }

                    businessDetail.put(DatabaseHelper.COL_CONTRIBUTOR, DatabaseHelper.CONTRIBUTOR);
                    Log.d(MainActivity.TAG, businessDetail.toString());
                    sendHttpRequest(businessDetail);
                } catch (JSONException e) {

                }
            }
        }

        cursor.close();
    }

    private void deleteData() {

    }

    // TODO refactor it's a general method
    private ArrayList<String> getImageList(Uri imageUri, int businessId) {
        String[] projection = { DatabaseHelper.COL_IMAGE_NAME };
        String selection = DatabaseHelper.COL_BUSINESS_PK + "=" + businessId;

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

    // TODO refactor it's a general method
    private String b64encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void sendHttpRequest(JSONObject data) {
        final JSONObject jsonObject = data;
        new Thread(new Runnable() {

            @Override
            public void run() {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(URL);
                try {
                    StringEntity entity = new StringEntity(jsonObject.toString());
                    httpPost.setEntity(entity);
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setHeader("Accept-Encoding", "gzip");

                    HttpResponse httpResponse = (HttpResponse) httpClient.execute(httpPost);
                    Log.d(MainActivity.TAG, String.valueOf(httpResponse.getStatusLine().getStatusCode()));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }).start();

    }
}