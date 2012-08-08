package com.novoda.location.util;

import android.location.Location;
import com.novoda.location.LocatorSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(NovocationTestRunner.class)
public class LocationAccuracyShould {

    private static final String NOT_IMPORTANT = "";
    private static final String NOT_IMPORTANT_PROVIDER = null;
    private static final Location INVALID_LOCATION = null;
    private static final int BETTER_ACCURACY = 50;
    private static final long ACCURACY_BELOW_THRESHOLD = LocationAccuracy.BAD_ACCURACY_THRESHOLD - 1;
    private static final int WORST_ACCURACY = 600;
    private static final long ONE_SECOND = 1000L;
    private static final long LOCATION_EXPIRY_TIME = ONE_SECOND * 60 * 4;
    private static final long NOW = System.currentTimeMillis();
    private static final long ONE_SECOND_AGO = NOW - ONE_SECOND;
    private static final long OUTDATED = NOW - ( LOCATION_EXPIRY_TIME + 1L );

    LocatorSettings settings = new LocatorSettings(NOT_IMPORTANT, NOT_IMPORTANT);
    LocationAccuracy locationAccuracy = new LocationAccuracy(settings);
    Location newLocation = new Location(NOT_IMPORTANT_PROVIDER);
    Location currentLocation = new Location(NOT_IMPORTANT_PROVIDER);

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
        settings.setUpdatesInterval(LOCATION_EXPIRY_TIME);
        currentLocation.setTime(OUTDATED);
        newLocation.setTime(NOW);
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_time_is_outdated_by_the_settings_preference() {
        settings.setUpdatesInterval(LOCATION_EXPIRY_TIME);
        currentLocation.setTime(NOW);
        newLocation.setTime(OUTDATED);
        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_accuracy_value_is_better() {
        newLocationIsMoreAccurate();
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_less_accurate_and_from_the_same_time() {
        newLocationIsLessAccurate();
        newLocation.setTime(NOW);
        currentLocation.setTime(NOW);
        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_is_more_accurate_even_if_its_not_more_recent() {
        newLocationIsMoreAccurate();
        newLocation.setTime(ONE_SECOND_AGO);
        currentLocation.setTime(NOW);
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void be_better_if_the_new_location_is_more_recent_even_if_the_accuracy_is_the_same() {
        newLocationIsMoreRecent();
        newLocation.setAccuracy(BETTER_ACCURACY);
        currentLocation.setAccuracy(BETTER_ACCURACY);
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_more_recent_but_less_accurate() {
        newLocationIsMoreRecent();
        newLocationIsLessAccurate();
        assertThat(locationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_is_more_recent_and_less_accurate_but_below_a_minimum_accuracy_threshold() {
        newLocationIsMoreRecent();
        newLocationIsLessAccurateButBelowThreshold();
        assertThat(locationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_more_recent___less_accurate___below_a_minimum_accuracy_threshold___but_from_a_different_provider() {
        newLocationIsMoreRecent();
        newLocationIsLessAccurateButBelowThreshold();
        newLocation.setProvider("different provider");
        assertThat(locationIsBetter(), is(false));
    }

    private void newLocationIsMoreRecent() {
        newLocation.setTime(NOW);
        currentLocation.setTime(ONE_SECOND_AGO);
    }

    private void newLocationIsMoreAccurate() {
        newLocation.setAccuracy(BETTER_ACCURACY);
        currentLocation.setAccuracy(WORST_ACCURACY);
    }

    private void newLocationIsLessAccurate() {
        newLocation.setAccuracy(WORST_ACCURACY);
        currentLocation.setAccuracy(BETTER_ACCURACY);
    }

    private void newLocationIsLessAccurateButBelowThreshold() {
        newLocation.setAccuracy(ACCURACY_BELOW_THRESHOLD);
        currentLocation.setAccuracy(BETTER_ACCURACY);
    }

    private boolean locationIsBetter() {
        return locationAccuracy.isBetterLocation(newLocation, currentLocation);
    }

}
