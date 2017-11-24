package com.example.hugolucas.cca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity allows the User to look through their custom image library and remove any pictures
 * they desire.
 *
 * Created by hugolucas on 11/23/17.
 */

public class DBGalleryActivity extends AppCompatActivity {

    private static final String TAG = "DBGalleryActivity";

    @BindView(R.id.db_gallery_recycler_view) RecyclerView mDBGallery;
    @BindView(R.id.db_gallery_no_images) ImageView mDBGalleryImageView;
    private RecyclerView.Adapter mDBGalleryAdapter;

    private List<String> mPhotoLabels;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_db_gallery);
        ButterKnife.bind(this);

        loadLabels();

        if (mPhotoLabels.size() > 0) {
            mDBGallery.setHasFixedSize(true);
            mDBGallery.setLayoutManager(new LinearLayoutManager(this));

            mDBGalleryAdapter = new DBGalleryAdapter();
            mDBGallery.setAdapter(mDBGalleryAdapter);
        }else{
            mDBGallery.setVisibility(View.INVISIBLE);
            mDBGalleryImageView.setVisibility(View.VISIBLE);
        }
    }

    private void loadLabels(){
        mPhotoLabels = new ArrayList<>();

        File storageDirectory = new File(getFilesDir() + "/DB");
        if (!storageDirectory.exists())
            Log.v(TAG, "Directory does not exist");
        else
            Collections.addAll(mPhotoLabels, storageDirectory.list());

        Log.v(TAG, "DB contains: " + mPhotoLabels.size() + " files.");
    }

    private class DBGalleryAdapter extends RecyclerView.Adapter<DBGalleryHolder>{

        @Override
        public DBGalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_db_gallery,
                    parent, false);
            return new DBGalleryHolder(view);
        }

        public void onBindViewHolder(DBGalleryHolder holder, int position) {
            String label = mPhotoLabels.get(position);
            String [] labelContents = label.split("_");
            String [] values = labelContents[1].split("\\.");

            holder.bindGalleryEntry(labelContents[0], values[0], loadPhotoBitmap(label));
        }

        @Override
        public int getItemCount() {
            return mPhotoLabels.size();
        }

        private Bitmap loadPhotoBitmap(String label){
            File storageDirectory = new File(getFilesDir() + "/DB");
            String filePath = storageDirectory.getPath() + "/" + label;

            Log.v(TAG, "Loading image: " + filePath);

            try{
                FileInputStream bitmapStream = new FileInputStream(new File(filePath));
                return BitmapFactory.decodeStream(bitmapStream);
            }catch (FileNotFoundException e){
                return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_img_error);
            }
        }
    }

    public class DBGalleryHolder extends RecyclerView.ViewHolder{

        private ImageView mDBGalleryPhoto;
        private TextView mDBGalleryCountry;
        private TextView mDBGalleryValue;

        public DBGalleryHolder(View itemView) {
            super(itemView);

            mDBGalleryPhoto = itemView.findViewById(R.id.db_gallery_photo);
            mDBGalleryCountry = itemView.findViewById(R.id.db_gallery_country);
            mDBGalleryValue = itemView.findViewById(R.id.db_gallery_value);
        }

        public void bindGalleryEntry(String country, String value, Bitmap photo){
            mDBGalleryPhoto.setImageBitmap(photo);
            mDBGalleryCountry.setText(country);
            mDBGalleryValue.setText(value);
        }
    }
}
