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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.location.LocationManager;

import com.novoda.location.LocatorSettings;

public class LegacyLocationUpdateRequester extends FroyoLocationUpdateRequester {

    protected final AlarmManager alarmManager;

    protected LegacyLocationUpdateRequester(LocationManager locationManager, AlarmManager alarmManager, LocatorSettings settings) {
        super(locationManager);
        this.alarmManager = alarmManager;
    }

    @Override
    public void requestPassiveLocationUpdates(LocatorSettings settings, PendingIntent pendingIntent) {
        // Pre-Froyo there was no Passive Location Provider, so instead we will
        // set an inexact repeating, non-waking alarm that will trigger once the
        // minimum time between passive updates has expired. This is potentially
        // more expensive than simple passive alarms, however the Receiver will
        // ensure we've transitioned beyond the minimum time and distance before
        // initiating a background nearby loction update.
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis()
                + settings.getPassiveUpdatesInterval(), settings.getPassiveUpdatesInterval(), pendingIntent);
    }
}
