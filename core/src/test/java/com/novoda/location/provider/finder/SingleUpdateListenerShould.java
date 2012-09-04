package com.novoda.location.provider.finder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(NovocationTestRunner.class)
public class SingleUpdateListenerShould {

    static final Location INVALID_LOCATION = null;
    Context context = mock(Context.class);
    LocationManager locationManager = mock(LocationManager.class);
    LegacyLastLocationFinder lastLocationFinder = new LegacyLastLocationFinder(locationManager, context);
    LocationListener locationListener = mock(LocationListener.class);

    @Test
    public void unregister_itself_from_the_location_manager() throws Exception {
        lastLocationFinder.singleUpdateListener.onLocationChanged(null);

        verify(locationManager).removeUpdates(eq(lastLocationFinder.singleUpdateListener));
    }

    @Test
    public void not_call_the_location_listener_if_the_location_is_invalid() throws Exception {
        lastLocationFinder.setChangedLocationListener(locationListener);

        lastLocationFinder.singleUpdateListener.onLocationChanged(INVALID_LOCATION);

        verify(locationListener, never()).onLocationChanged(any(Location.class));
    }

    @Test
    public void call_the_location_listener_if_the_location_is_valid() throws Exception {
        lastLocationFinder.setChangedLocationListener(locationListener);
        Location location = mock(Location.class);

        lastLocationFinder.singleUpdateListener.onLocationChanged(location);

        verify(locationListener).onLocationChanged(eq(location));
    }

}
