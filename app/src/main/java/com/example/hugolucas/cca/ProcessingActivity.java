package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import me.itangqi.waveloadingview.WaveLoadingView;


public class ProcessingActivity extends AppCompatActivity {

    /* Keys for putting extras in an Intent */
    private static final String PATH = "com.example.hugolucas.cca.processing_activity.path";
    private final String TAG = "hugo.ProcessingActivity";

    private String mPhotoPath;
    private Mat mBanknote;

    private ImagePreprocessor mProcessor;
    private Classifier mClassifier;

    private WaveLoadingView mWaveLoadingView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("nonfree");

                    new PreProcessorAsyncTask().execute(mPhotoPath);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /**
     * Creates an intent with the name and path of the photo the user has taken.
     *
     * @param context       the context of the Activity creating this intent
     * @param photoPath     the Android file path to the photo
     * @return              an intent containing the photo path and label
     */
    public static Intent genIntent(Context context, String photoPath){
        Intent intent = new Intent(context, ProcessingActivity.class);
        intent.putExtra(PATH, photoPath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        Intent startingIntent = getIntent();
        mPhotoPath = startingIntent.getStringExtra(PATH);

        mWaveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        updateLoadingIcon("Loading Image Libraries...", 0);
        mWaveLoadingView.startAnimation();

        mProcessor = new ImagePreprocessor();
        mClassifier = new Classifier(this.getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void updateLoadingIcon(String message, int progress){
        mWaveLoadingView.setCenterTitle(message);
        mWaveLoadingView.setProgressValue(progress);
    }

    /**
     * Displays the Mat image loaded by the application and pre-processed by the ImagePreprocessor.
     * Used for debugging, do not processed display image directly to User.
     *
     * @param image     a Mat of the image taken by the User for classification
     */
    private void displayMat(Mat image, int id){
        Bitmap imageBitMap = Bitmap.createBitmap(image.cols(), image.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, imageBitMap);

        ImageView view = (ImageView) findViewById(id);
        view.setImageBitmap(imageBitMap);

    }

    private class PreProcessorAsyncTask extends AsyncTask<String, Integer, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(TAG, "PP pre-execute");
            updateLoadingIcon("Starting Pre-processing...", 20);
        }

        @Override
        protected Void doInBackground(String... strings) {
            Log.v(TAG, "PP running");
            String path = strings[0];
            mBanknote = mProcessor.preprocessImage(path);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v(TAG, "PP complete");
            updateLoadingIcon("Pre-processing complete...", 40);
            new ClassifierAsyncTask().execute();
        }
    }

    private class ClassifierAsyncTask extends AsyncTask<Void, Void, Void>{

        private String mClassification;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(TAG, "C pre-execute");
            updateLoadingIcon("Starting Classification...", 60);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.v(TAG, "C running");
            mClassification = mClassifier.classify(mBanknote);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v(TAG, "C complete");
            updateLoadingIcon("Classification Complete..", 80);
            Toast.makeText(getApplicationContext(), mClassification, Toast.LENGTH_LONG).show();
        }
    }
}
