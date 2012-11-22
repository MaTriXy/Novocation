package com.novoda.location.provider.updater;

import android.app.AlarmManager;
import android.location.LocationManager;
import com.novoda.location.util.ApiLevelDetector;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdaterFactoryShould {

    ApiLevelDetector apiLevelDetector = mock(ApiLevelDetector.class);
    AlarmManager alarmManager = null;
    LocationManager locationManager = null;

    @Test
    public void return_a_legacy_instance_when_running_eclair_or_older() {
        when(apiLevelDetector.supportsFroyo()).thenReturn(false);
        when(apiLevelDetector.supportsGingerbread()).thenReturn(false);

        LocationUpdaterFactory factory = new LocationUpdaterFactory(locationManager, apiLevelDetector, alarmManager);
        LocationUpdater updater = factory.getLocationUpdater();

        assertTrue(updater instanceof LegacyLocationUpdater);
    }

    @Test
    public void return_a_froyo_instance_when_running_froyo() {
        when(apiLevelDetector.supportsFroyo()).thenReturn(true);
        when(apiLevelDetector.supportsGingerbread()).thenReturn(false);

        LocationUpdaterFactory factory = new LocationUpdaterFactory(locationManager, apiLevelDetector, alarmManager);
        LocationUpdater updater = factory.getLocationUpdater();

        assertTrue(updater instanceof FroyoLocationUpdater);
    }

    @Test
    public void return_a_gingerbread_instance_when_running_gingerbreader_or_higher() {
        when(apiLevelDetector.supportsGingerbread()).thenReturn(true);
        when(apiLevelDetector.supportsFroyo()).thenReturn(true);

        LocationUpdaterFactory factory = new LocationUpdaterFactory(locationManager, apiLevelDetector, alarmManager);
        LocationUpdater updater = factory.getLocationUpdater();

        assertTrue(updater instanceof GingerbreadLocationUpdater);
    }

}
