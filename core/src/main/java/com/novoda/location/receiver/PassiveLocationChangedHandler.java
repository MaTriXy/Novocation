package com.novoda.location.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LastLocationFinder;

class PassiveLocationChangedHandler {

    final LastLocationFinder lastLocationFinder;

    public PassiveLocationChangedHandler(LastLocationFinder locationFinder) {
        this.lastLocationFinder = locationFinder;
    }

    public void onNewChange(Context context, Intent intent) {
        String key = LocationManager.KEY_LOCATION_CHANGED;
        if (intent.hasExtra(key)) {
            Location location = (Location) intent.getExtras().get(key);
            updateLocation(location);
        } else {
            updateLocationFromAlarmIntent(context);
        }
    }

    private void updateLocationFromAlarmIntent(Context context) {
        // This update came from a recurring alarm. We need to determine if
        // there has been a more recent Location received than the last
        // location we used.

        LocatorSettings settings = getSettings();
        long locationUpdateInterval = settings.getPassiveUpdatesInterval();
        float locationUpdateDistanceDiff = settings.getPassiveUpdatesDistance();

        // Get the best last location detected from the providers.
        long delta = System.currentTimeMillis() - locationUpdateInterval;

        Location location = lastLocationFinder.getLastBestLocation(delta);
        // Check if the last location detected from the providers is either
        // too soon, or too close to the last value we used. If it is within
        // those thresholds we set the location to null to prevent the
        // update Service being run unnecessarily (and spending battery on
        // data transfers).
        verifyAndUpdateLocation(location, locationUpdateDistanceDiff, delta);
    }

    private LocatorSettings getSettings() {
        return LocatorFactory.getInstance().getSettings();
    }

    private void verifyAndUpdateLocation(Location location, float locationUpdateDistanceDiff, long delta) {
        Location currentLocation = LocatorFactory.getLocation();
        if (location != null && currentLocation != null) {
            if (currentLocation.getTime() <= delta && currentLocation.distanceTo(location) >= locationUpdateDistanceDiff) {
                updateLocation(location);
            }
        }
    }

    private void updateLocation(Location location) {
        if (location == null) {
            return;
        }
        LocatorFactory.setLocation(location);
    }


}
