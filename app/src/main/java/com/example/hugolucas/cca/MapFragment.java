package com.example.hugolucas.cca;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.opencv.core.Point;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by hugolucas on 10/26/17.
 */

public class MapFragment extends Fragment{

    private final static int FINE_LOCATION_CODE = 100;

    @BindView(R.id.mapView)
    private MapView mapView;

    private MapboxMap mMapBoxMap;
    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(getActivity());
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mapbox.getInstance(getActivity(), getString(R.string.mapbox_api_key));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapBoxMap = mapboxMap;
                int fineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (fineLocationPermission == PackageManager.PERMISSION_GRANTED){
                    /* Sufficient permissions already granted, proceed normally */
                    buildGoogleApiClient();
                }else{
                    /* Permissions not yet granted, ask user before proceeding */
                    handleLocationPermission();
                }
            }
        });
    }

    private void buildGoogleApiClient(){

    }

    /**
     * Requests access to the User's location data. If needed, also provides an explanation for why
     * the application needs this data.
     */
    private void handleLocationPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)){
            /* Explain to user why we need access to this information */
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.loc_explanation))
                    .setMessage(getString(R.string.loc_title))
                    .setPositiveButton(getString(R.string.affirmative_selection),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            /* Request permission from user */
                            requestLocationPermission();
                        }
                    })
                    .create()
                    .show();
        }else{
            /* Request, but don't explain, permission */
            requestLocationPermission();
        }
    }

    /**
     * Requests permission to access User's location data.
     */
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case FINE_LOCATION_CODE: {
                /* Check if the permission was granted */
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    /* Permission granted! */
                    buildGoogleApiClient();
                }else{
                    /* Permission denied! */
                    Toast.makeText(getContext(), R.string.loc_denied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
