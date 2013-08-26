package com.ciheul.dirbancollector;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ImageListActivity extends ListActivity {

    private Uri imageUri;
    private ArrayList<String> imageList;
    private ListView lvImage;

    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageList = extras.getStringArrayList(BusinessDetailActivity.IMAGE_LIST);
            imageUri = extras.getParcelable(BusinessContentProvider.IMAGE_CONTENT_ITEM_TYPE);
        }

        populateImageList();

    }

    private void populateImageList() {
        lvImage = (ListView) findViewById(android.R.id.list);
        lvImage.setAdapter(new ImageListAdapter(this, android.R.layout.simple_list_item_1, imageList));
    }

    private static class ImageListViewHolder {
        public ImageView image;
        public Button btnDelete;

    }

    private class ImageListAdapter extends ArrayAdapter<String> implements OnClickListener {

        private Context context;
        private ArrayList<String> listItems;

        private ImageListAdapter(Context context, int resource, ArrayList<String> listItems) {
            super(context, resource, listItems);
            this.context = context;
            this.listItems = listItems;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ImageListViewHolder holder;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.image_row, parent, false);

                holder = new ImageListViewHolder();
                holder.image = (ImageView) v.findViewById(R.id.image_row_image);
                holder.btnDelete = (Button) v.findViewById(R.id.image_row_delete);
                v.setTag(holder);
            } else {
                holder = (ImageListViewHolder) v.getTag();
            }

            String imageName = listItems.get(position);
            if (imageName != null) {
                String absolutePath = mediaStorageDir.toString() + "/" + imageName;
                Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                holder.image.setImageBitmap(bitmap);
                holder.btnDelete.setOnClickListener(this);
                holder.btnDelete.setTag(position);                
            }

            return v;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.image_row_delete:
                Integer position = (Integer) view.getTag();
                String imageName = listItems.get(position);
                String selection = DatabaseHelper.COL_IMAGE_NAME + "='" + imageName + "' and "
                        + DatabaseHelper.COL_BUSINESS_PK + "=" + imageUri.getLastPathSegment();
                getContentResolver().delete(imageUri, selection, null);
                
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListView().getAdapter();
                adapter.remove(adapter.getItem(position));
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), imageName + " telah dihapus", Toast.LENGTH_LONG).show();
                break;
            }
        }

    }
}
