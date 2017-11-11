package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hugolucas on 11/10/17.
 */

public class ImageAdditionActivity extends AppCompatActivity {

    private static String PATH = "photoPath";

    private String mPhotoPath;

    @BindView(R.id.addition_banknote_preview)
    ImageView mBanknotePreview;

    @BindView(R.id.addition_country_selection)
    Spinner mCountrySpinner;

    /**
     * Creates an intent with the name and path of the photo the user has taken.
     *
     * @param context       the context of the Activity creating this intent
     * @param photoPath     the Android file path to the photo
     * @return              an intent containing the photo path and label
     */
    public static Intent genIntent(Context context, String photoPath){
        Intent intent = new Intent(context, ImageAdditionActivity.class);
        intent.putExtra(PATH, photoPath);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_addition);
        ButterKnife.bind(this);

        Intent startingIntent = getIntent();
        mPhotoPath = startingIntent.getStringExtra(PATH);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dialog_currencies, R.layout.spinner_text_view);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountrySpinner.setAdapter(adapter);
    }
}
