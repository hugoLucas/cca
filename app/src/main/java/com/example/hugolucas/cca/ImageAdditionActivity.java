package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This activity allows the User add a new banknote and its information to a separate database
 * in order to allow the User to define new banknotes to classify against. This feature is an
 * addition requested by the Professor of my class.
 *
 * Created by hugolucas on 11/10/17.
 */

public class ImageAdditionActivity extends AppCompatActivity {

    private static final String PATH = "photoPath";

    private static final int PREPROC = 100;

    private String mPhotoPath;
    private String mCountrySelected;
    private int mCurrencyValueSelected;

    @BindView(R.id.addition_banknote_preview)
    ImageView mBanknotePreview;

    @BindView(R.id.addition_country_selection)
    Spinner mCountrySpinner;

    @BindView(R.id.addition_number_picker)
    NumberPicker mValuePicker;

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

        Bitmap mPhotoBitmap = BitmapFactory.decodeFile(mPhotoPath);
        mBanknotePreview.setImageBitmap(mPhotoBitmap);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dialog_currencies, R.layout.spinner_text_view);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountrySpinner.setAdapter(adapter);

        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCountrySelected = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /* Do nothing */
            }
        });

        mCurrencyValueSelected = mValuePicker.getValue();
        mValuePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mCurrencyValueSelected = newVal;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PREPROC:
                if (resultCode != RESULT_OK)
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                finish();
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.addition_button)
    public void extractBanknoteFromImage(){
        if (mCurrencyValueSelected > 1 && mCountrySelected != null) {
            String fileName = mCountrySelected + "_" + mCurrencyValueSelected + ".jpg";
            startActivityForResult(ProcessingActivity.genIntent(this, mPhotoPath, false, fileName),
                    PREPROC);
        }
    }
}
