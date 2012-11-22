package com.novoda.location.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.provider.store.SettingsDao;
import com.novoda.location.util.ApiLevelDetector;
import com.novoda.location.util.SettingsDaoUtil;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class RestorePassiveListenerBootShould {

    static final Intent UNIMPORTANT_INTENT = null;
    final LocationManager locationManager = mock(LocationManager.class);
    final Context context = spy(Robolectric.getShadowApplication().getApplicationContext());
    final SettingsDao settingsDao = new SettingsDaoUtil().getSettingsDao();
    final LocatorSettings settings = new LocatorSettings("");
    final RestorePassiveListenerBoot restorePassiveListenerBoot = new RestorePassiveListenerBoot();
    final ApiLevelDetector apiLevelDetector = mock(ApiLevelDetector.class);

    @Before
    public void setUp() throws Exception {
        doReturn(locationManager).when(context).getSystemService(eq(Context.LOCATION_SERVICE));
        when(apiLevelDetector.supportsGingerbread()).thenReturn(true);

        restorePassiveListenerBoot.setApiLevelDetector(apiLevelDetector);
        Locator locator = mock(Locator.class);
        when(locator.getSettings()).thenReturn(settings);
        LocatorFactory.setLocator(locator);
    }

    @After
    public void tearDown() throws Exception {
        LocatorFactory.setLocator(null);
    }

    @Test
    public void request_passive_location_updates_if_the_app_has_run_once_and_passive_update_settings_are_enabled() throws Exception {
        settings.setEnablePassiveUpdates(true);

        settingsDao.persistSettingsToPreferences(context, settings);

        restorePassiveListenerBoot.onReceive(context, UNIMPORTANT_INTENT);

        verify(locationManager).requestLocationUpdates(eq(LocationManager.PASSIVE_PROVIDER), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void NOT_request_for_passive_updates_if_its_enabled_in_the_settings() throws Exception {
        settings.setEnablePassiveUpdates(false);

        settingsDao.persistSettingsToPreferences(context, settings);

        restorePassiveListenerBoot.onReceive(context, UNIMPORTANT_INTENT);

        verify(locationManager, never()).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }

    @Test
    public void NOT_request_for_passive_updates_if_the_application_has_not_run_once_yet() throws Exception {
        assertThat(settingsDao.hasApplicationRunOnce(context), is(false));

        restorePassiveListenerBoot.onReceive(context, UNIMPORTANT_INTENT);

        verify(locationManager, never()).requestLocationUpdates(anyString(), anyLong(), anyFloat(), any(PendingIntent.class));
    }
}
