package com.novoda.location.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.LocationProviderFactory;
import com.novoda.location.provider.store.SettingsDao;
import com.novoda.location.util.ApiLevelDetector;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;
import robolectricsetup.RobolectricFakeApp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class RestorePassiveListenerBootShould {

    final LocationManager locationManager = mock(LocationManager.class);
    final Context context = spy(Robolectric.getShadowApplication().getApplicationContext());
    final SettingsDao settingsDao = new LocationProviderFactory().getSettingsDao();
    final LocatorSettings settings = new LocatorSettings("", "");
    final RestorePassiveListenerBoot restorePassiveListenerBoot = new RestorePassiveListenerBoot();

    @Before
    public void setUp() throws Exception {
        doReturn(locationManager).when(context).getSystemService(eq(Context.LOCATION_SERVICE));
    }

    @Test
    public void request_passive_location_updates_if_the_app_has_run_once_and_passive_update_settings_are_enabled() throws Exception {
        settings.setEnablePassiveUpdates(true);

        settingsDao.persistSettingsToPreferences(context, settings);

        restorePassiveListenerBoot.onReceive(context, null);

        verify(locationManager).requestLocationUpdates(eq(LocationManager.PASSIVE_PROVIDER), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void NOT_request_for_passive_updates_if_its_enabled_in_the_settings() throws Exception {
        settings.setEnablePassiveUpdates(false);

        settingsDao.persistSettingsToPreferences(context, settings);

        restorePassiveListenerBoot.onReceive(context, null);

        verify(locationManager, never()).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void NOT_request_for_passive_updates_if_the_application_has_not_run_once_yet() throws Exception {
        assertThat(settingsDao.hasApplicationRunOnce(context), is(false));

        restorePassiveListenerBoot.onReceive(context, null);

        verify(locationManager, never()).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }
}
