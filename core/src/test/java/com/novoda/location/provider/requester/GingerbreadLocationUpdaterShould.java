package com.novoda.location.provider.requester;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;
import com.novoda.location.LocatorSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(NovocationTestRunner.class)
public class GingerbreadLocationUpdaterShould {

    LocationManager locationManager = mock(LocationManager.class);
    GingerbreadLocationUpdater updater = new GingerbreadLocationUpdater(locationManager);
    LocatorSettings settings = new LocatorSettings("");

    long time = 1000 * 60;
    float distance = 300.50F;
    Criteria criteria = mock(Criteria.class);
    PendingIntent pendingIntent = mock(PendingIntent.class);

    @Before
    public void setUp() throws Exception {
        settings.setUpdatesInterval(time);
        settings.setUpdatesDistance(distance);
    }

    @Test
    public void request_location_updates_from_the_location_manager_using_criteria() throws Exception {
        updater.startActiveLocationUpdates(settings, criteria, pendingIntent);

        verify(locationManager).requestLocationUpdates(eq(time), eq(distance), eq(criteria), eq(pendingIntent));
    }

}
