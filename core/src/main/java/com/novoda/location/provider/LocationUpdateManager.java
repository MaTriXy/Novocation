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
package com.novoda.location.provider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import com.novoda.location.Constants;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.provider.task.LastKnownLocationTask;
import com.novoda.location.receiver.PassiveLocationChanged;
import com.novoda.location.util.ApiLevelDetector;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class LocationUpdateManager {

    private final LocationProviderFactory locationProviderFactory = new LocationProviderFactory();
    private final LocatorSettings settings;
    private final Criteria criteria;
    private final LocationManager locationManager;
    private final LocationUpdateRequester locationUpdateRequester;
    private final PendingIntent locationListenerPendingIntent;
    private final PendingIntent locationListenerPassivePendingIntent;
    private AsyncTask<Void, Void, Location> lastKnownLocationTask;
    
    public LocationUpdateManager(LocatorSettings settings, Criteria criteria, Context context, LocationManager locationManager) {
    	this.settings = settings;
    	this.criteria = criteria;
    	this.locationManager = locationManager;
    	locationUpdateRequester = locationProviderFactory.getLocationUpdateRequester(locationManager);

        Intent activeIntent = new Intent(Constants.ACTIVE_LOCATION_UPDATE_ACTION);
        activeIntent.setPackage(settings.getPackageName());
        locationListenerPendingIntent = PendingIntent.getBroadcast(context, 0, activeIntent, FLAG_UPDATE_CURRENT);

        Intent passiveIntent = new Intent(context, PassiveLocationChanged.class);
        passiveIntent.setPackage(settings.getPackageName());
        locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, FLAG_UPDATE_CURRENT);
    }

	public void requestActiveLocationUpdates() throws NoProviderAvailable {
		try { 
			locationUpdateRequester.requestActiveLocationUpdates(
                    settings, criteria,
			                                                     locationListenerPendingIntent);
		} catch(IllegalArgumentException iae) {
			throw new NoProviderAvailable();
		}
	}

	public void requestPassiveLocationUpdates() {
		if (ApiLevelDetector.supportsFroyo() && settings.shouldEnablePassiveUpdates()) {
			locationUpdateRequester.requestPassiveLocationUpdates(settings, locationListenerPassivePendingIntent);
		}
	}
	
	public void removeUpdates() {
		locationManager.removeUpdates(locationListenerPendingIntent);
	}
	
	public void removePassiveUpdates() {
		locationManager.removeUpdates(locationListenerPassivePendingIntent);
	}
    
    public void fetchLastKnownLocation(Context context) {
    	LastLocationFinder finder = locationProviderFactory.getLastLocationFinder(locationManager, context);
        lastKnownLocationTask = new LastKnownLocationTask(finder, settings);
        lastKnownLocationTask.execute();
    }
    
	public void stopFetchLastKnownLocation() {
		if (lastKnownLocationTask == null) {
			return;
        }
		lastKnownLocationTask.cancel(true);
	}

}
