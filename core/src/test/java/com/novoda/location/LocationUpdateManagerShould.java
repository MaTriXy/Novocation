package com.novoda.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.provider.LocationProviderFactory;
import com.novoda.location.provider.LocationUpdateRequester;
import com.novoda.location.receiver.PassiveLocationChanged;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LocationUpdateManagerShould {

    final LocatorSettings settings = new LocatorSettings("", "");
    final Criteria criteria = mock(Criteria.class);
    final LocationManager locationManager = mock(LocationManager.class);
    final LocationProviderFactory locationProviderFactory = mock(LocationProviderFactory.class);
    final LocationUpdateRequester updateRequester = mock(LocationUpdateRequester.class);

    final PendingIntent activeUpdate = mock(PendingIntent.class);
    final PendingIntent passiveUpdate = mock(PendingIntent.class);
    LocationUpdateManager locationUpdateManager;

    @Before
    public void setUp() throws Exception {
        LocationUpdatesIntentFactory updatesIntentFactory = mock(LocationUpdatesIntentFactory.class);
        when(updatesIntentFactory.buildActive()).thenReturn(activeUpdate);
        when(updatesIntentFactory.buildPassive()).thenReturn(passiveUpdate);
        when(locationProviderFactory.getLocationUpdateRequester(eq(locationManager))).thenReturn(updateRequester);
        locationUpdateManager = new LocationUpdateManager(settings, locationManager, locationProviderFactory, updatesIntentFactory);
    }

    @Test
    public void request_active_locations_from_an_update_requester() throws Exception {
        locationUpdateManager.requestActiveLocationUpdates(criteria);

        verify(updateRequester).requestActiveLocationUpdates(eq(settings), eq(criteria), eq(activeUpdate));
    }

    @Test(expected = NoProviderAvailable.class)
    public void throw_an_exception_if_no_provider_is_available() throws Exception {
        doThrow(IllegalArgumentException.class).when(updateRequester).requestActiveLocationUpdates(eq(settings), eq(criteria), eq(activeUpdate));

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

}
