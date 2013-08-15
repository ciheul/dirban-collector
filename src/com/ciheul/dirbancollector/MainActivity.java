package com.ciheul.dirbancollector;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_list);
        getLoaderManager().initLoader(0, null, this);
        populate_list();
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

    private void populate_list() {
        String[] from = new String[] { DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };
        int[] to = new int[] { R.id.business_name, R.id.business_address };

        adapter = new SimpleCursorAdapter(this, R.layout.business_row, null, from, to, 0);
        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { DatabaseHelper.COL_BUSINESS_ID, DatabaseHelper.COL_NAME, DatabaseHelper.COL_ADDRESS };
        CursorLoader cursor_loader = new CursorLoader(this, BusinessContentProvider.CONTENT_URI,
                projection, null, null, null);
        return cursor_loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
