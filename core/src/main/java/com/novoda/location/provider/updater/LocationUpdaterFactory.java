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
package com.novoda.location.provider.updater;

import android.app.AlarmManager;
import android.location.LocationManager;
import com.novoda.location.util.ApiLevelDetector;

public class LocationUpdaterFactory {

    private final LocationManager locationManager;
    private final ApiLevelDetector apiLevelDetector;
    private final AlarmManager alarmManager;

    public LocationUpdaterFactory(LocationManager locationManager, ApiLevelDetector apiLevelDetector, AlarmManager alarmManager) {
        this.locationManager = locationManager;
        this.apiLevelDetector = apiLevelDetector;
        this.alarmManager = alarmManager;
    }

    public LocationUpdater getLocationUpdater() {
        if (apiLevelDetector.supportsGingerbread()) {
            return new GingerbreadLocationUpdater(locationManager);
        }
        if (apiLevelDetector.supportsFroyo()) {
            return new FroyoLocationUpdater(locationManager);
        }
        return new LegacyLocationUpdater(locationManager, alarmManager);
    }

}
