package com.novoda.location.receiver;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LastLocationFinder;

class PassiveLocationChangedHandler {

    private final LastLocationFinder lastLocationFinder;

    public PassiveLocationChangedHandler(LastLocationFinder locationFinder) {
        this.lastLocationFinder = locationFinder;
    }

    public void onNewChange(Intent intent) {
        String key = LocationManager.KEY_LOCATION_CHANGED;
        if (intent.hasExtra(key)) {
            Location location = (Location) intent.getExtras().get(key);
            updateLocation(location);
        } else {
            alarmIntentTriggeredLocationUpdate();
        }
    }

    private void alarmIntentTriggeredLocationUpdate() {
        LocatorSettings settings = getSettings();
        long passiveUpdateIntervalDelta = getPassiveUpdateIntervalDelta(settings);
        Location location = lastLocationFinder.getLastBestLocation(passiveUpdateIntervalDelta);
        if (locationUpdateRequired(location, settings.getPassiveUpdatesDistance(), passiveUpdateIntervalDelta)) {
            updateLocation(location);
        }
    }

    private LocatorSettings getSettings() {
        return LocatorFactory.getInstance().getSettings();
    }

    private long getPassiveUpdateIntervalDelta(LocatorSettings settings) {
        return System.currentTimeMillis() - settings.getPassiveUpdatesInterval();
    }

    private boolean locationUpdateRequired(Location location, float locationUpdateDistanceDiff, long delta) {
        if (location != null) {
            if (newLocationIsWithinUpdateThreshold(location, locationUpdateDistanceDiff, delta)) {
                return true;
            }
        }
        return false;
    }

    private boolean newLocationIsWithinUpdateThreshold(Location location, float  locationUpdateDistanceDiff, long delta) {
        Location currentLocation = LocatorFactory.getLocation();
        if (currentLocation == null) {
            return false;
        }
        return currentLocation.getTime() <= delta && currentLocation.distanceTo(location) >= locationUpdateDistanceDiff;
    }

    private void updateLocation(Location location) {
        if (location == null) {
            return;
        }
        LocatorFactory.setLocation(location);
    }

}
