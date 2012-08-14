package com.novoda.location.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import robolectricsetup.NovocationTestRunner;

import static android.content.pm.PackageManager.*;
import static org.mockito.Mockito.*;

@RunWith(NovocationTestRunner.class)
public class UnregisterPassiveListenerOnLowBatteryShould {
    public static final Intent INVALID_INTENT = null;
    Context context = spy(Robolectric.getShadowApplication().getApplicationContext());
    PackageManager packageManager = mock(PackageManager.class);
    ComponentName passiveLocationChanged = new ComponentName(context, PassiveLocationChanged.class);

    @Before
    public void setUp() throws Exception {
        doReturn(packageManager).when(context).getPackageManager();
    }

    private void verifyPassiveListenerIsEnabled() {
        verify(packageManager).setComponentEnabledSetting(eq(passiveLocationChanged), eq(COMPONENT_ENABLED_STATE_DEFAULT), eq(DONT_KILL_APP));
    }

    @Test
    public void enable_the_passive_listener_if_the_battery_is_not_low() throws Exception {
        Intent intent = new Intent(Intent.ACTION_BATTERY_OKAY);

        new UnregisterPassiveListenerOnLowBattery().onReceive(context, intent);

        verifyPassiveListenerIsEnabled();
    }

    @Test
    public void enable_the_passive_listener_if_the_intent_is_invalid() throws Exception {
        Intent intent = INVALID_INTENT;

        new UnregisterPassiveListenerOnLowBattery().onReceive(context, intent);

        verifyPassiveListenerIsEnabled();
    }

    private void verifyPassiveListenerIsDisabled() {
        verify(packageManager).setComponentEnabledSetting(eq(passiveLocationChanged), eq(COMPONENT_ENABLED_STATE_DISABLED), eq(DONT_KILL_APP));
    }

    @Test
    public void disable_the_passive_listener_if_the_battery_is_low() throws Exception {
        Intent intent = new Intent(Intent.ACTION_BATTERY_LOW);

        new UnregisterPassiveListenerOnLowBattery().onReceive(context, intent);

        verifyPassiveListenerIsDisabled();
    }
}
