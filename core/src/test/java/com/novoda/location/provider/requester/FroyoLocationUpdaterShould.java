package com.novoda.location.provider.requester;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;
import com.novoda.location.LocatorSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class FroyoLocationUpdaterShould {

    public static final String TEST_PROVIDER = "test provider";
    final static long UPDATE_TIME = 1000 * 60;
    final static float UPDATE_DISTANCE = 300.50F;

    final LocationManager locationManager = mock(LocationManager.class);
    final FroyoLocationUpdater updater = new FroyoLocationUpdater(locationManager);
    final LocatorSettings settings = new LocatorSettings("");
    final Criteria criteria = mock(Criteria.class);
    final PendingIntent pendingIntent = mock(PendingIntent.class);

    @Before
    public void setUp() throws Exception {
        settings.setUpdatesInterval(UPDATE_TIME);
        settings.setUpdatesDistance(UPDATE_DISTANCE);
    }

    @Test
    public void request_location_updates_from_the_location_manager_if_the_given_criteria_matches_an_enabled_provider() throws Exception {
        when(locationManager.getBestProvider(criteria, true)).thenReturn(TEST_PROVIDER);

        updater.startActiveLocationUpdates(settings, criteria, pendingIntent);

        verify(locationManager).requestLocationUpdates(eq(TEST_PROVIDER), eq(UPDATE_TIME), eq(UPDATE_DISTANCE), eq(pendingIntent));
    }

    @Test
    public void NOT_request_location_updates_from_the_location_manager_if_the_given_criteria_does_not_match_an_enabled_provider() throws Exception {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(null);

        updater.startActiveLocationUpdates(settings, criteria, pendingIntent);

        verify(locationManager, never()).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void request_passive_location_updates_using_the_passive_provider() throws Exception {
        long passiveTime = 60 * 5 * 1000;
        settings.setPassiveUpdatesInterval(passiveTime);

        float passiveDistance = 500;
        settings.setPassiveUpdatesDistance(passiveDistance);

        updater.startPassiveLocationUpdates(settings, pendingIntent);

        verify(locationManager).requestLocationUpdates(eq(LocationManager.PASSIVE_PROVIDER), eq(passiveTime), eq(passiveDistance), eq(pendingIntent));
    }
}
