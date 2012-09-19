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
package com.novoda.location.provider.task;

import android.location.Location;
import android.os.AsyncTask;

import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LastLocationFinder;

public class LastKnownLocationTask extends AsyncTask<Void, Void, Location> {
    
    private LastLocationFinder lastLocationFinder;
    private float locationUpdateDistanceDiff;
    private long locationUpdateInterval;

    public LastKnownLocationTask(LastLocationFinder lastLocationFinder, LocatorSettings settings) {
        this.lastLocationFinder = lastLocationFinder;
        this.locationUpdateDistanceDiff = settings.getUpdatesDistance();
        this.locationUpdateInterval = settings.getUpdatesInterval();
    }

    @Override
    protected Location doInBackground(Void... params) {
        long minimumTime = System.currentTimeMillis() - locationUpdateInterval;
        return lastLocationFinder.getLastBestLocation(locationUpdateDistanceDiff, minimumTime);
    }

    @Override
    protected void onPostExecute(Location lastKnownLocation) {
        if (lastKnownLocation == null) {
        	return;
        }
        LocatorFactory.setLocation(lastKnownLocation);
    }

}
