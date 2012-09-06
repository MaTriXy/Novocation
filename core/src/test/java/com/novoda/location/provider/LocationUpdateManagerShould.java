package com.novoda.location.provider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import com.novoda.location.Constants;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.receiver.PassiveLocationChanged;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LocationUpdateManagerShould {

    final LocatorSettings settings = new LocatorSettings("", "");
    final Criteria criteria = mock(Criteria.class);
    final Context context = mock(Context.class);
    final LocationManager locationManager = mock(LocationManager.class);
    final LocationProviderFactory locationProviderFactory = mock(LocationProviderFactory.class);
    final LocationUpdateRequester updateRequester = mock(LocationUpdateRequester.class);

    PendingIntent locationListenerPendingIntent;
    PendingIntent locationListenerPassivePendingIntent;
    LocationUpdateManager locationUpdateManager;

    @Before
    public void setUp() throws Exception {
        when(locationProviderFactory.getLocationUpdateRequester(eq(locationManager))).thenReturn(updateRequester);

        Intent activeIntent = new Intent(Constants.ACTIVE_LOCATION_UPDATE_ACTION);
        activeIntent.setPackage(settings.getPackageName());
        locationListenerPendingIntent = PendingIntent.getBroadcast(context, 0, activeIntent, FLAG_UPDATE_CURRENT);

        Intent passiveIntent = new Intent(context, PassiveLocationChanged.class);
        passiveIntent.setPackage(settings.getPackageName());
        locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent, FLAG_UPDATE_CURRENT);

        locationUpdateManager = new LocationUpdateManager(settings, locationManager, locationProviderFactory, criteria, locationListenerPendingIntent, locationListenerPassivePendingIntent);
    }

    @Test
    public void request_active_locations_from_an_update_requester() throws Exception {
        locationUpdateManager.requestActiveLocationUpdates();

        verify(updateRequester).requestActiveLocationUpdates(eq(settings), eq(criteria), eq(locationListenerPendingIntent));
    }

    @Test(expected = NoProviderAvailable.class)
    public void throw_an_exception_if_no_provider_is_available() throws Exception {
        doThrow(IllegalArgumentException.class).when(updateRequester).requestActiveLocationUpdates(eq(settings), eq(criteria), eq(locationListenerPendingIntent));

        locationUpdateManager.requestActiveLocationUpdates();
    }

    @Test
    public void remove_updates() throws Exception {
        locationUpdateManager.removeActiveLocationUpdates();

        verify(locationManager).removeUpdates(eq(locationListenerPendingIntent));
    }

    @Test
    public void remove_passive_updates() throws Exception {
        locationUpdateManager.removePassiveLocationUpdates();

        verify(locationManager).removeUpdates(eq(locationListenerPassivePendingIntent));
    }

}
