/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Code modified by Novoda Ltd, 2011.
 */
package com.novoda.location.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LastLocationFinder;

//TODO this logic needs to be extracted to a java object with the dependencies injected through the constructor
public class PassiveLocationChanged extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String key = LocationManager.KEY_LOCATION_CHANGED;
        Location location;
        if (intent.hasExtra(key)) {
            location = (Location) intent.getExtras().get(key);
            updateLocation(location);
        } else {
            // This update came from a recurring alarm. We need to determine if
            // there has been a more recent Location received than the last
            // location we used.

            LocatorSettings settings = getSettings();
            long locationUpdateInterval = settings.getPassiveUpdatesInterval();
            float locationUpdateDistanceDiff = settings.getPassiveUpdatesDistance();

            // Get the best last location detected from the providers.
            long delta = System.currentTimeMillis() - locationUpdateInterval;

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            LastLocationFinder lastLocationFinder = getLastLocationFinder(locationManager);
            location = lastLocationFinder.getLastBestLocation(delta);
            // Check if the last location detected from the providers is either
            // too soon, or too close to the last value we used. If it is within
            // those thresholds we set the location to null to prevent the
            // update Service being run unnecessarily (and spending battery on
            // data transfers).
            verifyAndUpdateLocation(location, locationUpdateDistanceDiff, delta);
        }
    }

    protected LastLocationFinder getLastLocationFinder(LocationManager locationManager) {
        return new LastLocationFinder(locationManager);
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

    private LocatorSettings getSettings() {
        return LocatorFactory.getInstance().getSettings();
    }
}