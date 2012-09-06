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
package com.novoda.location.provider.requester;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LocationUpdateRequester;

public class FroyoLocationUpdateRequester implements LocationUpdateRequester {

    protected LocationManager locationManager;

    public FroyoLocationUpdateRequester(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void requestActiveLocationUpdates(LocatorSettings settings, Criteria criteria, PendingIntent pendingIntent) {
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            return;
        }
        long minTime = settings.getUpdatesInterval();
        float minDistance = settings.getUpdatesDistance();
        locationManager.requestLocationUpdates(provider, minTime, minDistance, pendingIntent);
    }

    @Override
    public void requestPassiveLocationUpdates(LocatorSettings settings, PendingIntent pendingIntent) {
        long minTime = settings.getPassiveUpdatesInterval();
        float minDistance = settings.getPassiveUpdatesDistance();
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, pendingIntent);
    }

}