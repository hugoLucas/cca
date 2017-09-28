package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ClassificationActivity extends AppCompatActivity {

    /* Keys for putting extras in an Intent */
    private static final String PATH = "com.example.hugolucas.cca.classification_activity.path";
    private static final String LABEL = "com.example.hugolucas.cca.classification_activity.label";

    /**
     * Creates an intent with the name and path of the photo the user has taken.
     *
     * @param context       the context of the Activity creating this intent
     * @param photoPath     the Android file path to the photo
     * @param photoLabel    the file name of the photo
     * @return              an intent containing the photo path and label
     */
    public static Intent genIntent(Context context, String photoPath, String photoLabel){
        Intent intent = new Intent(context, ClassificationActivity.class);
        intent.putExtra(PATH, photoPath);
        intent.putExtra(LABEL, photoLabel);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
    }
}
