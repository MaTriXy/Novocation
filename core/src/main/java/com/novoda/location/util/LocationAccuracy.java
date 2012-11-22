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
package com.novoda.location.util;

import android.location.Location;

import com.novoda.location.LocatorSettings;

public class LocationAccuracy {

    private final LocatorSettings settings;

    public LocationAccuracy(LocatorSettings settings) {
        this.settings = settings;
    }

    public boolean isBetterLocation(Location newLocation, Location currentLocation) {
        if (currentLocation == null) {
            return true;
        }else if (newLocation == null) {
            return false;
        } else {
            long timeDelta = newLocation.getTime() - currentLocation.getTime();
            int accuracyDelta = (int) (newLocation.getAccuracy() - currentLocation.getAccuracy());

            if (isSignificantlyNewer(timeDelta)) {
                return true;
            } else if (isSignificantlyOlder(timeDelta)) {
                return false;
            } else if (isMoreAccurate(accuracyDelta)) {
                return true;
            } else if (isABadButAcceptableLocation(timeDelta, accuracyDelta, newLocation, currentLocation)) {
                return true;
            }
        }
        return false;
    }

    private boolean isABadButAcceptableLocation(long timeDelta, int accuracyDelta, Location newLocation, Location currentLocation) {
        boolean isNewer = timeDelta > 0;
        boolean isBellowBadAccuracyThreshold = accuracyDelta < settings.getBadAccuracyThreshold();
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentLocation.getProvider());

        return isNewer && isBellowBadAccuracyThreshold && isFromSameProvider;
    }

    private boolean isMoreAccurate(int accuracyDelta) {
        return accuracyDelta < 0;
    }

    private boolean isSignificantlyNewer(long timeDelta) {
        return timeDelta > settings.getUpdatesInterval();
    }

    private boolean isSignificantlyOlder(long timeDelta) {
        return timeDelta < -settings.getUpdatesInterval();
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
