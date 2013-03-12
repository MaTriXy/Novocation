package com.novoda.locationdemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.novoda.location.Locator;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.locationdemo.LocationDemo;
import com.novoda.locationdemo.R;
import com.novoda.locationdemo.analytics.Analytics;
import com.novoda.locationdemo.fragment.DemoSupportMapFragment;

import java.util.Date;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectView;

import static com.novoda.locationdemo.LocationDemo.LOCATION_UPDATE_ACTION;
import static com.novoda.locationdemo.LocationDemo.LOG_TAG;

public class NovocationDemo extends RoboFragmentActivity {

    public BroadcastReceiver freshLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentLocation = locator.getLocation();
            new Analytics(context).trackLocationReceived(locator.getLocation(),
                    currentLocation, time);
            displayNewLocation(locator.getLocation());
            updateMap(locator.getLocation());
        }
    };
    @InjectView(R.id.val_use_gps)
    TextView useGps;
    @InjectView(R.id.val_updates)
    TextView updates;
    @InjectView(R.id.val_passive_updates)
    TextView passive;
    @InjectView(R.id.val_update_interval)
    TextView interval;
    @InjectView(R.id.val_update_distance)
    TextView distance;
    @InjectView(R.id.val_passive_interval)
    TextView passiveInterval;
    @InjectView(R.id.val_passive_distance)
    TextView passiveDistance;
    private Locator locator;
    private Analytics analytics;
    private long time;
    private Location currentLocation;
    private DemoSupportMapFragment map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novocation_demo);

        map = (DemoSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        analytics = new Analytics(this);

        LocationDemo app = (LocationDemo) getApplication();
        locator = app.getLocator();
        analytics.trackLocationUpdateList();

        displayLocationSettings();
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter f = new IntentFilter();
        f.addAction(LOCATION_UPDATE_ACTION);
        registerReceiver(freshLocationReceiver, f);

        try {
            locator.startActiveLocationUpdates();
        } catch (NoProviderAvailable npa) {
            analytics.trackNoProviderAvailable();
            Toast.makeText(this, "No provider available", Toast.LENGTH_LONG).show();
        }
        //========================================================

        time = System.currentTimeMillis();
        currentLocation = null;
    }

    @Override
    public void onPause() {

        unregisterReceiver(freshLocationReceiver);
        locator.stopLocationUpdates();

        analytics.trackLocationSuccessOrFailure(currentLocation, time);
        super.onPause();
    }

    private void displayLocationSettings() {
        LocatorSettings settings = locator.getSettings();

        useGps.setText(getBooleanText(settings.shouldUseGps()));
        updates.setText(getBooleanText(settings.shouldUpdateLocation()));
        passive.setText(getBooleanText(settings.shouldEnablePassiveUpdates()));
        interval.setText(settings.getUpdatesInterval() / (60 * 1000) + " mins");
        distance.setText(settings.getUpdatesDistance() + "m");
        passiveInterval.setText(settings.getPassiveUpdatesInterval() / (60 * 1000) + " mins");
        passiveDistance.setText(settings.getPassiveUpdatesDistance() + "m");
    }

    private String getBooleanText(boolean bool) {
        return bool ? "ON" : "OFF";
    }

    private void displayNewLocation(final Location location) {
        Log.v(LOG_TAG, "Getting <accuracy,latitude,longitude>: " + location.getAccuracy() + " " + location.getLatitude() +
                " " + location.getLongitude());
        View block = getLayoutInflater().inflate(R.layout.location_view, null);

        TextView time = (TextView) block.findViewById(R.id.val_time);
        TextView accuracy = (TextView) block.findViewById(R.id.val_acc);
        TextView provider = (TextView) block.findViewById(R.id.val_prov);
        TextView latitude = (TextView) block.findViewById(R.id.val_lat);
        TextView longitude = (TextView) block.findViewById(R.id.val_lon);

        time.setText(DateFormat.format("hh:mm:ss", new Date(location.getTime())));
        accuracy.setText(location.getAccuracy() + "m");
        provider.setText(location.getProvider());
        latitude.setText("" + location.getLatitude());
        longitude.setText("" + location.getLongitude());

        String providerName = location.getProvider();
        if (providerName.equalsIgnoreCase("network")) {
            block.setBackgroundResource(R.color.network);
        } else if (providerName.equalsIgnoreCase("gps")) {
            block.setBackgroundResource(R.color.gps);
        }

        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMap(location);
            }
        });
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.content);
        viewGroup.addView(block, 0);
    }

    private void updateMap(Location location) {
        map.updateMapCamera(location);
    }

}
