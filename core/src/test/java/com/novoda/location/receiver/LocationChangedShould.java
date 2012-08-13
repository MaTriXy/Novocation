package com.novoda.location.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.Constants;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LocationChangedShould {

    final LocationChanged locationChangedSpy = spy(new LocationChanged());
    LocationChanged locationChanged = new LocationChanged();

    @Before
    public void setUp() throws Exception {
        Locator locator = LocatorFactory.getInstance();
        LocatorSettings locatorSettings = new LocatorSettings("", "");
        locator.prepare(mock(Context.class), locatorSettings);
    }

    @Test
    public void fail_gracefully_if_values_are_invalid() throws Exception {
        try {
            locationChanged.onReceive(null, null);
        } catch (Exception e) {
            fail("This exception shouldn't have been propagated");
        }
    }

    @Test
    public void update_the_current_location_if_a_new_location_is_received() throws Exception {
        Intent intent = new Intent();
        Location newLocation = new Location("random provider");
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, newLocation);

        locationChangedSpy.onReceive(null, intent);

        verify(locationChangedSpy).updateLocation(eq(intent));
    }

    @Test
    public void NOT_update_the_current_location_if_a_new_location_is_not_received() throws Exception {
        locationChangedSpy.onReceive(null, new Intent());

        verify(locationChangedSpy, never()).updateLocation(eq(new Intent()));
    }

    @Test
    public void broadcast_if_a_provider_has_been_enabled() throws Exception {
        Context context = mock(Context.class);

        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, true);
        locationChanged.onReceive(context, intent);

        Intent providerStatusChanged = new Intent(Constants.ACTIVE_LOCATION_UPDATE_PROVIDER_ENABLED_ACTION);
        verify(context).sendBroadcast(eq(providerStatusChanged));
    }

    @Test
    public void broadcast_if_a_provider_has_been_disabled() throws Exception {
        Context context = mock(Context.class);

        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
        locationChanged.onReceive(context, intent);

        Intent providerStatusChanged = new Intent(Constants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED_ACTION);
        verify(context).sendBroadcast(eq(providerStatusChanged));
    }

    @Test
    public void NOT_broadcast_if_a_provider_status_has_not_changed() throws Exception {
        locationChanged.onReceive(mock(Context.class), new Intent());

        verify(mock(Context.class), never()).sendBroadcast(any(Intent.class));
    }
}
