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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.updater.LocationUpdaterFactory;
import com.novoda.location.provider.updater.LocationUpdater;
import com.novoda.location.provider.store.SettingsDao;

//TODO extract the logic out of this receiver
public class RestorePassiveListenerBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        SettingsDao settingsDao = new LocationUpdaterFactory().getSettingsDao();
        if (settingsDao.hasApplicationRunOnce(c) && settingsDao.isPassiveLocationChanges(c)) {
            startPassiveLocationUpdates(c);
        }
    }

    private void startPassiveLocationUpdates(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationUpdater locationUpdater = new LocationUpdaterFactory().getLocationUpdater(lm);
        LocatorSettings settings = LocatorFactory.getInstance().getSettings();
        locationUpdater.startPassiveLocationUpdates(settings, createPendingIntent(context));
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent passiveIntent = new Intent(context, PassiveLocationChanged.class);
        PendingIntent locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return locationListenerPassivePendingIntent;
    }
}