package com.novoda.location.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.updater.LocationUpdater;
import com.novoda.location.provider.updater.LocationUpdaterFactory;
import com.novoda.location.util.ApiLevelDetector;

public class RestorePassiveListenerBootHandler {

    final ApiLevelDetector apiLevelDetector;

    public RestorePassiveListenerBootHandler(ApiLevelDetector apiLevelDetector) {
        this.apiLevelDetector = apiLevelDetector;
    }

    public void startPassiveLocationUpdates(Context context) {
        LocationUpdater locationUpdater = getLocationUpdater(context);
        locationUpdater.startPassiveLocationUpdates(getSettings(), createPendingIntent(context));
    }

    private LocationUpdater getLocationUpdater(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationUpdaterFactory locationUpdaterFactory = new LocationUpdaterFactory(lm, apiLevelDetector, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
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

}
