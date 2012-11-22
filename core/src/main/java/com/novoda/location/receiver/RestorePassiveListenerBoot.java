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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.util.ApiLevelDetector;

//TODO extract the logic out of this receiver
public class RestorePassiveListenerBoot extends BroadcastReceiver {

    private ApiLevelDetector apiLevelDetector;

    public RestorePassiveListenerBoot() {}

    public RestorePassiveListenerBoot(ApiLevelDetector apiLevelDetector) {
        this.apiLevelDetector = apiLevelDetector;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (getSettings().shouldEnablePassiveUpdates()) {
            startPassiveLocationUpdates(c);
        }
    }

    private void startPassiveLocationUpdates(Context c) {
        new RestorePassiveListenerBootHandler(getApiLevelDetector()).startPassiveLocationUpdates(c);
    }

    private ApiLevelDetector getApiLevelDetector() {
        return apiLevelDetector != null ? apiLevelDetector : new ApiLevelDetector();
    }

    private LocatorSettings getSettings() {
        return LocatorFactory.getInstance().getSettings();
    }

}