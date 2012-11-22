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

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.Locator;

class ActiveLocationChangedHandler {

    private final Locator locator;

    ActiveLocationChangedHandler(Locator locator) {
        this.locator = locator;
    }

    void onNewChange(Intent intent) {
        if (intent != null) {
            if (providerStatusHasChanged(intent)) {
                locator.providerStatusChanged();
            }

            if (locationHasChanged(intent)) {
                locator.setLocation(getLocationFrom(intent));
            }
        }
    }


    private boolean providerStatusHasChanged(Intent intent) {
        return intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED);
    }

    private boolean locationHasChanged(Intent intent) {
        return intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED);
    }

    private Location getLocationFrom(Intent intent) {
        return (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
    }
}
