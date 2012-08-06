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

    @Test
    public void be_more_accurate_if_the_current_location_is_not_valid() {
        LocatorSettings settings = new LocatorSettings("", "");
        LocationAccuracy locationAccuracy = new LocationAccuracy(settings);

        Location newLocation = new Location("");

        boolean locationIsBetter = locationAccuracy.isBetterLocation(newLocation, null);

        assertThat(locationIsBetter, is(true));
    }
}
