package com.novoda.location.provider.finder;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import com.novoda.location.provider.LastLocationFinder;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import robolectricsetup.NovocationTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LegacyLastLocationFinderShould {

    static final int MIN_ACCURACY = 100;
    static final long MIN_TIME = System.currentTimeMillis() - (1000 * 60 * 4);
    static final int TIME_DELTA = 1;
    static final long RECENT_TIME = MIN_TIME + TIME_DELTA;
    static final long LESS_RECENT_TIME = MIN_TIME - TIME_DELTA;

    static final Location INVALID_LOCATION = null;
    static final float INVALID_ACCURACY = Float.MAX_VALUE;
    static final String INVALID_VALUE = null;

    static final String PROVIDER_ONE = "provider one";
    static final String PROVIDER_TWO = "provider two";

    final Context context = mock(Context.class);
    final LocationManager locationManager = mock(LocationManager.class);
    final List<String> providers = new ArrayList<String>();
    final LastLocationFinder lastLocationFinder = new LegacyLastLocationFinder(locationManager, context);


    @Before
    public void setUp() throws Exception {
        when(locationManager.getProviders(true)).thenReturn(providers);
    }

    private Location getLastBestLocation() {
        return lastLocationFinder.getLastBestLocation(MIN_ACCURACY, MIN_TIME);
    }

    private Location addLocationToManager(long timestamp, float accuracy, String provider) {
        Location location = new Location(provider);
        location.setTime(timestamp);
        location.setAccuracy(accuracy);
        providers.add(provider);
        when(locationManager.getLastKnownLocation(provider)).thenReturn(location);
        return location;
    }

    @Test
    public void find_an_invalid_location_if_no_location_is_available() throws Exception {
        providers.add(PROVIDER_ONE);
        when(locationManager.getLastKnownLocation(PROVIDER_ONE)).thenReturn(INVALID_LOCATION);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(INVALID_LOCATION));
    }

    @Test
    public void find_a_location_if_its_more_recent_than_the_minimum_time_even_if_accurate() throws Exception {
        Location location = addLocationToManager(RECENT_TIME, MIN_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(location));
    }

    @Test
    public void find_an_INVALID_location_if_its_more_recent_than_the_minimum_time() throws Exception {
        addLocationToManager(RECENT_TIME, INVALID_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(INVALID_LOCATION));
    }

    @Test
    public void find_an_INVALID_location_if_no_locations_time_is_different_from_the_minimum_time_even_if_accurate() throws Exception {
        addLocationToManager(MIN_TIME, MIN_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(INVALID_LOCATION));
    }

    @Test
    public void find_an_INVALID_location_if_no_locations_time_is_different_from_the_minimum_time() throws Exception {
        addLocationToManager(MIN_TIME, INVALID_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(INVALID_LOCATION));
    }

    @Test
    public void find_a_location_even_if_its_time_is_below_the_minimum() throws Exception {
        Location location = addLocationToManager(LESS_RECENT_TIME, MIN_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(location));
    }

    @Test
    public void find_a_location_even_if_its_time_is_below_the_minimum_and_its_accuracy_invalid() throws Exception {
        Location location = addLocationToManager(LESS_RECENT_TIME, INVALID_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(location));
    }

    @Test
    public void find_an_INVALID_location_if_no_location_has_non_valid_values() throws Exception {
        addLocationToManager(Long.MIN_VALUE, INVALID_ACCURACY, PROVIDER_ONE);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(INVALID_LOCATION));
    }

    @Test
    public void find_the_better_location_if_its_the_first() throws Exception {
        Location firstLocation = addLocationToManager(RECENT_TIME, MIN_ACCURACY, PROVIDER_ONE);
        addLocationToManager(LESS_RECENT_TIME, MIN_ACCURACY, PROVIDER_TWO);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(firstLocation));
    }

    @Test
    public void find_the_better_location_if_its_the_second() throws Exception {
        addLocationToManager(LESS_RECENT_TIME, MIN_ACCURACY, PROVIDER_ONE);
        Location secondLocation = addLocationToManager(RECENT_TIME, MIN_ACCURACY, PROVIDER_TWO);
        Location lastBestLocation = getLastBestLocation();
        assertThat(lastBestLocation, is(secondLocation));
    }

    @Test
    public void request_location_updates_if_the_best_location_time_is_below_the_minimum_time() throws Exception {
        addLocationListener();
        addLocationToManager(LESS_RECENT_TIME, MIN_ACCURACY, PROVIDER_ONE);
        addBestProviderForCriteria(PROVIDER_ONE);

        getLastBestLocation();

        verifyLocationUpdatesAreRequestedFor(PROVIDER_ONE);
    }

    @Test
    public void request_location_updates_if_the_best_location_accuracy_is_worst_than_the_minimum_required() throws Exception {
        addLocationListener();
        addLocationToManager(RECENT_TIME, MIN_ACCURACY * 2, PROVIDER_ONE);
        addBestProviderForCriteria(PROVIDER_ONE);

        getLastBestLocation();

        verifyLocationUpdatesAreRequestedFor(PROVIDER_ONE);
    }

    @Test
    public void not_request_location_updates_if_no_provider_matches_the_required_criteria() throws Exception {
        addLocationListener();
        addBestProviderForCriteria(INVALID_VALUE);

        getLastBestLocation();

        verifyLocationUpdatesAre_NOT_Requested();
    }

    @Test
    public void not_request_location_updates_if_a_location_satisfying_the_time_and_distance_requirements_is_found() throws Exception {
        addLocationListener();
        addLocationToManager(RECENT_TIME, MIN_ACCURACY / 2, PROVIDER_ONE);
        addBestProviderForCriteria(INVALID_VALUE);

        getLastBestLocation();

        verifyLocationUpdatesAre_NOT_Requested();
    }

    private void addLocationListener() {
        LocationListener locationListener = mock(LocationListener.class);
        lastLocationFinder.setChangedLocationListener(locationListener);
    }

    private void addBestProviderForCriteria(String provider) {
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn(provider);
    }

    private void verifyLocationUpdatesAreRequestedFor(String provider) {
        verify(locationManager).requestLocationUpdates(eq(provider), anyLong(), anyLong(), any(LocationListener.class), any(Looper.class));
    }

    private void verifyLocationUpdatesAre_NOT_Requested() {
        verify(locationManager, never()).requestLocationUpdates(anyString(), anyLong(), anyLong(), any(LocationListener.class), any(Looper.class));
    }

    @Test
    public void stop_location_updates_when_asked_to() throws Exception {
        lastLocationFinder.cancel();

        verify(locationManager).removeUpdates(any(LocationListener.class));
    }

}
