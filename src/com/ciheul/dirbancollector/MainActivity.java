package com.ciheul.dirbancollector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
                            uploadData();
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

    private void uploadData() {

    }

    private void deleteData() {

    }

}