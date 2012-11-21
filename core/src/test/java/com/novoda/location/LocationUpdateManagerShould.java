package com.novoda.location;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.provider.LastLocationFinder;
import com.novoda.location.provider.LocationProviderFactory;
import com.novoda.location.provider.LocationUpdater;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterOrEqual;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LocationUpdateManagerShould {

    static final Location INVALID_LOCATION = null;
    static final float UPDATES_DISTANCE = 200;
    static final long UPDATES_INTERVAL = 3 * 60 * 1000;

    final LocatorSettings settings = new LocatorSettings("", "");
    final Criteria criteria = mock(Criteria.class);
    final LocationManager locationManager = mock(LocationManager.class);
    final LocationProviderFactory locationProviderFactory = mock(LocationProviderFactory.class);
    final LocationUpdater updater = mock(LocationUpdater.class);
    final PendingIntent activeUpdate = mock(PendingIntent.class);
    final PendingIntent passiveUpdate = mock(PendingIntent.class);
    final LastLocationFinder lastLocationFinder = mock(LastLocationFinder.class);
    final Locator locator = mock(Locator.class);

    LocationUpdateManager locationUpdateManager;

    @Before
    public void setUp() throws Exception {
        LocationUpdatesIntentFactory updatesIntentFactory = mock(LocationUpdatesIntentFactory.class);
        when(updatesIntentFactory.buildActive()).thenReturn(activeUpdate);
        when(updatesIntentFactory.buildPassive()).thenReturn(passiveUpdate);
        when(locationProviderFactory.getLocationUpdater(eq(locationManager))).thenReturn(updater);
        when(locationProviderFactory.getLastLocationFinder(eq(locationManager))).thenReturn(lastLocationFinder);
        LocatorFactory.setLocator(locator);
        settings.setUpdatesDistance(UPDATES_DISTANCE);
        settings.setUpdatesInterval(UPDATES_INTERVAL);
        locationUpdateManager = new LocationUpdateManager(settings, locationManager, locationProviderFactory, updatesIntentFactory);
    }

    @After
    public void tearDown() throws Exception {
        LocatorFactory.setLocator(null);
    }

    @Test
    public void request_active_locations_from_an_update_requester() throws Exception {
        locationUpdateManager.requestActiveLocationUpdates(criteria);

        verify(updater).requestActiveLocationUpdates(eq(settings), eq(criteria), eq(activeUpdate));
    }

    @Test(expected = NoProviderAvailable.class)
    public void throw_an_exception_if_no_provider_is_available() throws Exception {
        doThrow(IllegalArgumentException.class).when(updater).requestActiveLocationUpdates(eq(settings), eq(criteria), eq(activeUpdate));

        locationUpdateManager.requestActiveLocationUpdates(criteria);
    }

    @Test
    public void remove_updates() throws Exception {
        locationUpdateManager.removeActiveLocationUpdates();

        verify(locationManager).removeUpdates(eq(activeUpdate));
    }

    @Test
    public void remove_passive_updates() throws Exception {
        locationUpdateManager.removePassiveLocationUpdates();

        verify(locationManager).removeUpdates(eq(passiveUpdate));
    }

    @Test
    public void get_the_last_best_location_using_the_updates_distance_and_interval_from_the_settings() throws Exception {
        long expectedMinimumTime = System.currentTimeMillis() - UPDATES_INTERVAL;

        locationUpdateManager.fetchLastKnownLocation();

        verify(lastLocationFinder).getLastBestLocation(longThat(new GreaterOrEqual<Long>(expectedMinimumTime)));
    }

    @Test
    public void not_set_a_location_if_its_invalid() throws Exception {
        when(lastLocationFinder.getLastBestLocation(anyLong())).thenReturn(INVALID_LOCATION);

        locationUpdateManager.fetchLastKnownLocation();

        verify(locator, never()).setLocation(any(Location.class));
    }

    @Test
    public void set_a_location_if_its_valid() throws Exception {
        Location location = new Location("test provider");

        when(lastLocationFinder.getLastBestLocation(anyLong())).thenReturn(location);

        locationUpdateManager.fetchLastKnownLocation();

        verify(locator).setLocation(eq(location));
    }

}
