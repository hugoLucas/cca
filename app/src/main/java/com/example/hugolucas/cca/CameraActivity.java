package com.example.hugolucas.cca;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.CameraFragmentApi;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentControlsAdapter;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultAdapter;
import com.github.florent37.camerafragment.listeners.CameraFragmentStateAdapter;
import com.github.florent37.camerafragment.widgets.FlashSwitchView;
import com.github.florent37.camerafragment.widgets.RecordButton;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    public static final String FRAGMENT_TAG = "camera";

    @BindView(R.id.flash_switch_button)
    FlashSwitchView mFlashSwitchButton;

    @BindView(R.id.keep_photo_checkbox)
    CheckBox mKeepPhotoCheckBox;

    @BindView(R.id.record_button)
    RecordButton mTakePictureButton;

    @BindView(R.id.camera_button_layout)
    View mCameraLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Ensures the camera preview is not cutoff at the top */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        genPhotoStoragePath();
        buildCamera();
    }

    /**
     * Listens for the selection of the record button. Once the button is selected a picture of a
     * bill should be taken and passed to the classifier. If mKeepPhotoCheckBox is selected the
     * picture will be saved to the device's memory.
     */
    @OnClick(R.id.record_button)
    public void onRecordButtonClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();

        if (cameraFragment != null) {
            cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultAdapter() {
               @Override
               public void onVideoRecorded(String filePath) {
                   /* Video recordings are disabled for this application */
               }

               @Override
               public void onPhotoTaken(byte[] bytes, String filePath) {
                   Toast.makeText(getBaseContext(), "onPhotoTaken " + filePath, Toast.LENGTH_SHORT).show();
               }
           },
                    genPhotoStoragePath(), genPhotoLabel());
        }
    }

    public void buildCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        addCamera();
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void addCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Camera Permissions not granted.");
        } else {
            final CameraFragment cameraFragment = CameraFragment.newInstance(new Configuration.Builder()
                    .setCamera(Configuration.CAMERA_FACE_REAR).build());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.camera_preview, cameraFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();

            if (cameraFragment != null){
                Log.v(TAG, "Camera accessed, mounting preview.");
                mCameraLayout.setVisibility(View.VISIBLE);

                cameraFragment.setStateListener(new CameraFragmentStateAdapter() {

                    @Override
                    public void onCurrentCameraBack() {
                        /* No camera change possible, method not needed */
                    }

                    @Override
                    public void onCurrentCameraFront() {
                        /* No camera change possible, method not needed */
                    }

                    @Override
                    public void onFlashAuto() {
                        mFlashSwitchButton.displayFlashAuto();
                    }

                    @Override
                    public void onFlashOn() {
                        mFlashSwitchButton.displayFlashOn();
                    }

                    @Override
                    public void onFlashOff() {
                        mFlashSwitchButton.displayFlashOff();
                    }

                    @Override
                    public void onCameraSetupForPhoto() {
                        mTakePictureButton.displayPhotoState();
                        mFlashSwitchButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCameraSetupForVideo() {
                        /* Video recording disabled, method not needed */
                    }

                    @Override
                    public void shouldRotateControls(int degrees) {
                        ViewCompat.setRotation(mFlashSwitchButton, degrees);
                        ViewCompat.setRotation(mKeepPhotoCheckBox, degrees);
                    }

                    @Override
                    public void onRecordStateVideoReadyForRecord() {
                        /* Video recording disabled, method not needed */
                    }

                    @Override
                    public void onRecordStateVideoInProgress() {
                        /* Video recording disabled, method not needed */
                    }

                    @Override
                    public void onRecordStatePhoto() {
                        mTakePictureButton.displayPhotoState();
                    }

                    @Override
                    public void onStopVideoRecord() {
                        /* Video recording disabled, method not needed */
                    }

                    @Override
                    public void onStartVideoRecord(File outputFile) {
                        /* Video recording disabled, method not needed */
                    }
                });

                cameraFragment.setControlsListener(new CameraFragmentControlsAdapter() {

                    @Override
                    public void lockControls() {
                        mTakePictureButton.setEnabled(false);
                        mFlashSwitchButton.setEnabled(false);
                    }

                    @Override
                    public void unLockControls() {
                        mTakePictureButton.setEnabled(true);
                        mFlashSwitchButton.setEnabled(true);
                    }

                    @Override
                    public void allowCameraSwitching(boolean allow) {
                    }

                    @Override
                    public void allowRecord(boolean allow) {
                        mTakePictureButton.setEnabled(allow);
                    }

                    @Override
                    public void setMediaActionSwitchVisible(boolean visible) {
                    }
                });
            }
        }
    }

    /**
     * Returns reference to CameraFragment if it exists.
     *
     * @return CameraFragmentApi if mounted
     */
    private CameraFragmentApi getCameraFragment() {
        return (CameraFragmentApi) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    /**
     * Creates a unique label for each picture taken by the application by appending a timestamp
     * to each picture taken.
     *
     * @return a String label for the picture taken
     */
    private String genPhotoLabel(){
        return "photo_" + DateFormat.getDateTimeInstance().format(new java.util.Date());
    }

    /**
     * Returns a valid path to store photo taken for future processing.
     *
     * @return path of file save location
     */
    private String genPhotoStoragePath(){
        if(canFullyAccessExternalStorage()){
            File dir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
            if (dir != null) {
                Log.v(TAG, "Saving to external directory: " + dir.getAbsolutePath());
                return dir.getAbsolutePath();
            }
            else
                Log.v(TAG, "Unable to find external save directory.");
        }
        else
            Log.v(TAG, "Unable to read and write to external storage.");
        return null;
    }

    /**
     * Determines if application can read and write to external storage. Taken from the Android
     * documentation at:
     *
     * https://developer.android.com/guide/topics/data/data-storage.html#filesExternal
     *
     * @return true if application can READ and WRITE to external storage, false otherwise
     */
    private boolean canFullyAccessExternalStorage(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
