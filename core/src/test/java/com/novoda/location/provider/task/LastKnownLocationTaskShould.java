package com.novoda.location.provider.task;

import android.location.Location;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LastLocationFinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterOrEqual;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class LastKnownLocationTaskShould {

    static final Void[] UNIMPORTANT_VALUES = null;
    static final Location INVALID_LOCATION = null;
    static final float UPDATES_DISTANCE = 200;
    static final long UPDATES_INTERVAL = 60 * 3 * 1000;

    final LastLocationFinder lastLocationFinder = mock(LastLocationFinder.class);
    final LocatorSettings settings = new LocatorSettings("", "");
    final Locator locator = mock(Locator.class);
    LastKnownLocationTask task;

    @Before
    public void setUp() throws Exception {
        settings.setUpdatesDistance(UPDATES_DISTANCE);
        settings.setUpdatesInterval(UPDATES_INTERVAL);
        task = new LastKnownLocationTask(lastLocationFinder, settings);
        LocatorFactory.setLocator(locator);
    }

    @After
    public void tearDown() throws Exception {
        LocatorFactory.setLocator(null);
    }

    @Test
    public void get_the_last_best_location_using_the_updates_distance_and_interval_from_the_settings() throws Exception {
        long expectedMinimumTime = System.currentTimeMillis() - UPDATES_INTERVAL;

        task.doInBackground(UNIMPORTANT_VALUES);

        verify(lastLocationFinder).getLastBestLocation(eq(UPDATES_DISTANCE), longThat(new GreaterOrEqual<Long>(expectedMinimumTime)));
    }

    @Test
    public void not_set_a_location_if_its_invalid() throws Exception {
        task.onPostExecute(INVALID_LOCATION);

        verify(locator, never()).setLocation(any(Location.class));
    }

    @Test
    public void set_a_location_if_its_valid() throws Exception {
        Location location = new Location("test provider");

        task.onPostExecute(location);

        verify(locator).setLocation(eq(location));
    }
}