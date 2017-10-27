package com.example.hugolucas.cca;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import static com.example.hugolucas.cca.R.id.mapView;

/**
 * Created by hugolucas on 10/26/17.
 */

public class MapFragment extends Fragment{

    private String apiKey = "pk.eyJ1IjoiaHVnb2Y3NTYiLCJhIjoiY2o5OTc4N3I0MHBjZTJ3cW1tYXBsOTUzZyJ9.h2WlhaCYJMlkF_Nq88NDVg";
    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mapbox.getInstance(getActivity(), apiKey);
        mapView = getView().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
    }
}
