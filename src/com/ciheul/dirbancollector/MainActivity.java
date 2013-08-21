package com.ciheul.dirbancollector;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "dirban";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_list);        
        populate_list();
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
        case R.id.add:
            Intent i = new Intent(this, BusinessDetailActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(MainActivity.TAG, "an item is clicked");
        Intent i = new Intent(this, BusinessDetailActivity.class);
        Uri business_uri = Uri.parse(BusinessContentProvider.CONTENT_URI + "/" + id);
        i.putExtra(BusinessContentProvider.CONTENT_ITEM_TYPE, business_uri);
        startActivity(i);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {    
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.d(TAG, "create context menu");
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case DELETE_ID:
            Log.d(TAG, "context selected: delete_id");
            AdapterContextMenuInfo context_info = (AdapterContextMenuInfo) item.getMenuInfo();
            Uri uri = Uri.parse(BusinessContentProvider.CONTENT_URI + "/" + context_info.id);
            getContentResolver().delete(uri, null, null);
            populate_list();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void populate_list() {
        Log.d(TAG, "populate_list");
        
        String[] from = new String[] { DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };
        int[] to = new int[] { R.id.business_name, R.id.business_address };
        
        getLoaderManager().initLoader(0, null, this);
        Log.d(TAG, "populate_list: getLoader");
        
        adapter = new SimpleCursorAdapter(this, R.layout.business_row, null, from, to, 0);
        setListAdapter(adapter);
        Log.d(TAG, "populate_list: setListAdapter");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "create loader");
        String[] projection = { DatabaseHelper.COL_BUSINESS_ID, DatabaseHelper.COL_NAME,
                DatabaseHelper.COL_ADDRESS };
        CursorLoader cursor_loader = new CursorLoader(this, BusinessContentProvider.CONTENT_URI,
                projection, null, null, null);
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
}
