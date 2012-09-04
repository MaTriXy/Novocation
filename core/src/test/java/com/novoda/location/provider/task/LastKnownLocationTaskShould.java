package com.novoda.location.provider.task;

import android.location.Location;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LastLocationFinder;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.GreaterThan;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(NovocationTestRunner.class)
public class LastKnownLocationTaskShould {

    static final Void[] UNIMPORTANT_VALUES = null;
    static final Location INVALID_LOCATION = null;

    LastLocationFinder lastLocationFinder = mock(LastLocationFinder.class);
    LocatorSettings settings = new LocatorSettings("", "");
    LastKnownLocationTask task;
    int updatesDistance = 200;
    int updatesInterval = 60 * 3 * 1000;
    Locator locator = mock(Locator.class);

    @Before
    public void setUp() throws Exception {
        settings.setUpdatesDistance(updatesDistance);
        settings.setUpdatesInterval(updatesInterval);
        task = new LastKnownLocationTask(lastLocationFinder, settings);
        LocatorFactory.setLocator(locator);
    }

    @After
    public void tearDown() throws Exception {
        LocatorFactory.setLocator(null);
    }

    @Test
    public void get_the_last_best_location_using_the_updates_distance_and_interval_from_the_settings() throws Exception {
        long expectedMinimumTime = System.currentTimeMillis() - updatesInterval;

        task.doInBackground(UNIMPORTANT_VALUES);

        verify(lastLocationFinder).getLastBestLocation(eq(updatesDistance), longThat(new GreaterOrEqual<Long>(expectedMinimumTime)));
    }

    @Test
    public void cancel_location_updates_on_cancel() throws Exception {
        task.onCancelled();

        verify(lastLocationFinder).cancel();
    }

    @Test
    public void not_set_a_location_if_its_invalid() throws Exception {
        task.onPostExecute(INVALID_LOCATION);

        verify(locator, never()).setLocation(any(Location.class));
    }

    @Test
    public void set_a_location_if_its_valid() throws Exception {
        Location location = new Location("my provider");

        task.onPostExecute(location);

        verify(locator).setLocation(eq(location));
    }
}