package com.example.hugolucas.cca;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "hugo.CameraActivity";
    private static final String FRAGMENT_TAG = "CAMERA";

    private static final String RESULT_CURRENCY_CODE = "camera_activity_code";
    private static final String RESULT_CURRENCY_VALUE = "camera_activity_value";
    private static final String RESULT_CURRENCY_PIC = "camera_activity_picture";

    private static final int CAMERA_PERMISSION = 0;
    private static final int WRITE_PERMISSION = 1;
    private static final int READ_PERMISSION = 2;

    private static final int PROC_REQ_CODE = 1;

    private static boolean displayArray [] = new boolean[3];

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
        requestReadWritePermissions();
        buildCamera();
    }

    /**
     * Requests permissions to read and write to external memory. Needed to save and load images
     * of currencies.
     */
    public void requestReadWritePermissions(){
        final String writePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String readPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        int writeCheck = ActivityCompat.checkSelfPermission(this, writePermission);
        int readCheck = ActivityCompat.checkSelfPermission(this, readPermission);

        if (writeCheck != PackageManager.PERMISSION_GRANTED){
            Log.v(TAG, "External Memory Read Permission not yet granted.");
            askForPermission("External Memory Write", writePermission,
                    "Need to save images to external memory", WRITE_PERMISSION, this);
        }
        if (readCheck != PackageManager.PERMISSION_GRANTED){
            Log.v(TAG, "External Memory Write Permission not yet granted.");
            askForPermission("External Memory Read", readPermission,
                    "Need to read images from external memory", READ_PERMISSION, this);
        }
    }

    /**
     * Helper method to request and respond to a user permission.
     *
     * @param pName                 the label of the permission to display to the user
     * @param pCode                 the manifest value of the permission
     * @param permissionMessage     the explanation message to display to the user
     * @param index                 the index in the displayArray of the permission
     * @param activity              the calling activity, needed for dialog
     */
    public void askForPermission(final String pName, final String pCode, String permissionMessage,
                                 final int index, final Activity activity) {
        Log.v(TAG, pName + "not yet granted.");
        boolean explainPermission = ActivityCompat.shouldShowRequestPermissionRationale(this,
                pCode);

        if (explainPermission && !displayArray[index]) {
            Log.v(TAG, "Permission explanation requested for " + pName);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle( pName + " Explanation");
            builder.setMessage(permissionMessage);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.v(TAG, "User has read permission explanation for " + pName);
                    ActivityCompat.requestPermissions(activity, new String[]{pCode}, index);
                }
            });
            builder.show();

            displayArray[index] = true;
        }else
            ActivityCompat.requestPermissions(activity, new String[]{pCode}, index);
    }

    /**
     * Listens for the selection of the record button. Once the button is selected a picture of a
     * bill should be taken and passed to the classifier. If mKeepPhotoCheckBox is selected the
     * picture will be saved to the device's memory.
     */
    @OnClick(R.id.record_button)
    public void onRecordButtonClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        final String photoStoragePath = genPhotoStoragePath();
        final String photoLabel = genPhotoLabel();

        if (cameraFragment != null) {
            cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultAdapter() {
               @Override
               public void onVideoRecorded(String filePath) {
                   /* Video recordings are disabled for this application */
               }

               @Override
               public void onPhotoTaken(byte[] bytes, String filePath) {
                   Toast.makeText(getBaseContext(), "onPhotoTaken " + filePath,
                           Toast.LENGTH_SHORT).show();
                   startImageProcessing(filePath);
               }
           },
                    photoStoragePath, photoLabel);
        }
    }

    /**
     * Once a photo has been taken by the user the photo should be passed to another Activity for
     * processing.
     *
     * @param photoPath     the path to the photo
     */
    public void startImageProcessing(String photoPath){
        startActivityForResult(ProcessingActivity.genIntent(getApplicationContext(),
                photoPath), PROC_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PROC_REQ_CODE: {
                if(resultCode == RESULT_OK) {
                    /* Classifier succeeded, start ResultsActivity */
                    Log.v(TAG, "Reported back successfully!");
                    String currencyCode = data.getStringExtra(RESULT_CURRENCY_CODE);
                    String currencyValue = data.getStringExtra(RESULT_CURRENCY_VALUE);
                    String currencyPic = data.getStringExtra(RESULT_CURRENCY_PIC);

                    startResultsActivity(currencyCode, currencyValue, currencyPic);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Once the ProcessingActivity has ended successfully, passes the information to the
     * ResultsActivity.
     *
     * @param code      the three-letter currency code used by Fixer.io API
     * @param value     the value, in its local currency, of the classified banknote
     */
    public void startResultsActivity(String code, String value, String path){
        Log.v(TAG, "Starting ResultsActivity....");
        Intent resActIntent = ResultsActivity.buildIntent(this, code, value, path);
        startActivity(resActIntent);
    }

    /**
     * Handles permissions for camera usage and makes sure the camera preview is visible if the
     * application has sufficient permissions.
     */
    public void buildCamera() {
        final String cameraPermission = Manifest.permission.CAMERA;
        int permissionCheck = ActivityCompat.checkSelfPermission(this, cameraPermission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            askForPermission("Camera Permission", cameraPermission,
                    "This application needs Camera permissions in order to take store images of " +
                            "unknown currencies.", CAMERA_PERMISSION, this);
        }
        else
            addCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){

            case CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Camera permission granted.");
                    buildCamera();
                } else {
                    Log.v(TAG, "Camera permission not granted.");
                    Toast.makeText(getBaseContext(), "Please enable camera permissions to continue",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Adds the camera preview to the Activity view and hooks up listeners for camera events and
     * controls.
     */
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

                    }

                    @Override
                    public void onFlashOn() {

                    }

                    @Override
                    public void onFlashOff() {

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
                    }

                    @Override
                    public void unLockControls() {
                        mTakePictureButton.setEnabled(true);
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
        return "photo_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    /**
     * Returns a valid path to store photo taken for future processing.
     *
     * @return path of file save location
     */
    private String genPhotoStoragePath(){
        if(canFullyAccessExternalStorage()){
            File dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
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

    public static Intent buildProcessingResultIntent(String currencyCode, String currencyValue,
                                                     String photoPath){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_CURRENCY_CODE, currencyCode);
        resultIntent.putExtra(RESULT_CURRENCY_VALUE, currencyValue);
        resultIntent.putExtra(RESULT_CURRENCY_PIC, photoPath);

        return resultIntent;
    }

    /**
     * Handles the application request for Camera access. Called by
     * {@link CameraActivity#buildCamera()} in order to handle permission request after user has
     * been notified of why the permission is needed.
     *
     * @param cameraPermission string value of camera permission
     */
    private void requestCameraPermission(String cameraPermission){
        Log.v(TAG, "Permission requested.");
        ActivityCompat.requestPermissions(this, new String[]{cameraPermission},
                CAMERA_PERMISSION);
    }
}
