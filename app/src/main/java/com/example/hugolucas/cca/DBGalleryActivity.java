package com.example.hugolucas.cca;

import android.graphics.Bitmap;
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
    private RecyclerView.Adapter mDBGalleryAdapter;

    private List<String> mPhotoLabels;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_db_gallery);
        ButterKnife.bind(this);

        mDBGallery.setHasFixedSize(true);
        mDBGallery.setLayoutManager(new LinearLayoutManager(this));

        mDBGalleryAdapter = new DBGalleryAdapter();
        mDBGallery.setAdapter(mDBGalleryAdapter);

        loadLabels();
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
            holder.bindGalleryEntry(labelContents[0], labelContents[1], loadPhotoBitmap(label));
        }

        @Override
        public int getItemCount() {
            return mPhotoLabels.size();
        }

        public Bitmap loadPhotoBitmap(String label){
            return null;
        }
    }

    public class DBGalleryHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.db_gallery_photo) ImageView mDBGalleryPhoto;
        @BindView(R.id.db_gallery_country) TextView mDBGalleryCountry;
        @BindView(R.id.db_gallery_value) TextView mDBGalleryValue;

        public DBGalleryHolder(View itemView) {
            super(itemView);
        }

        public void bindGalleryEntry(String country, String value, Bitmap photo){
            mDBGalleryPhoto.setImageBitmap(photo);
            mDBGalleryCountry.setText(country);
            mDBGalleryValue.setText(value);
        }
    }
}
