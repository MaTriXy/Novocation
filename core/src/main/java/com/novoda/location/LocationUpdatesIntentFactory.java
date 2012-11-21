package com.novoda.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.novoda.location.receiver.PassiveLocationChanged;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

class LocationUpdatesIntentFactory {

    private final LocatorSettings settings;
    private final Context context;

    LocationUpdatesIntentFactory(LocatorSettings settings, Context context) {
        this.settings = settings;
        this.context = context;
    }

    PendingIntent buildActive() {
        Intent activeIntent = new Intent(Constants.ACTIVE_LOCATION_UPDATE_ACTION);
        return PendingIntent.getBroadcast(context, 0, activeIntent, FLAG_UPDATE_CURRENT);
    }

    PendingIntent buildPassive() {
        Intent passiveIntent = new Intent(context, PassiveLocationChanged.class);
        return PendingIntent.getBroadcast(context, 0, passiveIntent, FLAG_UPDATE_CURRENT);
    }

}
