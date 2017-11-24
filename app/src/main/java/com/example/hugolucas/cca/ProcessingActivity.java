package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import me.itangqi.waveloadingview.WaveLoadingView;


public class ProcessingActivity extends AppCompatActivity {

    /* Keys for putting extras in an Intent */
    private static final String PATH = "com.example.hugolucas.cca.processing_activity.path";
    private static final String EXTRACT = "com.example.hugolucas.cca.processing_activity.extract";
    private final String TAG = "hugo.ProcessingActivity";

    private boolean mFullyProcess;
    private String mPhotoPath;
    private Mat mBanknote;

    private String [] mBanknoteClassification;
    private ImagePreprocessor mProcessor;
    private Classifier mClassifier;

    private WaveLoadingView mWaveLoadingView;

    /**
     * Ensures that the OpenCV library is loaded before any code using any OpenCV methods is
     * called. Once the OpenCV library has been loaded successfully, an AsyncTask starting the
     * image pre-processing is started. This ensures the loading icon can be updated while the
     * ImagePreprocessor is running.
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("opencv_java");
                    System.loadLibrary("nonfree");

                    if (mFullyProcess)
                        new PreProcessorAsyncTask().execute(mPhotoPath);
                    else
                        new ExtractionAsyncTask().execute(mPhotoPath);
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
     * @param context           the context of the Activity creating this intent
     * @param photoPath         the Android file path to the photo
     * @param fullProcessing    whether to full-process the image or just extract the largest
     *                          contour
     * @return                  an intent containing the photo path and label
     */
    public static Intent genIntent(Context context, String photoPath, boolean fullProcessing){
        Intent intent = new Intent(context, ProcessingActivity.class);
        intent.putExtra(PATH, photoPath);
        intent.putExtra(EXTRACT, fullProcessing);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        Intent startingIntent = getIntent();
        mPhotoPath = startingIntent.getStringExtra(PATH);
        mFullyProcess = startingIntent.getBooleanExtra(EXTRACT, true);

        mWaveLoadingView = (WaveLoadingView) findViewById(R.id.waveLoadingView);
        updateLoadingIcon("Loading Image Libraries...", 0);
        mWaveLoadingView.setAmplitudeRatio(10);
        mWaveLoadingView.startAnimation();
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

    /**
     * Updates the loading icon to a specific progress value and with a specific message.
     *
     * @param message       the String message to display to the User indicating progress
     * @param progress      a value out of 100 indicating progress
     */
    public void updateLoadingIcon(String message, int progress){
        mWaveLoadingView.setCenterTitle(message);
        mWaveLoadingView.setProgressValue(progress);
    }

    /**
     * Displays the Mat image loaded by the application and pre-processed by the ImagePreprocessor.
     * Used for debugging, do not display image directly to User.
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

    /**
     * Returns classification result to calling activity if no errors were detected.
     *
     * @param error     true if error, false otherwise
     */
    public void returnResult(boolean error){
        if (!error) {
            Intent resultIntent = CameraActivity.buildProcessingResultIntent(
                    mBanknoteClassification[Classifier.mCurrencyCodeIndex],
                    mBanknoteClassification[Classifier.mCurrencyValueIndex],
                    mPhotoPath
            );
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    /**
     * Returns a new banknote to add to the User DB if no errors were detected.
     *
     * @param error     true if error, false otherwise.
     */
    public void returnDBResult(boolean error){
        if (!error){

        }
    }

    /**
     * Class handles the extraction of a rectangular banknote from an image. This task will only be
     * used when the User defines a new banknote to add to their own personal DB. Extraction is
     * required in this case in order to speed up feature extraction.
     */
    private class ExtractionAsyncTask extends AsyncTask<String, Integer, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateLoadingIcon("Processing New Image...", 50);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String path = strings[0];
            mProcessor = new ImagePreprocessor();
            mBanknote = mProcessor.loadAndExtractImage(path);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateLoadingIcon("Banknote found...", 100);

        }
    }

    /**
     * AsyncTask used to run the ImagePreprocessor. When complete calls the Classifier.
     */
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
            mProcessor = new ImagePreprocessor();
            mClassifier = new Classifier(getApplicationContext());
            mBanknote = mProcessor.preprocessImage(path);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v(TAG, "PP complete");
            updateLoadingIcon("Pre-processing complete...", 25);
            mWaveLoadingView.setAmplitudeRatio(20);
            new ClassifierAsyncTask().execute();
        }
    }

    /**
     * AsyncTask used to run the Classifier. When complete calls method that starts the
     * ResultsActivity.
     */
    private class ClassifierAsyncTask extends AsyncTask<Void, Integer, Void>{

        private int START = 30;
        private int PROC_JUMP = 20;
        private int WIDTH = 49;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(TAG, "C pre-execute");
            updateLoadingIcon("Starting Classification...", START);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.v(TAG, "C running");
            int currentProgress = START;
            int progressStep = mClassifier.calculateStep(WIDTH);

            publishProgress(currentProgress);
            mClassifier.extractImageFeatures(mBanknote);
            publishProgress(currentProgress += PROC_JUMP);

            while(!mClassifier.comparisonComplete()){
                mClassifier.processFile();
                currentProgress += progressStep;

                publishProgress(currentProgress);
            }
            mBanknoteClassification = mClassifier.getResults();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v(TAG, "C complete");
            updateLoadingIcon("Classification Complete..", 100);
            returnResult(false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int updateValue = values[0];
            if(updateValue == 30){
                updateLoadingIcon("Extracting features....", updateValue);
            }else if (updateValue == 50) {
                updateLoadingIcon("Features extracted...", updateValue);
            }else{
                updateLoadingIcon("Classifying...", updateValue);
            }
        }
    }
}
