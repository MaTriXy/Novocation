package com.novoda.location;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class DefaultLocatorShould {

    public static final String PROVIDER = "test provider";
    public static final String UPDATE_ACTION = "action.location_updated";
    public static final Location INVALID_LOCATION = null;

    final DefaultLocator locator = new DefaultLocator();
    final Context context = spy(Robolectric.getShadowApplication().getApplicationContext());
    final LocatorSettings settings = new LocatorSettings(UPDATE_ACTION);
    final Location worstLocation = new Location(PROVIDER);
    final Location betterLocation = new Location(PROVIDER);
    final LocationManager locationManager = mock(LocationManager.class);
    final LocationUpdateManager updateManager = mock(LocationUpdateManager.class);

    @Before
    public void setUp() throws Exception {
        doReturn(locationManager).when(context).getSystemService(eq(Context.LOCATION_SERVICE));
        locator.prepare(context, settings);
        worstLocation.setTime(0);
        betterLocation.setTime(System.currentTimeMillis());
        locator.setLocationUpdateManager(updateManager);
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
    public void broadcast_when_a_location_is_updated_with_the_update_action_from_settings() throws Exception {
        locator.setLocation(betterLocation);

        Intent locationUpdated = new Intent();
        locationUpdated.setAction(UPDATE_ACTION);

        verify(context).sendBroadcast(eq(locationUpdated));
    }

    @Test
    public void find_the_last_known_location_when_starting_location_updates_if_its_currently_location_is_invalid() throws Exception {
        locator.setLocation(INVALID_LOCATION);

        locator.startLocationUpdates();

        verify(updateManager).fetchLastKnownLocation();
    }

    @Test
    public void broadcast_that_the_location_is_updated_when_starting_location_updates_and_its_current_location_is_valid() throws Exception {
        locator.setLocation(new Location(PROVIDER));

        locator.startLocationUpdates();

        Intent locationUpdated = new Intent();
        locationUpdated.setAction(UPDATE_ACTION);

        //2 broadcasts occur as setting a location will also broadcast
        verify(context, times(2)).sendBroadcast(eq(locationUpdated));
    }

    @Test
    public void use_criteria_for_fine_accuracy_when_chosen_to_use_gps_in_the_settings() throws Exception {
        settings.setUseGps(true);

        locator.startLocationUpdates();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        verify(updateManager).requestActiveLocationUpdates(eq(criteria));
    }

    @Test
    public void use_criteria_fow_low_power_when_chosen_NOT_to_use_gps_in_the_settings() throws Exception {
        settings.setUseGps(false);

        locator.startLocationUpdates();

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        verify(updateManager).requestActiveLocationUpdates(eq(criteria));
    }

    @Test
    public void retrieve_a_network_location_if_gps_is_the_best_provider_when_starting_a_location_update() throws Exception {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(LocationManager.GPS_PROVIDER);
        when(locationManager.isProviderEnabled(eq(LocationManager.NETWORK_PROVIDER))).thenReturn(true);

        locator.startLocationUpdates();

        verify(locationManager).requestLocationUpdates(eq(LocationManager.NETWORK_PROVIDER), anyLong(), anyFloat(), any(LocationListener.class));
    }

    @Test
    public void only_retrieve_a_network_location_if_gps_is_the_best_provider_AND_the_network_provider_is_enabled_when_starting_a_location_update() throws Exception {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(LocationManager.GPS_PROVIDER);
        when(locationManager.isProviderEnabled(eq(LocationManager.NETWORK_PROVIDER))).thenReturn(false);

        locator.startLocationUpdates();

        verify(locationManager, never()).requestLocationUpdates(eq(LocationManager.NETWORK_PROVIDER), anyLong(), anyFloat(), any(LocationListener.class));
    }

    @Test
    public void remove_a_network_location_listener_if_GPS_is_the_best_provider_when_starting_a_location_update() throws Exception {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(LocationManager.GPS_PROVIDER);

        locator.startLocationUpdates();

        verify(locationManager).removeUpdates(any(LocationListener.class));
    }

    @Test
    public void NOT_remove_a_network_location_listener_if_NETWORK_is_the_best_provider_when_starting_a_location_update() throws Exception {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(LocationManager.NETWORK_PROVIDER);

        locator.startLocationUpdates();

        verify(locationManager, never()).removeUpdates(any(LocationListener.class));
    }

    @Test
    public void NOT_remove_a_network_location_listener_if_no_provider_is_enabled_when_starting_a_location_update() throws Exception {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(null);

        locator.startLocationUpdates();

        verify(locationManager, never()).removeUpdates(any(LocationListener.class));
    }

    @Test
    public void remove_active_location_updates_when_a_provider_status_has_changed() throws Exception {
        locator.startLocationUpdates();

        locator.providerStatusChanged();

        verify(updateManager).removeActiveLocationUpdates();
    }

    @Test
    public void request_active_location_updates_when_a_provider_status_has_changed() throws Exception {
        locator.startLocationUpdates();

        locator.providerStatusChanged();

        //2 because start location updates also requests active location updates
        verify(updateManager, times(2)).requestActiveLocationUpdates(any(Criteria.class));
    }

    @Test
    public void NOT_act_when_a_provider_status_changes_if_location_updates_have_not_been_started() throws Exception {
        locator.stopLocationUpdates();

        locator.providerStatusChanged();

        verify(updateManager, never()).requestActiveLocationUpdates(any(Criteria.class));
    }

    @Test
    public void remove_updates_when_stopping_location_updates() throws Exception {
        locator.stopLocationUpdates();

        verify(updateManager).removeActiveLocationUpdates();
    }

    @Test
    public void request_passive_updates_when_stopping_location_updates() throws Exception {
        locator.stopLocationUpdates();

        verify(updateManager).requestPassiveLocationUpdates();
    }

    @Test
    public void remove_the_one_shot_location_listener_when_stopping_location_updates() throws Exception {
        locator.stopLocationUpdates();

        verify(locationManager).removeUpdates(any(LocationListener.class));
    }

    @Test
    public void respect_the_location_updates_settings_when_starting_location_updates() throws Exception {
        settings.setUpdateOnLocationChange(false);

        locator.startLocationUpdates();

        verify(updateManager, never()).requestActiveLocationUpdates(any(Criteria.class));
    }

    @Test
    public void respect_the_location_updates_settings_when_stopping_location_updates() throws Exception {
        settings.setUpdateOnLocationChange(false);

        locator.stopLocationUpdates();

        verify(updateManager, never()).removeActiveLocationUpdates();
    }

}
