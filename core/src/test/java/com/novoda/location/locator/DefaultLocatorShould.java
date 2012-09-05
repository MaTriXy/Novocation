package com.novoda.location.locator;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LocationUpdateManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(NovocationTestRunner.class)
public class DefaultLocatorShould {

    public static final String PROVIDER = "test provider";
    public static final String UPDATE_ACTION = "action.location_updated";
    public static final String PACKAGE_NAME = "com.example";

    DefaultLocator locator = new DefaultLocator();
    Context context = mock(Context.class);
    LocatorSettings settings = new LocatorSettings(PACKAGE_NAME, UPDATE_ACTION);
    Location worstLocation = new Location(PROVIDER);
    Location betterLocation = new Location(PROVIDER);
    LocationManager locationManager = mock(LocationManager.class);

    @Before
    public void setUp() throws Exception {
        when(context.getSystemService(eq(Context.LOCATION_SERVICE))).thenReturn(locationManager);
        locator.prepare(context, settings);
        worstLocation.setTime(0);
        betterLocation.setTime(System.currentTimeMillis());
    }

    @Test
    public void replace_a_bad_location_with_a_better_one() throws Exception {
        locator.setLocation(worstLocation);
        locator.setLocation(betterLocation);

        assertThat(locator.getLocation(), is(betterLocation));
    }

    @Test
    public void NOT_replace_a_bad_location_with_a_better_one() throws Exception {
        locator.setLocation(betterLocation);
        locator.setLocation(worstLocation);

        assertThat(locator.getLocation(), is(betterLocation));
    }

    @Test
    public void broadcast_when_a_location_is_updated_with_the_settings_action_and_package_name() throws Exception {
        locator.setLocation(betterLocation);

        Intent locationUpdated = new Intent();
        locationUpdated.setAction(UPDATE_ACTION);
        locationUpdated.setPackage(PACKAGE_NAME);

        verify(context).sendBroadcast(eq(locationUpdated));
    }


    @Test
    public void testName() throws Exception {
        LocationUpdateManager updateManager = mock(LocationUpdateManager.class);

        locator.setLocationUpdateManager(updateManager);

        locator.stopLocationUpdates();

        verify(updateManager).removeUpdates();
        verify(updateManager).stopFetchLastKnownLocation();
        verify(updateManager).requestPassiveLocationUpdates();
    }

    @Ignore
    @Test
    public void testb() throws Exception {
        LocationUpdateManager updateManager = mock(LocationUpdateManager.class);

        locator.setLocationUpdateManager(updateManager);

        locator.startLocationUpdates();
    }
}
