package com.novoda.location.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LocationChangedHandlerShould {

    final Locator locator = mock(Locator.class);
    final LocationChangedHandler locationChanged = new LocationChangedHandler(locator);

    @Before
    public void setUp() throws Exception {
        LocatorFactory.setLocator(locator);
    }

    @After
    public void tearDown() throws Exception {
        LocatorFactory.setLocator(null);
    }

    @Test
    public void fail_gracefully_if_values_are_invalid() throws Exception {
        try {
            locationChanged.onNewChange(null);
        } catch (Exception e) {
            fail("This exception shouldn't have been propagated");
        }
    }

    @Test
    public void update_the_current_location_if_a_new_location_is_received() throws Exception {
        Intent intent = new Intent();
        Location newLocation = new Location("random provider");
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, newLocation);

        locationChanged.onNewChange(intent);

        verify(locator).setLocation(eq(newLocation));
    }

    @Test
    public void NOT_update_the_current_location_if_a_new_location_is_not_received() throws Exception {
        locationChanged.onNewChange(new Intent());

        verify(locator, never()).setLocation(any(Location.class));
    }

    @Test
    public void warn_the_locator_that_a_provider_status_has_changed() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, true);

        locationChanged.onNewChange(intent);

        verify(locator).providerStatusChanged();
    }

    @Test
    public void warn_the_locator_that_a_provider_status_has_changed_if_a_provider_has_been_disabled() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, false);

        locationChanged.onNewChange(intent);

        verify(locator).providerStatusChanged();
    }

    @Test
    public void NOT_broadcast_if_a_provider_status_has_not_changed() throws Exception {
        locationChanged.onNewChange(new Intent());

        verify(mock(Context.class), never()).sendBroadcast(any(Intent.class));
    }
}
