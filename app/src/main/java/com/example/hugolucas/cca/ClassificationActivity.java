package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class ClassificationActivity extends AppCompatActivity {

    /* Keys for putting extras in an Intent */
    private static final String PATH = "com.example.hugolucas.cca.classification_activity.path";
    private final String TAG = "ClassificationActivity";
    private String mPhotoPath;

    @BindView(R.id.test_image_view)
    ImageView frame;

    private ImageView testView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    loadImage();
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
        Intent intent = new Intent(context, ClassificationActivity.class);
        intent.putExtra(PATH, photoPath);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
        ButterKnife.bind(this);

        Intent startingIntent = getIntent();
        mPhotoPath = startingIntent.getStringExtra(PATH);

        testView = (ImageView) findViewById(R.id.test_image_view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void loadImage(){
        Mat image = imread(mPhotoPath);
        Bitmap imageBitMap = Bitmap.createBitmap(image.cols(), image.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, imageBitMap);

        frame.setImageBitmap(imageBitMap);
//        testView.setImageBitmap(imageBitMap);
    }
}
