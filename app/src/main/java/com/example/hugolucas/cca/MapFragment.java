package com.example.hugolucas.cca;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hugolucas.cca.apis.GoogleMaps;
import com.example.hugolucas.cca.apiObjects.LocationResponse;
import com.example.hugolucas.cca.apiObjects.MapResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by hugolucas on 10/26/17.
 */

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "mapFragment";

    private final static int FINE_LOCATION_CODE = 100;

    @BindView(R.id.mapView) MapView mMapView;
    @BindView(R.id.floating_my_location) FloatingActionButton mSearchButton;

    private Marker mMarker;
    private LatLng mLatLng;
    private GoogleMap mGoogleMap;
    private MarkerOptions mMarkerOptions;
    private GoogleApiClient mGoogleApiClient;

    /* Radius is in meters */
    private String mDefaultSearchRadius = "1000";
    private int maxSearchRadius = 20000;

    private boolean mFirstCameraUpdate = true;
    private boolean mUpdateNearbyLocations = true;

    private List<MapResult> mCurrentMapResults;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_map_fragment, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mapbox.getInstance(getActivity(), getString(R.string.mapbox_api_key));
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
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
     * Asynchronously queries the Google Maps API for the locations of nearby financial
     * institutions. Once a positive result has been received, method updates the MapView with the
     * locations of these new places.
     */
    private void getNearbyLocationData(boolean calledDirectly){
        if (mUpdateNearbyLocations || calledDirectly) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            GoogleMaps map = retrofit.create(GoogleMaps.class);
            Call<LocationResponse> call = map.nearbyLocations(
                    "true",
                    getString(R.string.google_maps_api_key),
                    "bank",
                    Double.toString(mLatLng.latitude) + "," + Double.toString(mLatLng.longitude),
                    mDefaultSearchRadius);
            call.enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    if (response.body().getStatus().equals("OK")){
                        mCurrentMapResults = response.body().getMapResults();
                        placeLocationMarkers();
                    }
                }

                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    Toast.makeText(getActivity(), "Error :(", Toast.LENGTH_LONG).show();
                }
            });
            mUpdateNearbyLocations = false;
        }
    }

    /**
     * Using the results from the Google Maps API, this method populates the MapView with the
     * locations of the financial institutions.
     */
    public void placeLocationMarkers(){
        // Icon icon = drawableToIcon(getContext(), R.drawable.map_icon_bank);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_icon_bank);

        for(MapResult res: mCurrentMapResults){
            com.example.hugolucas.cca.apiObjects.Location loc = res.getGeometry().getLocation();

            MarkerOptions newMarker = new MarkerOptions()
                    .position(new LatLng(loc.getLat(), loc.getLng()))
                    .title(res.getName())
                    .snippet(res.getVicinity())
                    .icon(icon);
            mGoogleMap.addMarker(newMarker);
        }
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
            zoomInOnUser();
            mFirstCameraUpdate = false;
        }
    }

    /**
     * Zooms in on the User when the floating action button is selected.
     */
    @OnClick(R.id.floating_my_location)
    public void zoomInOnUser(){
        mMapView.setCameraDistance(20);
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(mLatLng)
                        .zoom(15)
                        .bearing(180)
                        .tilt(30)
                        .build()), 3000,
                new GoogleMap.CancelableCallback(){
                    @Override
                    public void onFinish() {
                        /* Don't Care */
                    }

                    @Override
                    public void onCancel() {
                        /* Don't Care */
                    }
                }
        );
        mFirstCameraUpdate = false;
    }

    /**
     * Increases the search radius for financial institutions up to some limit.
     */
    @OnClick(R.id.floating_increase_distance)
    public void increaseSearchRadius(){
        int currentRadius = Integer.parseInt(mDefaultSearchRadius);

        if (currentRadius < maxSearchRadius){
            if (currentRadius == 1000)
                currentRadius = 5000;
            else if (currentRadius == 5000)
                currentRadius = 10000;
            else if (currentRadius == 10000)
                currentRadius = 20000;

            mDefaultSearchRadius = String.valueOf(currentRadius);

            getNearbyLocationData(true);
        }
    }

    /**
     * Adds a marker to signify where the user is located if one does not exist already.
     */
    private void addUserMarker(){
        if (mMarker == null) {
            mMarkerOptions = new MarkerOptions()
                    .position(mLatLng)
                    .title(getString(R.string.map_user_title));
            mMarker = mGoogleMap.addMarker(mMarkerOptions);
        }else{
            mMarker.setPosition(mLatLng);
        }
    }

    /**
     * Converts an vector image into a bitmap. Used to supply MapView with custom icons.
     *
     * @param context       application context
     * @param id            resource id of vector asset
     * @return              icon made from the drawable
     */
    public static Icon drawableToIcon(@NonNull Context context, @DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        // return IconFactory.getInstance(context).fromBitmap(bitmap);
        return null;
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
        /* Don't Care */
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
        addUserMarker();
        updateMapCamera();
        getNearbyLocationData(false);
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
