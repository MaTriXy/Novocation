package com.novoda.location.provider.finder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import javax.sound.sampled.LineListener;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(NovocationTestRunner.class)
public class SingleUpdateReceiverShould {

    static final Location INVALID_LOCATION = null;

    Context context = mock(Context.class);
    LocationManager locationManager = mock(LocationManager.class);
    GingerbreadLastLocationFinder lastLocationFinder = new GingerbreadLastLocationFinder(locationManager, context);
    BroadcastReceiver updateReceiver = lastLocationFinder.singleUpdateReceiver;
    LocationListener locationListener = mock(LocationListener.class);

    private void broadcastNewLocation(Location location) {
        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        updateReceiver.onReceive(context, intent);
    }

    @Test
    public void pass_the_location_received_to_the_listener() throws Exception {
        lastLocationFinder.setChangedLocationListener(locationListener);

        Location location = new Location("random provider");
        broadcastNewLocation(location);

        verify(locationListener).onLocationChanged(eq(location));
    }

    @Test
    public void not_pass_the_location_to_its_listener_if_the_location_received_is_invalid() throws Exception {
        lastLocationFinder.setChangedLocationListener(locationListener);

        broadcastNewLocation(INVALID_LOCATION);

        verify(locationListener, never()).onLocationChanged(any(Location.class));
    }

    @Test
    public void unregister_itself_even_if_the_location_is_not_valid() throws Exception {
        broadcastNewLocation(INVALID_LOCATION);
        verify(context).unregisterReceiver(eq(updateReceiver));
    }

    @Test
    public void stop_location_update_even_if_the_location_is_not_valid() throws Exception {
        broadcastNewLocation(INVALID_LOCATION);
        verify(locationManager).removeUpdates(any(PendingIntent.class));
    }
}
