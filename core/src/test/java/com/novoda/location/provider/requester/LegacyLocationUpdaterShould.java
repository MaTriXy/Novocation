package com.novoda.location.provider.requester;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.location.LocationManager;
import com.novoda.location.LocatorSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterOrEqual;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(NovocationTestRunner.class)
public class LegacyLocationUpdaterShould {

    final LocationManager locationManager = mock(LocationManager.class);
    final AlarmManager alarmManager = mock(AlarmManager.class);
    final LocatorSettings locatorSettings = new LocatorSettings("", "");
    final LegacyLocationUpdater updater = new LegacyLocationUpdater(locationManager, alarmManager);
    final PendingIntent pendingIntent = mock(PendingIntent.class);

    @Test
    public void set_an_inexact_repeating_non_waking_alarm_using_the_passive_provider_interval_settings() throws Exception {
        long passiveUpdatesInterval = locatorSettings.getPassiveUpdatesInterval();
        long triggerTime = System.currentTimeMillis() + passiveUpdatesInterval;
        GreaterOrEqual<Long> matchesTriggerTime = new GreaterOrEqual<Long>(triggerTime);

        updater.requestPassiveLocationUpdates(locatorSettings, pendingIntent);

        verify(alarmManager).setInexactRepeating(eq(AlarmManager.ELAPSED_REALTIME), longThat(matchesTriggerTime), eq(passiveUpdatesInterval), eq(pendingIntent));
    }
}
