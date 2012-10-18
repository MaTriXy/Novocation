package com.novoda.location.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.provider.LastLocationFinder;
import com.novoda.location.provider.LocationProviderFactory;
import com.novoda.location.provider.store.SettingsDao;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class PassiveLocationChangedShould {

    static final Location INVALID_LOCATION = null;
    public static final int ZERO_LATITUDE = 0;
    public static final String PROVIDER_ONE = "provider one";

    final long now = System.currentTimeMillis();
    final PassiveLocationChanged passiveLocationChanged = spy(new PassiveLocationChanged());
    final Locator locator = mock(Locator.class);
    final Context context = Robolectric.getShadowApplication().getApplicationContext();
    final SettingsDao settingsDao = new LocationProviderFactory().getSettingsDao();
    final long outdated = now - settingsDao.getPassiveLocationInterval(context);

    @Before
    public void setUp() throws Exception {
        LocatorFactory.setLocator(locator);
    }

    @After
    public void tearDown() throws Exception {
        LocatorFactory.setLocator(null);
    }

    private Intent buildIntentWith(Location location) {
        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        return intent;
    }

    @Test
    public void update_the_current_location_if_its_part_of_the_intent() throws Exception {
        Location location = new Location(PROVIDER_ONE);
        Intent intent = buildIntentWith(location);

        passiveLocationChanged.onReceive(null, intent);

        verify(locator).setLocation(eq(location));
    }

    @Test
    public void NOT_update_the_current_location_if_its_invalid() throws Exception {
        Intent intent = buildIntentWith(INVALID_LOCATION);

        passiveLocationChanged.onReceive(null, intent);

        verify(locator, never()).setLocation(any(Location.class));
    }

    private void addCurrentLocation(long time, int latitude) {
        Location currentLocation = new Location(PROVIDER_ONE);
        currentLocation.setTime(time);
        currentLocation.setLatitude(latitude);
        addCurrentLocation(currentLocation);
    }

    private void addCurrentLocation(Location location) {
        when(locator.getLocation()).thenReturn(location);
    }

    private Location addLastBestLocation(long time, int latitude) {
        Location lastBestLocation = new Location(PROVIDER_ONE);
        lastBestLocation.setTime(time);
        lastBestLocation.setLatitude(latitude);
        addLastBestLocation(lastBestLocation);
        return lastBestLocation;
    }

    private void addLastBestLocation(Location lastBestLocation) {
        LastLocationFinder lastLocationFinder = mock(LastLocationFinder.class);
        when(lastLocationFinder.getLastBestLocation(anyLong())).thenReturn(lastBestLocation);
        when(passiveLocationChanged.getLastLocationFinder(any(LocationManager.class))).thenReturn(lastLocationFinder);
    }

    @Test
    public void update_the_location_if_the_current_one_is_outdated_and_beyond_the_distance_threshold() throws Exception {
        addCurrentLocation(outdated, ZERO_LATITUDE);
        Location lastBestLocation = addLastBestLocation(now, 123456);

        passiveLocationChanged.onReceive(context, new Intent());

        verify(locator).setLocation(eq(lastBestLocation));
    }

    @Test
    public void not_update_the_location_if_the_current_is_not_outdated() throws Exception {
        addCurrentLocation(outdated + 1000L, ZERO_LATITUDE);
        addLastBestLocation(now, ZERO_LATITUDE);

        passiveLocationChanged.onReceive(context, new Intent());

        verify(locator, never()).setLocation(any(Location.class));
    }

    @Test
    public void not_update_the_location_if_the_current_one_is_not_beyond_the_distance_threshold___even_if_its_outdated() throws Exception {
        addCurrentLocation(outdated, ZERO_LATITUDE);
        addLastBestLocation(now, ZERO_LATITUDE);

        passiveLocationChanged.onReceive(context, new Intent());

        verify(locator, never()).setLocation(any(Location.class));
    }

    @Test
    public void should_not_blow_up_if_the_current_location_is_invalid() throws Exception {
        addCurrentLocation(INVALID_LOCATION);
        addLastBestLocation(now, ZERO_LATITUDE);

        try {
            passiveLocationChanged.onReceive(context, new Intent());
        } catch (Exception e) {
            fail("Should not have propagated this: " + e);
        }
    }

    @Test
    public void should_not_blow_up_if_the_last_best_location_is_invalid() throws Exception {
        addCurrentLocation(outdated, ZERO_LATITUDE);
        addLastBestLocation(INVALID_LOCATION);

        try {
            passiveLocationChanged.onReceive(context, new Intent());
        } catch (Exception e) {
            fail("Should not have propagated this: " + e);
        }
    }
}
