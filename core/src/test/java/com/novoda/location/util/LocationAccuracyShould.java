package com.novoda.location.util;

import android.location.Location;
import com.novoda.location.LocatorSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(NovocationTestRunner.class)
public class LocationAccuracyShould {

    private static final String NOT_IMPORTANT = "";
    private static final Location INVALID_LOCATION = null;
    private static final int HIGH_ACCURACY = 50;
    private static final long MEDIUM_ACCURACY = 180;
    private static final int LOW_ACCURACY = 600;
    private static final long ONE_SECOND = 1000L;
    private static final long LOCATION_EXPIRY_TIME = ONE_SECOND * 60 * 4;
    private static final long NOW = System.currentTimeMillis();
    private static final long ONE_SECOND_AGO = NOW - ONE_SECOND;
    private static final long OUTDATED = NOW - (LOCATION_EXPIRY_TIME + 1L);

    LocatorSettings settings = new LocatorSettings(NOT_IMPORTANT, NOT_IMPORTANT);
    LocationAccuracy locationAccuracy = new LocationAccuracy(settings);
    Location newLocation = new Location(NOT_IMPORTANT);
    Location currentLocation = new Location(NOT_IMPORTANT);

    @Before
    public void setUp() throws Exception {
        settings.setUpdatesInterval(LOCATION_EXPIRY_TIME);
    }

    @Test
    public void be_better_if_the_current_location_is_invalid() {
        currentLocation = INVALID_LOCATION;
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_current_location_is_the_same_as_the_new_one() {
        currentLocation = newLocation;
        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_current_location_time_is_outdated_by_the_settings_preference() {
        currentLocation.setTime(OUTDATED);
        newLocation.setTime(NOW);
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_time_is_outdated_by_the_settings_preference() {
        currentLocation.setTime(NOW);
        newLocation.setTime(OUTDATED);
        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_accuracy_value_is_higher() {
        newLocation.setAccuracy(HIGH_ACCURACY);
        currentLocation.setAccuracy(LOW_ACCURACY);
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_accuracy_value_is_lower() {
        newLocation.setAccuracy(HIGH_ACCURACY);
        currentLocation.setAccuracy(HIGH_ACCURACY / 2);
        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_accuracy_value_is_higher_even_if_its_not_more_recent() {
        newLocation.setAccuracy(HIGH_ACCURACY);
        currentLocation.setAccuracy(LOW_ACCURACY);

        newLocation.setTime(ONE_SECOND_AGO);
        currentLocation.setTime(NOW);

        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void be_better_if_more_recent_even_if_the_new_location_accuracy_is_the_same() {
        newLocation.setAccuracy(HIGH_ACCURACY);
        currentLocation.setAccuracy(HIGH_ACCURACY);

        newLocation.setTime(NOW);
        currentLocation.setTime(ONE_SECOND_AGO);

        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_even_if_more_recent_but_the_new_location_accuracy_is_lower() {
        newLocation.setAccuracy(LOW_ACCURACY);
        currentLocation.setAccuracy(HIGH_ACCURACY);

        newLocation.setTime(NOW);
        currentLocation.setTime(ONE_SECOND_AGO);

        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_more_recent_even_if_the_new_location_accuracy_is_worse_but_below_a_minimum_accuracy_threshold() {
        newLocation.setAccuracy(MEDIUM_ACCURACY);
        currentLocation.setAccuracy(HIGH_ACCURACY);

        newLocation.setTime(NOW);
        currentLocation.setTime(ONE_SECOND_AGO);

        assertThat(locationIsBetter(), is(true));
    }


    @Test
    public void not_be_better_if_more_recent___below_a_minimum_accuracy_threshold___but_from_a_different_provider() {
        newLocation.setAccuracy(MEDIUM_ACCURACY);
        currentLocation.setAccuracy(HIGH_ACCURACY);

        newLocation.setTime(NOW);
        currentLocation.setTime(ONE_SECOND_AGO);

        newLocation.setProvider("different provider");

        assertThat(locationIsBetter(), is(false));
    }

    private boolean locationIsBetter() {
        return locationAccuracy.isBetterLocation(newLocation, currentLocation);
    }

}
