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
 *
 * This code is based on Reto Meier's Location Pro-tips and Stefano 
 * Dacchille's IgnitedLocation.
 *
 * Code modified by Novoda Ltd, 2011.
 */
package com.novoda.location;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.provider.LocationProviderFactory;
import com.novoda.location.util.LocationAccuracy;

class DefaultLocator implements Locator {

    private volatile Location currentLocation;
    private LocationAccuracy locationAccuracy;
    private Context context;
    private LocatorSettings settings;
    private LocationManager locationManager;
    private Criteria criteria;
    private LocationUpdateManager locationUpdateManager;

    private final LocationListener oneShotNetworkLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            locationManager.removeUpdates(this);
        }

        @Override
        public void onProviderEnabled(String arg0) {
            locationManager.removeUpdates(this);
        }

        @Override
        public void onProviderDisabled(String arg0) {
            locationManager.removeUpdates(this);
        }

        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(this);
            setLocation(location);
        }
    };

    private boolean listeningForActiveLocationUpdates;

    @Override
    public void prepare(Context c, LocatorSettings settings) {
        this.settings = settings;
        context = c;
        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        locationAccuracy = new LocationAccuracy(settings);
        LocationProviderFactory providerFactory = new LocationProviderFactory();
        LocationUpdatesIntentFactory updatesIntentFactory = new LocationUpdatesIntentFactory(settings, context);
        locationUpdateManager = new LocationUpdateManager(settings, locationManager, providerFactory, updatesIntentFactory);
    }

    @Override
    public LocatorSettings getSettings() {
        return settings;
    }

    @Override
    public void startLocationUpdates() throws NoProviderAvailable {
        createActiveUpdateCriteria();
        persistSettingsToPreferences();
        sendFirstAvailableLocation();
        startListeningForLocationUpdates();
    }

    private void createActiveUpdateCriteria() {
        criteria = new Criteria();
        if (settings.shouldUseGps()) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
    }

    private void persistSettingsToPreferences() {
        new LocationProviderFactory().getSettingsDao().persistSettingsToPreferences(context, settings);
    }

    private void sendFirstAvailableLocation() {
        if (currentLocation == null) {
            locationUpdateManager.fetchLastKnownLocation();
        } else {
            sendLocationUpdateBroadcast();
        }
    }

    private void sendLocationUpdateBroadcast() {
        if (context == null) {
            return;
        }
        Intent broadcast = new Intent();
        broadcast.setAction(settings.getUpdateAction());
        broadcast.setPackage(settings.getPackageName());
        context.sendBroadcast(broadcast);
    }

    private void startListeningForLocationUpdates() throws NoProviderAvailable {
        if (!settings.shouldUpdateLocation()) {
            return;
        }
        listeningForActiveLocationUpdates = true;
        locationUpdateManager.requestActiveLocationUpdates(criteria);
        ifGPSregisterOneShotNetworkUpdate();
        locationUpdateManager.removePassiveLocationUpdates();
    }

    private void ifGPSregisterOneShotNetworkUpdate() {
        String bestEnabledProvider = locationManager.getBestProvider(criteria, true);
        if (bestEnabledProvider != null && LocationManager.GPS_PROVIDER.equals(bestEnabledProvider)) {
            locationManager.removeUpdates(oneShotNetworkLocationListener);
            if (isNetworkProviderEnabled()) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, oneShotNetworkLocationListener);
            }
        }
    }

    @Override
    public void stopLocationUpdates() {
        if (!settings.shouldUpdateLocation()) {
            return;
        }
        listeningForActiveLocationUpdates = false;
        stopListeningForLocationUpdates();
    }

    private void stopListeningForLocationUpdates() {
        locationUpdateManager.removeActiveLocationUpdates();
        locationUpdateManager.requestPassiveLocationUpdates();
        locationManager.removeUpdates(oneShotNetworkLocationListener);
    }

    @Override
    public Location getLocation() {
        return currentLocation;
    }

    @Override
    public void setLocation(Location location) {
        if (locationAccuracy.isBetterLocation(location, currentLocation)) {
            currentLocation = location;
            sendLocationUpdateBroadcast();
        }
    }

    @Override
    public void providerStatusChanged() {
        if (listeningForActiveLocationUpdates) {
            try {
                locationUpdateManager.removeActiveLocationUpdates();
                startListeningForLocationUpdates();
            } catch (NoProviderAvailable npa) {
                //We cant listen for updates if no provider is enabled
            }
        }
    }

    @Override
    public boolean isNetworkProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public boolean isGpsProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * This is for testing only, it will be removed
     *
     * @param locationUpdateManager
     */
    @Deprecated
    void setLocationUpdateManager(LocationUpdateManager locationUpdateManager) {
        this.locationUpdateManager = locationUpdateManager;
    }
}