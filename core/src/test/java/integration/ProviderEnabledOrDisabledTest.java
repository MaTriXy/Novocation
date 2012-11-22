package integration;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;
import com.novoda.location.receiver.LocationChanged;
import com.novoda.location.util.ApiLevelDetector;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class ProviderEnabledOrDisabledTest {

    final Context context = spy(Robolectric.getShadowApplication().getApplicationContext());
    final LocationManager locationManager = mock(LocationManager.class);
    final LocatorSettings settings = new LocatorSettings("com.example.update");
    final Locator locator = LocatorFactory.getInstance();
    private ApiLevelDetector apiLevelDetector = mock(ApiLevelDetector.class);

    @Before
    public void setUp() throws Exception {
        doReturn(LocationManager.GPS_PROVIDER).when(locationManager).getBestProvider(any(Criteria.class), anyBoolean());
        doReturn(locationManager).when(context).getSystemService(eq(Context.LOCATION_SERVICE));
        when(apiLevelDetector.supportsFroyo()).thenReturn(true);
        locator.prepare(context, settings, apiLevelDetector);
    }

    private void changeProviderStateTo(boolean enabled) {
        LocationChanged locationChanged = new LocationChanged();
        Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, enabled);
        locationChanged.onReceive(context, intent);
    }

    private void enableProvider() {
        changeProviderStateTo(true);
    }

    @Test
    public void locator_should_restart_location_updates_when_a_provider_is_enabled() throws NoProviderAvailable {
        locator.startLocationUpdates();

        enableProvider();

        verify(locationManager, times(2)).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void locator_should_disable_location_updates_when_a_provider_is_enabled() throws NoProviderAvailable {
        locator.startLocationUpdates();

        enableProvider();

        verify(locationManager, times(3)).removeUpdates(any(PendingIntent.class));
    }

    private void disableProvider() {
        changeProviderStateTo(false);
    }

    @Test
    public void locator_should_restart_location_updates_when_a_provider_is_disabled() throws NoProviderAvailable {
        locator.startLocationUpdates();

        disableProvider();

        verify(locationManager, times(2)).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void locator_should_disable_location_updates_when_a_provider_is_disabled() throws NoProviderAvailable {
        locator.startLocationUpdates();

        disableProvider();

        verify(locationManager, times(3)).removeUpdates(any(PendingIntent.class));
    }

}
