package com.example.hugolucas.cca;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by hugolucas on 10/26/17.
 */

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int FINE_LOCATION_CODE = 100;

    @BindView(R.id.mapView) MapView mMapView;

    private LatLng mLatLng;
    private MapboxMap mMapBoxMap;
    private MarkerOptions mMarker;
    private GoogleApiClient mGoogleApiClient;

    /* Radius is in meters */
    private String mDefaultSearchRadius = "10000";

    private boolean mFirstCameraUpdate = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mapbox.getInstance(getActivity(), getString(R.string.mapbox_api_key));
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
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

    /**
     * Builds GoogleApiClient object to get data from Google Play Services.
     */
    private synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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
     * Creates a location request and configures it request the user's current location once every
     * 10 seconds. If another application has the user's location data, this application will only
     * make use of that data once every 5 seconds.
     *
     * @return      a LocationRequest object configured as described above
     */
    private LocationRequest createRequest(){
        return new LocationRequest()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests permission to access User's location data.
     */
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_CODE);
    }

    /**
     * Updates the MapBox camera in order to keep the User's view of their position consistent.
     */
    private void updateMapCamera(){
        if (mFirstCameraUpdate) {
            mMapView.setCameraDistance(20);
            mMapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(mLatLng)
                            .zoom(15)
                            .bearing(180)
                            .tilt(30)
                            .build()), 7000);
            mFirstCameraUpdate = false;
        }
    }

    /**
     * Adds a marker to signify where the user is located if one does not exist already.
     *
     * @param clearOldMarkers   if True clears the map of all markers
     */
    private void addUserMarker(boolean clearOldMarkers){
        if (clearOldMarkers)
            mMapBoxMap.clear();

        mMarker = new MarkerOptions()
                .position(mLatLng)
                .title(getString(R.string.map_user_title));
        mMapBoxMap.addMarker(mMarker);
    }

    /**
     * Creates the URL to send to the Google Maps API for the location of nearby financial
     * institutions.
     *
     * @return      a String URL
     */
    private String createQueryURL(){
        Uri.Builder urlBuilder = new Uri.Builder();
        urlBuilder.scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("place")
                .appendPath("nearbysearch")
                .appendPath("json")
                .appendQueryParameter("location", Double.toString(mLatLng.getLatitude()) + "," +
                        Double.toString(mLatLng.getLongitude()))
                .appendQueryParameter("radius", mDefaultSearchRadius)
                .appendQueryParameter("type", "bank")
                .appendQueryParameter("sensor", "true")
                .appendQueryParameter("key", getString(R.string.google_maps_api_key));
        return urlBuilder.build().toString();
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

    /* ****************************************************************************************** */
    /* CONNECTION CALLBACKS METHODS  */
    /* ****************************************************************************************** */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest request = createRequest();
        int permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request,
                    this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /* ****************************************************************************************** */
    /* ON CONNECTION FAILED LISTENER METHODS  */
    /* ****************************************************************************************** */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Connection Failed", Toast.LENGTH_LONG).show();
    }

    /* ****************************************************************************************** */
    /* LOCATION LISTENER METHODS  */
    /* ****************************************************************************************** */

    @Override
    public void onLocationChanged(Location location) {
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.v("DEBUG", createQueryURL());
        if(mMarker != null)
            addUserMarker(true);
        else
            addUserMarker(false);
        updateMapCamera();
    }

    /* ****************************************************************************************** */
    /* LIFE CYCLE METHODS, OVERWRITTEN FOR MAP VIEW COMPARABILITY */
    /* ****************************************************************************************** */

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
