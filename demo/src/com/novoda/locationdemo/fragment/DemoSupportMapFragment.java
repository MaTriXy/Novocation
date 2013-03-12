package com.novoda.locationdemo.fragment;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class DemoSupportMapFragment extends SupportMapFragment implements GoogleMap.OnInfoWindowClickListener {

    private static final double LONDON_LAT = 51.5001402;
    private static final double LONDON_LONG = -0.1261932;
    private static final int ZOOM_LEVEL = 14;

    private GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(inflater, viewGroup, bundle);
        setUpMapIfNeeded();
        return view;
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = getMap();
            setUpMap();
        }
    }

    private void setUpMap() {
        initUi();
        initListeners();
        map.setMyLocationEnabled(true);
    }

    public void initUi() {
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    private void initListeners() {
        map.setOnInfoWindowClickListener(this);
    }

    public void updateMapCamera(Location location) {
        map.animateCamera(getCameraUpdateFromLocation(location));
    }

    private CameraUpdate getCameraUpdateFromLocation(Location location) {
        return CameraUpdateFactory.newLatLngZoom(locationToLatLng(location), ZOOM_LEVEL);
    }

    private LatLng locationToLatLng(Location location) {
        return location == null ? new LatLng(LONDON_LAT, LONDON_LONG) : new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        new AlertDialog.Builder(getActivity()).setTitle(marker.getTitle()).setMessage(marker.getSnippet()).show();
    }

}
