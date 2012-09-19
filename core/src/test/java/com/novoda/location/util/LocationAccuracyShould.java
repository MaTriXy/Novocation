package com.novoda.location.util;

import android.location.Location;
import com.novoda.location.LocatorSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(NovocationTestRunner.class)
public class LocationAccuracyShould {

    private static final String NOT_IMPORTANT = "";
    private static final Location INVALID_LOCATION = null;
    private static final String INVALID_PROVIDER = null;
    private static final int BETTER_ACCURACY = 50;
    private static final long ACCURACY_BELOW_THRESHOLD = LocationAccuracy.BAD_ACCURACY_THRESHOLD - 1;
    private static final int WORST_ACCURACY = 600;
    private static final long ONE_SECOND = 1000L;
    private static final long LOCATION_EXPIRY_TIME = ONE_SECOND * 60 * 4;
    private static final long NOW = System.currentTimeMillis();
    private static final long ONE_SECOND_AGO = NOW - ONE_SECOND;
    private static final long OUTDATED = NOW - (LOCATION_EXPIRY_TIME + 1L);

    LocatorSettings settings = new LocatorSettings(NOT_IMPORTANT, NOT_IMPORTANT);
    LocationAccuracy locationAccuracy = new LocationAccuracy(settings);
    Location newLocation = new Location(NOT_IMPORTANT);
    Location currentLocation = new Location(NOT_IMPORTANT);

    @Test
    public void be_better_if_the_current_location_is_invalid() {
        currentLocation = INVALID_LOCATION;
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_invalid() {
        newLocation = INVALID_LOCATION;
        assertThat(newLocationIsBetter(), is(false));
    }

    @Test
    public void not_be_better_if_both_locations_are_the_same() {
        currentLocation = newLocation;
        assertThat(newLocationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_current_location_time_is_outdated_by_the_settings_preference() {
        settings.setUpdatesInterval(LOCATION_EXPIRY_TIME);
        currentLocation.setTime(OUTDATED);
        newLocation.setTime(NOW);
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_time_is_outdated_by_the_settings_preference() {
        settings.setUpdatesInterval(LOCATION_EXPIRY_TIME);
        currentLocation.setTime(NOW);
        newLocation.setTime(OUTDATED);
        assertThat(newLocationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_is_more_recent_and_more_accurate() {
        newLocationIsMoreRecent();
        newLocationIsMoreAccurate();
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void be_better_if_the_new_location_is_more_accurate_even_if_its_not_more_recent() {
        newLocationIsMoreAccurate();
        newLocation.setTime(ONE_SECOND_AGO);
        currentLocation.setTime(NOW);
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void be_better_if_the_new_location_is_more_accurate_and_both_locations_are_from_the_same_time() {
        newLocationIsMoreAccurate();
        setBothLocationsTimeToBeTheSame();
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_less_accurate_and_both_locations_are_from_the_same_time() {
        newLocationIsLessAccurate();
        setBothLocationsTimeToBeTheSame();
        assertThat(newLocationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_is_more_recent_even_if_the_accuracy_is_the_same() {
        newLocationIsMoreRecent();
        newLocation.setAccuracy(BETTER_ACCURACY);
        currentLocation.setAccuracy(BETTER_ACCURACY);
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_more_recent_but_less_accurate() {
        newLocationIsMoreRecent();
        newLocationIsLessAccurate();
        assertThat(newLocationIsBetter(), is(false));
    }

    @Test
    public void be_better_if_the_new_location_is_more_recent_and_less_accurate_but_below_a_minimum_accuracy_threshold() {
        newLocationIsMoreRecent();
        newLocationIsLessAccurateButBelowThreshold();
        assertThat(newLocationIsBetter(), is(true));
    }

    @Test
    public void not_be_better_if_the_new_location_is_more_recent___less_accurate___below_a_minimum_accuracy_threshold___but_from_a_different_provider() {
        newLocationIsMoreRecent();
        newLocationIsLessAccurateButBelowThreshold();
        newLocation.setProvider("different provider");
        assertThat(newLocationIsBetter(), is(false));
    }

    private void assertThatLocationAccuracyDoesntCrash() {
        try {
            newLocationIsBetter();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void handle_a_location_having_an_invalid_provider() {
        newLocation.setProvider(INVALID_PROVIDER);
        assertThatLocationAccuracyDoesntCrash();
    }

    @Test
    public void handle_both_locations_having_an_invalid_providers() {
        newLocation.setProvider(INVALID_PROVIDER);
        currentLocation.setProvider(INVALID_PROVIDER);
        assertThatLocationAccuracyDoesntCrash();
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

    private void setBothLocationsTimeToBeTheSame() {
        newLocation.setTime(NOW);
        currentLocation.setTime(NOW);
    }

    private boolean newLocationIsBetter() {
        return locationAccuracy.isBetterLocation(newLocation, currentLocation);
    }

}
