/**
 * Copyright 2011 Novoda Ltd.
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
 */
package com.novoda.location;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.provider.LastLocationFinder;
import com.novoda.location.provider.LocationProviderFactory;
import com.novoda.location.provider.LocationUpdateRequester;
import com.novoda.location.util.ApiLevelDetector;

class LocationUpdateManager {

    private final LocationProviderFactory locationProviderFactory;
    private final LocatorSettings settings;
    private final LocationManager locationManager;
    private final LocationUpdateRequester locationUpdateRequester;
    private final PendingIntent activeLocationUpdate;
    private final PendingIntent passiveLocationUpdate;

    LocationUpdateManager(LocatorSettings settings,
                          LocationManager locationManager,
                          LocationProviderFactory locationProviderFactory,
                          LocationUpdatesIntentFactory updatesIntentFactory) {

        this.settings = settings;
        this.locationProviderFactory = locationProviderFactory;
        this.locationManager = locationManager;
        activeLocationUpdate = updatesIntentFactory.buildActive();
        passiveLocationUpdate = updatesIntentFactory.buildPassive();
        locationUpdateRequester = locationProviderFactory.getLocationUpdateRequester(locationManager);
    }

    void requestActiveLocationUpdates(Criteria criteria) throws NoProviderAvailable {
        try {
            locationUpdateRequester.requestActiveLocationUpdates(settings, criteria, activeLocationUpdate);
        } catch (IllegalArgumentException iae) {
            throw new NoProviderAvailable();
        }
    }

    void requestPassiveLocationUpdates() {
        if (ApiLevelDetector.supportsFroyo() && settings.shouldEnablePassiveUpdates()) {
            locationUpdateRequester.requestPassiveLocationUpdates(settings, passiveLocationUpdate);
        }
    }

    void removeActiveLocationUpdates() {
        locationManager.removeUpdates(activeLocationUpdate);
    }

    void removePassiveLocationUpdates() {
        locationManager.removeUpdates(passiveLocationUpdate);
    }

    void fetchLastKnownLocation() {
        LastLocationFinder finder = locationProviderFactory.getLastLocationFinder(locationManager);
        float locationUpdateDistanceDiff = settings.getUpdatesDistance();
        long locationUpdateInterval = settings.getUpdatesInterval();
        long minimumTime = System.currentTimeMillis() - locationUpdateInterval;
        Location lastKnownLocation = finder.getLastBestLocation(locationUpdateDistanceDiff, minimumTime);
        if (lastKnownLocation != null) {
            LocatorFactory.setLocation(lastKnownLocation);
        }
    }

}
