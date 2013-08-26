package com.ciheul.dirbancollector;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class ImageListActivity extends ListActivity {

    ArrayList<String> imageList;
    ListView lvImage;
    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_list);
        
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageList = extras.getStringArrayList(BusinessDetailActivity.IMAGE_LIST);
        }
        
        populateImageList();        
    }

    private void populateImageList() {
        lvImage = (ListView) findViewById(android.R.id.list);
        lvImage.setAdapter(new ImageListAdapter(this, android.R.layout.simple_list_item_1, imageList));
    }
    
    private static class ImageListViewHolder {
        public ImageView image;
    }
    
    private class ImageListAdapter extends ArrayAdapter<String> {
        
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
                v.setTag(holder);
            } else {
                holder = (ImageListViewHolder) v.getTag();
            }
            
            String imageName = listItems.get(position);
            if (imageName != null) {                
                String absolutePath = mediaStorageDir.toString() + "/" + imageName;
                Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                holder.image.setImageBitmap(bitmap);
            }
            
            return v;
        }
        
           
    }
}
