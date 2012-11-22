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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.store.SettingsDao;
import com.novoda.location.provider.updater.LocationUpdater;
import com.novoda.location.provider.updater.LocationUpdaterFactory;
import com.novoda.location.util.ApiLevelDetector;
import com.novoda.location.util.SettingsDaoUtil;

//TODO extract the logic out of this receiver
public class RestorePassiveListenerBoot extends BroadcastReceiver {

    private ApiLevelDetector apiLevelDetector;

    @Override
    public void onReceive(Context c, Intent intent) {
        SettingsDao settingsDao = new SettingsDaoUtil().getSettingsDao();
        if (settingsDao.hasApplicationRunOnce(c) && settingsDao.isPassiveLocationChanges(c)) {
            startPassiveLocationUpdates(c);
        }
    }

    private void startPassiveLocationUpdates(Context context) {
        LocationUpdater locationUpdater = getLocationUpdater(context);
        locationUpdater.startPassiveLocationUpdates(getSettings(), createPendingIntent(context));
    }

    private LocationUpdater getLocationUpdater(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationUpdaterFactory locationUpdaterFactory = new LocationUpdaterFactory(lm, getApiLevelDetector(), (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        return locationUpdaterFactory.getLocationUpdater();
    }

    private LocatorSettings getSettings() {
        return LocatorFactory.getInstance().getSettings();
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent passiveIntent = new Intent(context, PassiveLocationChanged.class);
        PendingIntent locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return locationListenerPassivePendingIntent;
    }

    public void setApiLevelDetector(ApiLevelDetector apiLevelDetector) {
        this.apiLevelDetector = apiLevelDetector;
    }

    private ApiLevelDetector getApiLevelDetector() {
        return apiLevelDetector != null ? apiLevelDetector : new ApiLevelDetector();
    }
}